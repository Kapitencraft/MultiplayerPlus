package net.kapitencraft.multiplayer_plus.network.S2C;

import com.mojang.datafixers.kinds.IdF;
import io.netty.buffer.ByteBuf;
import net.kapitencraft.multiplayer_plus.MultiplayerPlusMod;
import net.kapitencraft.multiplayer_plus.guild.Guild;
import net.kapitencraft.multiplayer_plus.guild.GuildHandler;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DisbandGuildPacket(String guildName) implements CustomPacketPayload {
    public static final Type<DisbandGuildPacket> TYPE = new Type<>(MultiplayerPlusMod.res("guild/disband"));
    public static final StreamCodec<ByteBuf, DisbandGuildPacket> CODEC = ByteBufCodecs.STRING_UTF8.map(DisbandGuildPacket::new, DisbandGuildPacket::guildName);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        Guild.LOGGER.info("received data to remove '{}'", guildName);
        GuildHandler.getClientInstance().removeGuild(guildName);
    }
}
