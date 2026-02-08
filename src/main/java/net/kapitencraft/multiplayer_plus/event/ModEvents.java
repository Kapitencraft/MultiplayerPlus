package net.kapitencraft.multiplayer_plus.event;

import net.kapitencraft.multiplayer_plus.guild.GuildHandler;
import net.kapitencraft.multiplayer_plus.network.S2C.SyncGuildsPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class ModEvents {

    @SubscribeEvent
    public static void onOnDatapackSync(OnDatapackSyncEvent event) {
        event.getRelevantPlayers().forEach(player ->
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, SyncGuildsPacket.loadAll(GuildHandler.all(player.level())))
        );
    }
}
