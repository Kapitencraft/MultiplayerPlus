package net.kapitencraft.multiplayer_plus.network.C2S;

import io.netty.buffer.ByteBuf;
import net.kapitencraft.multiplayer_plus.MultiplayerPlusMod;
import net.kapitencraft.multiplayer_plus.guild.Guild;
import net.kapitencraft.multiplayer_plus.guild.GuildHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record RequestBanMemberPacket(UUID id, long duration) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, RequestBanMemberPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RequestBanMemberPacket::id,
            ByteBufCodecs.VAR_LONG, RequestBanMemberPacket::duration,
            RequestBanMemberPacket::new
    );
    public static final Type<RequestBanMemberPacket> TYPE = new Type<>(MultiplayerPlusMod.res("guild/request/ban"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        Player player = context.player();
        Level level = player.level();
        GuildHandler handler = GuildHandler.getInstance(level);
        Guild guild = handler.getGuildForPlayer(player);
        if (guild == null) {
            Guild.LOGGER.warn("Player \"{}\" illegally attempted to access Guild Data!", player.getScoreboardName());
            context.disconnect(Component.translatable("command.guild.fail.noGuild"));
            return;
        }
        if (!guild.canDoAction(player, 4)) {
            Guild.LOGGER.warn("Player \"{}\" illegally attempted to perform action beyond their permission level", player.getScoreboardName());
            context.disconnect(Component.translatable("command.guild.fail.unauthorized"));
        }
        Player target = level.getPlayerByUUID(id);
        if (target != null) {
            guild.kickMember(target, Guild.KickReason.BAN);
        } else {
            guild.removeOfflineMember(id, Guild.KickReason.BAN);
        }
        guild.banPlayer(id, duration);
        player.sendSystemMessage(Component.translatable("command.guild.ban.success", target).withStyle(ChatFormatting.GREEN));

    }
}
