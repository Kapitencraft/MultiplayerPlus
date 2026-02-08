package net.kapitencraft.multiplayer_plus.guild;

import com.google.common.collect.HashMultimap;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class GuildBlockProtection {

    private static final HashMultimap<Guild, BlockPos> protectedBlocks = HashMultimap.create();
    private static final HashMultimap<Guild, BlockPos> protectorBlocks = HashMultimap.create();

    public static void updateSize(Guild guild, int newSize, BlockPos core) {
        if (protectedBlocks.containsKey(guild) && !protectorBlocks.get(guild).contains(core)) {
            for (int x = -newSize; x < newSize; x++) {
                for (int z = -newSize; z < newSize; z++) {
                    BlockPos offset = core.offset(x, 0, z);
                    if (!protectedBlocks.get(guild).contains(offset)) protectedBlocks.put(guild, offset);
                }
            }
        }
        protectorBlocks.put(guild, core);
    }

    public static void remove(@NotNull Guild guild, @NotNull BlockPos core, int size) {
        if (protectorBlocks.get(guild).contains(core)) {
            Collection<BlockPos> allGuildProtected = protectedBlocks.get(guild);
            for (int x = -size; x < size; x++) {
                for (int z = -size; z < size; z++) {
                    allGuildProtected.remove(core.offset(x, 0, z));
                }
            }
            protectorBlocks.remove(guild, core);
        }
        for (BlockPos pos : protectorBlocks.get(guild)) {
            updateSize(guild, size, pos);
        }
    }
}