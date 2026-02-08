package net.kapitencraft.multiplayer_plus.guild;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.kapitencraft.kap_lib.client.gui.browse.IBrowsable;
import net.kapitencraft.kap_lib.collection.MapStream;
import net.kapitencraft.kap_lib.helpers.*;
import net.kapitencraft.multiplayer_plus.guild.client.GuildScreen;
import net.kapitencraft.multiplayer_plus.pings.visuals.Pingable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
public class Guild implements Pingable, IBrowsable {
    public static final Logger LOGGER = LoggerFactory.getLogger("Guild");
    //region network
    public static final StreamCodec<RegistryFriendlyByteBuf, Guild> STREAM_CODEC = StreamCodec.of(Guild::toNetwork, Guild::new);
    private final StreamCodec<ByteBuf, WarContainer> WAR_CONTAINER_STREAM_CODEC = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).map(WarContainer::new, WarContainer::getRaw);

    public Guild(RegistryFriendlyByteBuf buf) {
        this.name = buf.readUtf();
        this.ranks = RankContainer.STREAM_CODEC.decode(buf);
        this.members = membersFromNw(buf);
        this.wars = WAR_CONTAINER_STREAM_CODEC.decode(buf);
        this.banner = ItemStack.STREAM_CODEC.decode(buf);
        this.upgrades = GuildUpgradeContainer.STREAM_CODEC.decode(buf);
        this.isPublic = buf.readBoolean();
    }


    private MemberContainer membersFromNw(RegistryFriendlyByteBuf buf) {
        MemberContainer container = new MemberContainer(buf.readUUID());
        container.invites.putAll(MemberContainer.INVITES_CODEC.decode(buf));
        container.members.putAll(container.MEMBERS_CODEC.decode(buf));
        container.banned.putAll(MemberContainer.BANNED_CODEC.decode(buf));
        return container;
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, Guild guild) {
        buf.writeUtf(guild.name);
        guild.ranks.toNw(buf);
        guild.members.toNw(buf);
        guild.WAR_CONTAINER_STREAM_CODEC.encode(buf, guild.wars);
        ItemStack.STREAM_CODEC.encode(buf, guild.banner);
        GuildUpgradeContainer.STREAM_CODEC.encode(buf, guild.upgrades);
        buf.writeBoolean(guild.isPublic);
    }
    //endregion

    private static final String NAME_ID = "Name", MEMBER_ID = "Members", BANNER_ID = "Banner", UPGRADES_ID = "Upgrades", WARS_ID = "Wars", RANKS_ID = "Ranks";

    //TODO add guild marker next player name (chat & ig)

    //Fields
    private final String name;
    private final MemberContainer members;
    private final RankContainer ranks;
    private boolean isPublic = false;
    private final WarContainer wars;
    private final ItemStack banner;
    private final GuildUpgradeContainer upgrades;

    private boolean removed = false;

    public Guild(String name, Player ownerId, ItemStack banner, boolean isPublic) {
        this.name = name;
        this.members = new MemberContainer(ownerId.getUUID());
        this.ranks = new RankContainer();
        this.banner = banner;
        this.upgrades = new GuildUpgradeContainer();
        this.wars = new WarContainer(Lists.newArrayList());
        this.isPublic = isPublic;
    }

    //region load
    @SuppressWarnings("ALL")
    public static Guild loadFromTag(CompoundTag tag, HolderLookup.Provider provider) {
        try {
            return new Guild(provider, tag);
        } catch (Exception e) {
            Guild.LOGGER.warn("unable to read Guild: {}", e.getMessage());
            return null;
        }
    }

    public Guild(HolderLookup.Provider provider, CompoundTag tag) {
        this.name = tag.getString(NAME_ID);
        this.banner = ItemStack.parseOptional(provider, tag);
        if (!this.banner.is(ItemTags.BANNERS)) {
            throw new IllegalStateException("Guild has no valid banner!");
        }
        this.upgrades = GuildUpgradeContainer.load(tag.getCompound(UPGRADES_ID));
        this.ranks = RankContainer.load(tag.getCompound(RANKS_ID));
        this.members = this.loadMembers(tag.getCompound(MEMBER_ID));
        this.wars = this.loadWarOpponents(tag.getCompound(WARS_ID));
    }

    private MemberContainer.ContainerElement loadContainerElement(CompoundTag tag, String id) {
        CompoundTag tag1 = tag.getCompound(id);
        IRank rank = Guild.this.ranks.getByName(tag1.getString("Rank"));
        MemberContainer.ContainerElement element = new MemberContainer.ContainerElement(rank);
        if (tag1.contains("Name", 8)) {
            element.name = tag1.getString("Name");
        }
        if (tag1.contains("MutedUntil", 4)) {
            element.mutedUntil = tag1.getLong("MutedUntil");
        }
        return element;
    }

    MemberContainer loadMembers(CompoundTag tag) {
        UUID owner = tag.getUUID("Owner");
        Map<UUID, String> invites = IOHelper.readMap(tag.getList("Invites", 10), CompoundTag::getUUID, CompoundTag::getString).toMap();
        Map<UUID, MemberContainer.ContainerElement> members = IOHelper.readMap(tag.getList(MEMBER_ID, 10), CompoundTag::getUUID, this::loadContainerElement).toMap();
        MemberContainer container = new MemberContainer(owner);
        container.members.putAll(members);
        container.invites.putAll(invites);
        return container;
    }

    WarContainer loadWarOpponents(CompoundTag tag) {
        return new WarContainer(IOHelper.readList(tag, "Opponents", StringTag.class, StringTag::getAsString, 8).toList());
    }
    //endregion load

    public CompoundTag save(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString(NAME_ID, name);
        tag.put(MEMBER_ID, members.save());
        tag.put(BANNER_ID, this.banner.save(provider));
        tag.put(UPGRADES_ID, upgrades.save());
        tag.put(WARS_ID, wars.save());
        CompoundTag tag1 = new CompoundTag();
        ranks.save(tag1);
        tag.put(RANKS_ID, tag1);
        return tag;
    }

    public void setRank(Player player, String rankId) {
        this.members.addMember(player, this.ranks.getByName(rankId));
    }

    //region upgrades
    public int getProtectionRange() {
        return 16 + (8 * this.upgrades.getUpgradeLevel(GuildUpgrades.RANGE));
    }

    public boolean upgrade(GuildUpgrades toUpgrade) {
        return this.upgrades.upgrade(toUpgrade);
    }
    //endregion

    //region member
    public int getMemberAmount() {
        return members.size();
    }

    public boolean containsMember(UUID member) {
        return this.members.members.containsKey(member);
    }

    public Collection<UUID> getAllPlayerIds() {
        return this.members.members.keySet();
    }

    public String addInvitation(Player player) {
        return this.members.addInvitation(player.getUUID());
    }

    public boolean acceptInvitation(Player player, String inviteKey) {
        return this.members.acceptInvite(player, inviteKey);
    }

    public boolean isOwner(Player player) {
        return this.members.cachedOwner == player;
    }

    public boolean canDoAction(Player player, int permissionLevel) {
        Map<UUID, MemberContainer.ContainerElement> elementMap = this.members.members;
        return elementMap.containsKey(player.getUUID()) && elementMap.get(player.getUUID()).rank.getPermissionLevel() >= permissionLevel;
    }

    public void muteMember(UUID player, Long until) {
        this.members.mute(player, until);
    }

    public void banPlayer(UUID player, Long until) {
        this.members.ban(player, until);
    }

    public class MemberContainer {
        public static final String PLAYER_GUILD_NAME_TAG = "GuildName";
        private static final StreamCodec<ByteBuf, Map<UUID, String>> INVITES_CODEC = ByteBufCodecs.STRING_UTF8.apply(ExtraStreamCodecs.map(UUIDUtil.STREAM_CODEC));
        private final StreamCodec<RegistryFriendlyByteBuf, Map<UUID, ContainerElement>> MEMBERS_CODEC = StreamCodec.of((buffer, value) -> value.toNw(buffer), this::elementFromNw).apply(ExtraStreamCodecs.map(UUIDUtil.STREAM_CODEC));
        private static final StreamCodec<ByteBuf, Map<UUID, Long>> BANNED_CODEC = ByteBufCodecs.VAR_LONG.apply(ExtraStreamCodecs.map(UUIDUtil.STREAM_CODEC));

        private final Map<UUID, String> invites = new HashMap<>();
        private final Map<UUID, ContainerElement> members = new HashMap<>();
        private final Map<UUID, Long> banned = new HashMap<>();
        private final UUID rawOwner;
        Player cachedOwner = null;
        private final Map<Player, IRank> onlineMembers = new HashMap<>();

        public MemberContainer(UUID rawOwner) {
            this.rawOwner = rawOwner;
            this.members.put(rawOwner, new ContainerElement(Rank.OWNER));
        }

        private MemberContainer(RegistryFriendlyByteBuf buf) {
            this.invites.putAll(INVITES_CODEC.decode(buf));
            this.members.putAll(MEMBERS_CODEC.decode(buf));
            this.banned.putAll(BANNED_CODEC.decode(buf));
            this.rawOwner = buf.readUUID();
        }

        int size() {
            return members.size();
        }

        String getOwnerName() {
            return members.get(this.rawOwner).name;
        }

        public void initialize(ServerLevel level) {
            MinecraftServer server = level.getServer();
            GameProfileCache cache = server.getProfileCache();
            Guild.LOGGER.debug("Guild {} is initializing their members", Guild.this.getGuildName());
            this.members.forEach((uuid, containerElement) -> {
                Optional<GameProfile> optional = cache.get(uuid);
                optional.ifPresent(gameProfile -> containerElement.name = gameProfile.getName());
            });
        }

        void setOnline(Player player) {
            if (this.members.containsKey(player.getUUID())) {
                IRank rank = members.get(player.getUUID()).rank;
                this.onlineMembers.put(player, members.get(player.getUUID()).rank);
                if (rank == Rank.OWNER) {
                    if (cachedOwner != null) {
                        LOGGER.error("Found guild ({}) with multiple owners ({} & {})",
                                Guild.this.getGuildName(),
                                cachedOwner.getGameProfile().getName(),
                                player.getGameProfile().getName()
                        );
                        return;
                    }
                    cachedOwner = player;
                }
                if (!Objects.equals(player.getGameProfile().getName(), this.members.get(player.getUUID()).name)) {
                    this.members.get(player.getUUID()).name = player.getGameProfile().getName();
                }
            }
        }

        public void setOffline(Player player) {
            this.onlineMembers.remove(player);
            if (player == cachedOwner) cachedOwner = null;
        }

        public boolean isBanned(UUID uuid) {
            if (banned.containsKey(uuid)) {
                if (banned.get(uuid) >= Util.getMillis()) {
                    banned.remove(uuid);
                    return false;
                }
                return true;
            }
            return false;
        }

        public void ban(UUID uuid, long l) {
            banned.put(uuid, l);
        }

        public boolean isMuted(UUID uuid) {
            if (members.containsKey(uuid) && members.get(uuid).mutedUntil != -1) {
                if (members.get(uuid).mutedUntil >= Util.getMillis()) {
                    members.remove(uuid);
                    return false;
                }
                return true;
            }
            return false;
        }

        public void mute(UUID uuid, long l) {
            if (this.members.containsKey(uuid)) {
                this.members.get(uuid).mutedUntil = l;
            }
        }

        public void disband() {
            this.onlineMembers.keySet().forEach(player -> player.getPersistentData().remove(PLAYER_GUILD_NAME_TAG));
            this.onlineMembers.clear();
            this.members.clear();
            this.cachedOwner = null;
        }

        int getOnlineMembers() {
            return this.onlineMembers.size();
        }

        public String addInvitation(UUID player) {
            if (members.containsKey(player)) {
                return "isMember";
            } else if (invites.containsKey(player)) {
                return "isInvited";
            }
            String inviteKey = TextHelper.createRandom(8);
            invites.put(player, inviteKey);
            return inviteKey;
        }

        void sendMSGtoAllMembers(Component toSend) {
            this.onlineMembers.keySet().forEach(player -> player.sendSystemMessage(toSend));
        }

        boolean contains(Player player) {
            return this.members.containsKey(player.getUUID());
        }

        public void addMember(Player player) {
            insertNewMember(player, Rank.DEFAULT);
        }

        private void insertNewMember(Player player, IRank rank) {
            this.onlineMembers.put(player, rank);
            this.members.put(player.getUUID(), new ContainerElement(rank));
            player.getPersistentData().putString(PLAYER_GUILD_NAME_TAG, Guild.this.getGuildName());
        }

        public void addMember(Player player, IRank rank) {
            insertNewMember(player, rank);
        }

        boolean acceptInvite(Player player, String inviteKey) {
            UUID playerId = player.getUUID();
            if (this.invites.containsKey(playerId) && Objects.equals(this.invites.get(playerId), inviteKey)) {
                this.addMember(player);
                this.invites.remove(playerId);
                return true;
            }
            return false;
        }

        public void removeMember(Player player) {
            this.onlineMembers.remove(player);
            this.members.remove(player.getUUID());
            player.getPersistentData().remove(PLAYER_GUILD_NAME_TAG);
        }

        public String promotePlayer(UUID player, @Nullable IRank rank) {
            if (members.containsKey(player) && !removed) {
                IRank nextRank;
                IRank currentRank = members.get(player).rank;
                if (rank != null) {
                    if (currentRank == rank) {
                        return "isSame";
                    }
                    nextRank = rank;
                } else if (currentRank instanceof Rank rank1) {
                    switch (rank1) {
                        case DEFAULT -> nextRank = Rank.APPRENTICE;
                        case APPRENTICE -> nextRank = Rank.OFFICER;
                        case OFFICER -> nextRank = Rank.MASTER;
                        default -> {
                            return "alreadyMax";
                        }
                    }
                } else {
                    return "noInfo";
                }
                members.get(player).rank = nextRank;
                return "success";
            }
            return player == rawOwner ? "isOwner" : "notMember";
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Owner", rawOwner);
            tag.put("Invites", IOHelper.writeMap(this.invites, NbtUtils::createUUID, StringTag::valueOf));
            tag.put(MEMBER_ID, IOHelper.writeMap(this.members, NbtUtils::createUUID, ContainerElement::save));
            return tag;
        }

        public void toNw(RegistryFriendlyByteBuf buf) {
            buf.writeUUID(this.rawOwner);
            INVITES_CODEC.encode(buf, this.invites);
            MEMBERS_CODEC.encode(buf, this.members);
            BANNED_CODEC.encode(buf, this.banned);
        }

        private ContainerElement elementFromNw(RegistryFriendlyByteBuf buf) {
            ContainerElement element = new ContainerElement(Guild.this.ranks.getByName(buf.readUtf()));
            element.mutedUntil = buf.readLong();
            element.name = buf.readUtf();
            return element;
        }

        private static class ContainerElement {
            private IRank rank;
            private long mutedUntil = -1;
            private String name;

            public ContainerElement(IRank rank) {
                this.rank = rank;
            }

            public static Tag save(ContainerElement inst) {
                CompoundTag tag = new CompoundTag();
                tag.putString("Rank", inst.rank.getRegistryName());
                if (inst.name != null) {
                    tag.putString("Name", inst.name);
                }
                if (inst.mutedUntil != -1) {
                    tag.putLong("MutedUntil", inst.mutedUntil);
                }
                return tag;
            }

            public void toNw(RegistryFriendlyByteBuf buf) {
                buf.writeUtf(rank.getRegistryName());
                buf.writeLong(mutedUntil);
                buf.writeUtf(name);
            }
        }
    }

    void setOffline(Player player) {
        this.members.setOffline(player);
    }

    public void setOnline(Player player) {
        this.members.setOnline(player);
    }

    //region rank
    public IRank getRank(Player player) {
        return this.members.onlineMembers.get(player);
    }

    public String promote(Player player, @Nullable Rank rank) {
        return this.members.promotePlayer(player.getUUID(), rank);
    }

    public static class RankContainer {
        private static final StreamCodec<ByteBuf, Map<String, CustomRank>> RANKS_CODEC = CustomRank.STREAM_CODEC.apply(ExtraStreamCodecs.map(ByteBufCodecs.STRING_UTF8));
        private static final StreamCodec<ByteBuf, RankContainer> STREAM_CODEC = RANKS_CODEC.map(RankContainer::fromNw, RankContainer::getRanks);

        private Map<String, CustomRank> getRanks() {
            return this.customRanks;
        }

        private static RankContainer fromNw(Map<String, CustomRank> stringCustomRankMap) {
            RankContainer container = new RankContainer();
            container.customRanks.putAll(stringCustomRankMap);
            return container;
        }

        private final Map<String, CustomRank> customRanks = new HashMap<>();

        public IRank getByName(String name) {
            return MiscHelper.nonNullOr(Rank.CODEC.byName(name), customRanks.get(name));
        }

        private void loadData(CompoundTag tag) {
            this.customRanks.putAll(tag.getAllKeys().stream().collect(CollectorHelper.toValueMappedStream(tag::get)).filterValues(CompoundTag.class::isInstance, null).mapValues(CompoundTag.class::cast)
                    .mapToSimple(CustomRank::new).collect(CollectorHelper.toKeyMappedStream(CustomRank::getRegistryName)).toMap());
        }

        public static RankContainer load(CompoundTag tag) {
            RankContainer container = new RankContainer();
            container.loadData(tag);
            return container;
        }

        private void save(CompoundTag tag) {
            MapStream.of(this.customRanks).mapValues(CustomRank::save)
                    .forEach(tag::put);
        }

        public void toNw(RegistryFriendlyByteBuf buf) {

        }
    }

    public interface IRank {
        int DEFAULT_PERMISSION = 0, ACCEPT_INVITES = 1, MUTE_MEMBERS = 2, KICK_MEMBERS = 3, BAN_MEMBERS = 4, CHANGE_SETTINGS = 5, DISBAND = 6;

        Component getName();

        int getPermissionLevel();

        TextColor color();

        Tag save();

        String getRegistryName();
    }

    public enum Rank implements StringRepresentable, IRank {
        DEFAULT("default", DEFAULT_PERMISSION, TextColor.fromLegacyFormat(ChatFormatting.BLUE)),
        APPRENTICE("apprentice", ACCEPT_INVITES, TextColor.fromLegacyFormat(ChatFormatting.GREEN)),
        OFFICER("officer", MUTE_MEMBERS, TextColor.fromLegacyFormat(ChatFormatting.AQUA)),
        MASTER("master", BAN_MEMBERS, TextColor.fromLegacyFormat(ChatFormatting.DARK_AQUA)),
        OWNER("owner", DISBAND, TextColor.fromLegacyFormat(ChatFormatting.DARK_RED));


        static final EnumCodec<Rank> CODEC = StringRepresentable.fromEnum(Rank::values);
        private final String registryName;
        private final int permissionLevel;
        private final TextColor color;


        Rank(String registryName, int permissionLevel, @Nullable TextColor color) {
            this.registryName = registryName;
            this.permissionLevel = permissionLevel;
            if (color == null) this.color = TextColor.fromRgb(-1);
            else this.color = color;
        }

        @Override
        public String getRegistryName() {
            return registryName;
        }

        @Override
        public @NotNull String getSerializedName() {
            return registryName;
        }

        @Override
        public StringTag save() {
            return StringTag.valueOf(this.registryName);
        }

        @Override
        public Component getName() {
            return Component.translatable(this.registryName);
        }

        @Override
        public int getPermissionLevel() {
            return permissionLevel;
        }

        @Override
        public TextColor color() {
            return color;
        }
    }

    public static class CustomRank implements IRank {
        public static final StreamCodec<ByteBuf, CustomRank> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, CustomRank::getRegistryName,
                ByteBufCodecs.INT, r -> r.color.getValue(),
                ByteBufCodecs.INT, CustomRank::getPermissionLevel,
                CustomRank::new
        );
        private final Component name;
        private final String stringName;
        private final TextColor color;
        private final int permissionLevel;

        public CustomRank(String name, int color, int permissionLevel) {
            this.name = Component.literal(name);
            this.stringName = name;
            this.color = TextColor.fromRgb(color);
            this.permissionLevel = permissionLevel;
        }

        public CustomRank(String name, CompoundTag tag) {
            this(name, tag.getInt("Color"), tag.getInt("PermissionLevel"));
        }

        @Override
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Color", this.color.getValue());
            tag.putInt("PermissionLevel", this.permissionLevel);
            return tag;
        }

        @Override
        public Component getName() {
            return this.name;
        }

        @Override
        public int getPermissionLevel() {
            return this.permissionLevel;
        }

        @Override
        public String getRegistryName() {
            return stringName;
        }

        @Override
        public TextColor color() {
            return this.color;
        }
    }
    //endregion rank

    public boolean memberLeave(Player member) {
        if (containsMember(member.getUUID())) {
            removeMember(member);
            return true;
        }
        return false;
    }

    public void kickMember(@Nullable Player member, KickStatus status) {
        if (member != null && containsMember(member.getUUID()) && !removed) {
            removeMember(member);
            member.sendSystemMessage(Component.translatable("guild." + status.getSerializedName(), this.getGuildName()));
        }
    }

    public enum KickStatus implements StringRepresentable {
        LEAVE("leave"),
        KICK("kick"),
        BAN("ban"),
        DISBAND("disband");

        private static final EnumCodec<KickStatus> CODEC = StringRepresentable.fromEnum(KickStatus::values);
        private final String name;

        KickStatus(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

    private void removeMember(Player player) {
        this.members.removeMember(player);
    }

    public void addMember(Player player) {
        this.members.addMember(player);
    }

    //endregion

    public void disband() {
        this.members.disband();
    }

    @Override
    public void openScreen() {
        Minecraft.getInstance().setScreen(new GuildScreen(this));
    }

    @Override
    public Component getName() {
        return Component.literal(this.getGuildName());
    }

    //region war
    public WarContainer getWarInstance() {
        return wars;
    }

    public class WarContainer {

        private List<String> getRaw() {
            return rawOpponents;
        }

        private final List<Guild> opponents = new ArrayList<>();
        private final List<String> rawOpponents = new ArrayList<>();

        public WarContainer(List<String> rawOpponents) {
            this.rawOpponents.addAll(rawOpponents);
        }

        public String finalizeWar(Guild opponent) {
            if (opponents.contains(opponent)) {
                opponent.wars.opponents.remove(Guild.this);
                opponents.remove(opponent);
                return "success";
            }
            return "noOpponent";
        }

        void load(GuildHandler handler) {
            rawOpponents.stream().map(handler::getGuild).forEach(opponents::add);
        }

        public String startWar(Guild other) {
            if (checkStartPossible()) {
                Guild.this.members.sendMSGtoAllMembers(Component.translatable("guild.war.start", other.makeDisplay()));
                return "success";
            }
            return "fail";
        }

        private boolean checkStartPossible() {
            return Guild.this.members.getOnlineMembers() > Guild.this.members.size() / 2;
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.put("Opponents", IOHelper.writeList(rawOpponents, StringTag::valueOf));
            return tag;
        }
    }

    public void reviveWarOpponents(GuildHandler handler) {
        this.wars.load(handler);
    }
    //endregion

    private Component makeDisplay() {
        return Component.literal(this.getGuildName()).withStyle(Style.EMPTY
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextHelper.listToPlainText(description(false)))));
    }

    @Override
    public String display() {
        return this.getGuildName();
    }

    private List<Component> description(boolean suppressed) {
        List<Component> list = Lists.newArrayList();
        list.add(Component.literal(getGuildName()));
        list.add(Component.translatable("guild.owner", this.members.getOwnerName()));
        list.add(Component.translatable("guild.member_count", getMemberAmount()));
        if (suppressed || getMemberAmount() > 5) {
            list.add(Component.literal(String.join(", ", this.members.members.values().stream().map(containerElement -> containerElement.name).toList())));
        } else {
            this.members.members.forEach((uuid, s) -> {
                IRank rank = this.members.members.get(uuid).rank;
                list.add(Component.literal(s.name).append(": ").append(rank.getName()));
            });
        }
        return list;
    }

    public void remove() {
        isPublic = false;
        removed = true;
    }

    public String getGuildName() {
        return name;
    }

    public boolean isPublic() {
        return isPublic && !removed;
    }

    public void setPublic(boolean aPublic) {
        if (!removed) isPublic = aPublic;
    }

    public ItemStack getBanner() {
        return banner;
    }

    @Override
    public boolean pinges(Player player) {
        return this.members.contains(player);
    }
}