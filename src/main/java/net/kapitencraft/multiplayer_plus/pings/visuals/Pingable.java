package net.kapitencraft.multiplayer_plus.pings.visuals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface Pingable {
    boolean pinges(Player player);

    String display();

    static boolean isPinged(String name) {
        LocalPlayer player = Minecraft.getInstance().player;
        return allPingables.stream().filter(pingable -> Objects.equals(name, pingable.display())).anyMatch(pingable -> pingable.pinges(player));
    }

    List<Pingable> allPingables = new ArrayList<>();
}
