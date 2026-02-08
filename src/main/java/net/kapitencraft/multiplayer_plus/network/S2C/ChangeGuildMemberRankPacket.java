package net.kapitencraft.multiplayer_plus.network.S2C;

import net.kapitencraft.multiplayer_plus.MultiplayerPlusMod;
import net.kapitencraft.multiplayer_plus.guild.Guild;
import net.kapitencraft.multiplayer_plus.guild.GuildHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ChangeGuildMemberRankPacket(UUID uuid, String rankName) implements CustomPacketPayload {
    public static final Type<ChangeGuildMemberRankPacket> TYPE = new Type<>(MultiplayerPlusMod.res("guild/change_member_rank"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            Player player = level.getPlayerByUUID(uuid);
            Guild.LOGGER.info("received data to change rank of {} to {}", player.getName(), rankName);
            GuildHandler.getClientInstance().getGuildForPlayer(player).setRank(player, rankName);
        }
    }
}
