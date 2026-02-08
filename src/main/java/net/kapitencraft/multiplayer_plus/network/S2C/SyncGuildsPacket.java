package net.kapitencraft.multiplayer_plus.network.S2C;

import net.kapitencraft.multiplayer_plus.MultiplayerPlusMod;
import net.kapitencraft.multiplayer_plus.guild.Guild;
import net.kapitencraft.multiplayer_plus.guild.GuildHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record SyncGuildsPacket(List<Guild> data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncGuildsPacket> TYPE = new CustomPacketPayload.Type<>(MultiplayerPlusMod.res("guild/sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncGuildsPacket> CODEC = Guild.STREAM_CODEC.apply(ByteBufCodecs.list()).map(SyncGuildsPacket::new, SyncGuildsPacket::data);

    public static SyncGuildsPacket loadAll(Collection<Guild> guilds) {
        return new SyncGuildsPacket(new ArrayList<>(guilds));
    }

    public void handle(IPayloadContext sup) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && Minecraft.getInstance().getCameraEntity() != null) {
            Guild.LOGGER.info("received data for {} guilds", data.size());
            data.forEach(GuildHandler::addGuildClient);
            level.players().forEach(player -> GuildHandler.all(level).forEach(guild -> guild.setOnline(player)));
            Guild.LOGGER.info("loaded {} Guilds", GuildHandler.all(level).size());
        }
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
