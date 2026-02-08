package net.kapitencraft.multiplayer_plus.duel;

import net.kapitencraft.kap_lib.helpers.CollectorHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Duel {
    private final List<ServerPlayer> team1 = new ArrayList<>();
    private final List<ServerPlayer> team2 = new ArrayList<>();
    private final ServerPlayer owner;

    private boolean completed = false;

    public Duel(ServerPlayer owner) {
        this.owner = owner;
        this.team1.add(owner);
    }

    public static Duel load(CompoundTag tag, MinecraftServer server) {
        CompoundTag team1Tag = tag.getCompound("team1");
        PlayerList list = server.getPlayerList();
        ServerPlayer player = list.getPlayer(UUID.fromString(team1Tag.getString("owner")));
        Duel duel = new Duel(player);
        team1Tag.getList("players", Tag.TAG_STRING).stream().map(Tag::getAsString).map(UUID::fromString).forEach(u -> duel.addToTeam(player, list.getPlayer(u)));
        CompoundTag team2Tag = tag.getCompound("team2");
        team2Tag.getList("players", Tag.TAG_STRING).stream().map(Tag::getAsString).map(UUID::fromString).forEach(u -> duel.addToEnemyTeam(player, list.getPlayer(u)));
        return duel;
    }

    public void addToTeam(ServerPlayer newTeammate, ServerPlayer member) {
        if (team1.contains(member)) {
            team1.add(newTeammate);
        } else if (team2.contains(member)) {
            team2.add(newTeammate);
        }
    }

    public void addToEnemyTeam(ServerPlayer newMember, ServerPlayer oldMember) {
        if (team1.contains(oldMember)) {
            team2.add(newMember);
        } else if (team2.contains(oldMember)) {
            team1.add(newMember);
        }
    }

    public CompoundTag save() {
        if (!completed) {
            CompoundTag tag = new CompoundTag();

            ListTag team1Players = new ListTag();
            team1.stream()
                    .filter(player -> !player.is(owner))
                    .map(ServerPlayer::getStringUUID)
                    .map(StringTag::valueOf)
                    .forEach(team1Players::add);
            tag.put("team1", team1Players);

            ListTag team2Players = new ListTag();
            team2.stream().map(ServerPlayer::getStringUUID)
                    .map(StringTag::valueOf)
                    .forEach(team2Players::add);
            tag.put("team2", team2Players);

            return tag;
        }
        return null;
    }

    public boolean isMember(ServerPlayer player) {
        return team1.contains(player) || team2.contains(player);
    }

    public boolean isOwner(ServerPlayer player) {
        return owner == player;
    }

    public boolean close() {
        if (completed) return false;
        this.completed = true;
        return true;
    }

    public int getMemberCount() {
        return team1.size() + team2.size();
    }
}
