package net.kapitencraft.multiplayer_plus.network.S2C;

import net.kapitencraft.multiplayer_plus.MultiplayerPlusMod;
import net.kapitencraft.multiplayer_plus.guild.Guild;
import net.kapitencraft.multiplayer_plus.guild.GuildHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record AddGuildPacket(UUID owner, String name, boolean isPublic, ItemStack banner) implements CustomPacketPayload {
    public static final Type<AddGuildPacket> TYPE = new Type<>(MultiplayerPlusMod.res("guild/add"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddGuildPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AddGuildPacket::owner,
            ByteBufCodecs.STRING_UTF8, AddGuildPacket::name,
            ByteBufCodecs.BOOL, AddGuildPacket::isPublic,
            ItemStack.STREAM_CODEC, AddGuildPacket::banner,
            AddGuildPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            GuildHandler handler = GuildHandler.getClientInstance();
            Player player = level.getPlayerByUUID(owner);
            Guild.LOGGER.info("received data for adding new guild '{}' from {}", name, player.getName());
            handler.addGuild(name, player, banner, isPublic);
        }
    }
}
