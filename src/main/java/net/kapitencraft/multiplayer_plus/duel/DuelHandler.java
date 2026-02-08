package net.kapitencraft.multiplayer_plus.duel;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

public class DuelHandler extends SavedData {
    private static DuelHandler INSTANCE;
    public List<Duel> duels = new ArrayList<>();

    public void addDuel(Duel duel) {
        duels.add(duel);
    }

    public static DuelHandler getInstance() {
        return INSTANCE;
    }

    public static void setInstance(DuelHandler duelHandler) {
        INSTANCE = duelHandler;
    }

    public static DuelHandler load(CompoundTag tag, MinecraftServer server) {
        int i = 0;
        DuelHandler handler = new DuelHandler();
        while (tag.contains("duel" + i)) {
            Duel duel = Duel.load(tag.getCompound("duel" + i), server);
            handler.addDuel(duel);
        }
        return handler;
    }

    public Duel computeOrAdd(ServerPlayer player) {
        if (getDuel(player) != null) {
            return getDuel(player);
        }
        Duel duel = new Duel(player);
        duels.add(duel);
        return duel;
    }

    public Duel getDuel(ServerPlayer player) {
        for (Duel duel : duels) {
            if (duel.isMember(player)) {
                return duel;
            }
        }
        return null;
    }

    public boolean hasDuel(ServerPlayer player) {
        return getDuel(player) != null;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag duels = new ListTag();
        for (Duel duel : this.duels) {
            duels.add(duel.save());
        }
        tag.put("duels", duels);
        return tag;
    }
}