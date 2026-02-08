package net.kapitencraft.multiplayer_plus.network.S2C;

import io.netty.buffer.ByteBuf;
import net.kapitencraft.kap_lib.helpers.ExtraStreamCodecs;
import net.kapitencraft.multiplayer_plus.MultiplayerPlusMod;
import net.kapitencraft.multiplayer_plus.guild.GuildHandler;
import net.kapitencraft.multiplayer_plus.guild.client.GuildScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record CreateGuildResponsePacket(Result result) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, CreateGuildResponsePacket> CODEC = Result.CODEC.map(CreateGuildResponsePacket::new, CreateGuildResponsePacket::result);
    public static final Type<CreateGuildResponsePacket> TYPE = new Type<>(MultiplayerPlusMod.res("guild/create_response"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Result {
        NO_BANNER(Component.translatable("guild.create.no_banner")),
        DUPLICATE_BANNER(Component.translatable("guild.create.duplicateBanner")),
        DUPLICATE_NAME(Component.translatable("guild.create.duplicateName")),
        SUCCESS(Component.translatable("guild.create.success"));

        private final Component message;
        private static final StreamCodec<ByteBuf, Result> CODEC = ExtraStreamCodecs.enumCodec(Result.values());

        Result(Component message) {
            this.message = message;
        }
    }

    public void handle(IPayloadContext context) {
        Player player = context.player();
        player.displayClientMessage(result.message, true);
        if (result == Result.SUCCESS) {
            Minecraft.getInstance().setScreen(new GuildScreen(GuildHandler.getClientInstance().getGuildForPlayer(player)));
        }
    }
}
