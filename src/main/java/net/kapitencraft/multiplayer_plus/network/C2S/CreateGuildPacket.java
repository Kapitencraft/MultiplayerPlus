package net.kapitencraft.multiplayer_plus.network.C2S;

import io.netty.buffer.ByteBuf;
import net.kapitencraft.multiplayer_plus.MultiplayerPlusMod;
import net.kapitencraft.multiplayer_plus.guild.GuildHandler;
import net.kapitencraft.multiplayer_plus.network.S2C.response.CreateGuildResponsePacket;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CreateGuildPacket(String name, boolean isPublic) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, CreateGuildPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CreateGuildPacket::name,
            ByteBufCodecs.BOOL, CreateGuildPacket::isPublic,
            CreateGuildPacket::new
    );
    public static final Type<CreateGuildPacket> TYPE = new Type<>(MultiplayerPlusMod.res("guild/create"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        Player player = context.player();
        ItemStack mainHandItem = player.getMainHandItem();
        if (!mainHandItem.is(ItemTags.BANNERS)) {
            context.reply(new CreateGuildResponsePacket(CreateGuildResponsePacket.Result.NO_BANNER));
            return;
        }
        GuildHandler handler = GuildHandler.getInstance(player.level());
        if (handler.getGuild(name) != null) {
            context.reply(new CreateGuildResponsePacket(CreateGuildResponsePacket.Result.DUPLICATE_NAME));
            return;
        }
        if (handler.isStackFromGuildBanner(mainHandItem)) {
            context.reply(new CreateGuildResponsePacket(CreateGuildResponsePacket.Result.DUPLICATE_BANNER));
            return;
        }
        handler.addNewGuild(name, isPublic, mainHandItem, player);
    }
}
