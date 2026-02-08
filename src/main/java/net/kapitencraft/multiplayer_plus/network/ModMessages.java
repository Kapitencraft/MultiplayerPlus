package net.kapitencraft.multiplayer_plus.network;

import net.kapitencraft.multiplayer_plus.network.S2C.*;
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
    }
}