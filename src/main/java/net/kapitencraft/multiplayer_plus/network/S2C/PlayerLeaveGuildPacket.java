package net.kapitencraft.multiplayer_plus.network.S2C;

import io.netty.buffer.ByteBuf;
import net.kapitencraft.multiplayer_plus.MultiplayerPlusMod;
import net.kapitencraft.multiplayer_plus.guild.Guild;
import net.kapitencraft.multiplayer_plus.guild.GuildHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record PlayerLeaveGuildPacket(UUID player) implements CustomPacketPayload {
    public static final Type<PlayerLeaveGuildPacket> TYPE = new Type<>(MultiplayerPlusMod.res("guild/player_leave"));
    public static final StreamCodec<ByteBuf, PlayerLeaveGuildPacket> CODEC = UUIDUtil.STREAM_CODEC.map(PlayerLeaveGuildPacket::new, PlayerLeaveGuildPacket::player);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            Player player = level.getPlayerByUUID(player());
            Guild.LOGGER.info("received data to remove {} from their guild", player);
            GuildHandler.getClientInstance().getGuildForPlayer(player).kickMember(player, Guild.KickStatus.LEAVE);
        }
    }
}
