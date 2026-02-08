package net.kapitencraft.multiplayer_plus.network.S2C;

import io.netty.buffer.ByteBuf;
import net.kapitencraft.multiplayer_plus.MultiplayerPlusMod;
import net.kapitencraft.multiplayer_plus.guild.Guild;
import net.kapitencraft.multiplayer_plus.guild.GuildHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record PlayerJoinGuildPacket(UUID player, String guildName) implements CustomPacketPayload {
    public static final Type<PlayerJoinGuildPacket> TYPE = new Type<>(MultiplayerPlusMod.res("guild/add_player"));
    public static final StreamCodec<ByteBuf, PlayerJoinGuildPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PlayerJoinGuildPacket::player,
            ByteBufCodecs.STRING_UTF8, PlayerJoinGuildPacket::guildName,
            PlayerJoinGuildPacket::new
    );

    @Override
    public @NotNull Type<PlayerJoinGuildPacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        ClientLevel level = Minecraft.getInstance().level;
        Player player = level.getPlayerByUUID(player());
        Guild.LOGGER.info("received data for adding {} to '{}'", player.getName(), guildName);
        GuildHandler.getClientInstance().getGuild(guildName).addMember(player);
    }
}
