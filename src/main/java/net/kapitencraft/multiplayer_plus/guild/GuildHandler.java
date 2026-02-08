package net.kapitencraft.multiplayer_plus.guild;

import net.kapitencraft.kap_lib.collection.MapStream;
import net.kapitencraft.kap_lib.helpers.CollectorHelper;
import net.kapitencraft.kap_lib.helpers.IOHelper;
import net.kapitencraft.multiplayer_plus.MultiplayerPlusMod;
import net.kapitencraft.multiplayer_plus.network.S2C.AddGuildPacket;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Stream;


@EventBusSubscriber
public class GuildHandler extends SavedData {
    private static GuildHandler clientInstance;
    private final HashMap<String, Guild> guilds = new HashMap<>();

    public static GuildHandler getInstance(Level level) {
        if (level instanceof ClientLevel) {
            return getClientInstance();
        } else {
            ServerLevel serverLevel = (ServerLevel) level;
            return serverLevel.getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(
                    new Factory<>(GuildHandler::createDefault, GuildHandler::load, DataFixTypes.SAVED_DATA_RAIDS),
                    "multiplayer_plus:guilds"
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static GuildHandler getClientInstance() {
        ensureInstanceNotNull();
        return clientInstance;
    }

    public static GuildHandler createDefault() {
        Guild.LOGGER.info("no Guilds found; using default");
        return new GuildHandler();
    }

    public static Collection<Guild> all(Level level) {
        return getInstance(level).allGuilds();
    }

    public Collection<Guild> allGuilds() {
        return guilds.values();
    }

    public static void ensureInstanceNotNull() {
        if (clientInstance == null) clientInstance = new GuildHandler();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        MultiplayerPlusMod.LOGGER.info("saving Guilds...");
        tag.put("Guilds", saveAllGuilds(provider));
        return tag;
    }

    public static GuildHandler load(CompoundTag tag, HolderLookup.Provider provider) {
        Guild.LOGGER.info("loading Guilds...");
        return loadAllGuilds(tag, provider);
    }

    @ApiStatus.Internal
    public void addNewGuild(String name, boolean isPublic, ItemStack banner, Player owner) {
        this.setDirty();
        Guild guild = new Guild(name, owner, banner, isPublic);
        guilds.put(name, guild);
        owner.getPersistentData().putString(Guild.MemberContainer.PLAYER_GUILD_NAME_TAG, name);
        PacketDistributor.sendToAllPlayers(new AddGuildPacket(owner.getUUID(), name, isPublic, banner));
    }

    /**
     * only used for adding Guilds to the client instance, do not use!
     */
    @ApiStatus.Internal
    public void addGuild(String name, Player owner, ItemStack banner, boolean isPublic) {
        owner.getPersistentData().putString(Guild.MemberContainer.PLAYER_GUILD_NAME_TAG, name);
        this.guilds.put(name, new Guild(name, owner, banner, isPublic));
    }

    public String removeGuild(String guildName) {
        if (!this.guilds.containsKey(guildName)) {
            return "noSuchGuild";
        } else {
            this.setDirty();
            Guild guild = this.guilds.get(guildName);
            guild.disband();
            this.guilds.remove(guildName);
            return "success";
        }
    }

    public @Nullable Guild getGuildForBanner(ItemStack banner) {
        if (!(banner.getItem() instanceof BannerItem)) {
            return null;
        }
        return MapStream.of(allGuilds().stream()
                        .collect(CollectorHelper.toMapForKeys(Guild::getBanner)))
                .filterKeys(stack -> ItemStack.matches(stack, banner))
                .toMap()
                .values()
                .stream()
                .findFirst()
                .orElse(null);
    }

    public Guild getGuild(String name) {
        return guilds.get(name);
    }

    public Guild getGuildForPlayer(Player player) {
        return getGuild(player.getPersistentData().getString(Guild.MemberContainer.PLAYER_GUILD_NAME_TAG));
    }

    @SubscribeEvent
    public static void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        all(event.getEntity().level()).forEach(guild -> guild.setOnline(event.getEntity()));
    }

    @SubscribeEvent
    public static void playerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        all(event.getEntity().level()).forEach(guild -> guild.setOffline(event.getEntity()));
    }

    public static void addGuildClient(Guild guild) {
        ensureInstanceNotNull();
        clientInstance.reviveGuild(guild);
    }

    private void reviveGuild(Guild guild) {
        if (guild == null) {
            this.setDirty();
            return;
        }
        this.guilds.put(guild.getGuildName(), guild);
    }

    private static GuildHandler loadAllGuilds(CompoundTag tag, HolderLookup.Provider provider) {
        GuildHandler guildHandler = new GuildHandler();
        Stream<CompoundTag> tags = IOHelper.readCompoundList(tag, "Guilds");
        tags.map(t -> Guild.loadFromTag(t, provider)).forEach(guildHandler::reviveGuild);
        guildHandler.allGuilds().forEach(guild -> guild.reviveWarOpponents(guildHandler));
        return guildHandler;
    }

    public boolean isStackFromGuildBanner(ItemStack stack) {
        return this.allGuilds()
                .stream()
                .map(Guild::getBanner)
                .anyMatch(stack1 -> ItemStack.matches(stack1, stack));
    }

    public ListTag saveAllGuilds(HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();
        listTag.addAll(allGuilds().stream().map(g -> g.save(provider)).toList());
        this.guilds.clear();
        return listTag;
    }

    public void invalidate() {
        this.guilds.clear();
    }
}
