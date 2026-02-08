package net.kapitencraft.multiplayer_plus.network;

import net.kapitencraft.multiplayer_plus.network.C2S.CreateGuildPacket;
import net.kapitencraft.multiplayer_plus.network.C2S.RequestBanMemberPacket;
import net.kapitencraft.multiplayer_plus.network.C2S.RequestKickMemberPacket;
import net.kapitencraft.multiplayer_plus.network.C2S.RequestMuteMemberPacket;
import net.kapitencraft.multiplayer_plus.network.S2C.*;
import net.kapitencraft.multiplayer_plus.network.S2C.response.CreateGuildResponsePacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class ModMessages {
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToClient(AddGuildPacket.TYPE, AddGuildPacket.CODEC, AddGuildPacket::handle);
        registrar.playToClient(PlayerJoinGuildPacket.TYPE, PlayerJoinGuildPacket.CODEC, PlayerJoinGuildPacket::handle);
        registrar.playToClient(DisbandGuildPacket.TYPE, DisbandGuildPacket.CODEC, DisbandGuildPacket::handle);
        registrar.playToClient(PlayerLeaveGuildPacket.TYPE, PlayerLeaveGuildPacket.CODEC, PlayerLeaveGuildPacket::handle);
        registrar.playToClient(SyncGuildsPacket.TYPE, SyncGuildsPacket.CODEC, SyncGuildsPacket::handle);
        registrar.playToClient(CreateGuildResponsePacket.TYPE, CreateGuildResponsePacket.CODEC, CreateGuildResponsePacket::handle);

        registrar.playToServer(CreateGuildPacket.TYPE, CreateGuildPacket.STREAM_CODEC, CreateGuildPacket::handle);
        registrar.playToServer(RequestBanMemberPacket.TYPE, RequestBanMemberPacket.STREAM_CODEC, RequestBanMemberPacket::handle);
        registrar.playToServer(RequestMuteMemberPacket.TYPE, RequestMuteMemberPacket.STREAM_CODEC, RequestMuteMemberPacket::handle);
        registrar.playToServer(RequestKickMemberPacket.TYPE, RequestKickMemberPacket.STREAM_CODEC, RequestKickMemberPacket::handle);
    }
}