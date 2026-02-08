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

public record RequestMuteMemberPacket(UUID id, long duration) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, RequestMuteMemberPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RequestMuteMemberPacket::id,
            ByteBufCodecs.VAR_LONG, RequestMuteMemberPacket::duration,
            RequestMuteMemberPacket::new
    );
    public static final Type<RequestMuteMemberPacket> TYPE = new Type<>(MultiplayerPlusMod.res("guild/request/mute"));

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
        guild.muteMember(id, duration);
        player.sendSystemMessage(Component.translatable("command.guild.mute.success", id).withStyle(ChatFormatting.GREEN));
    }
}
