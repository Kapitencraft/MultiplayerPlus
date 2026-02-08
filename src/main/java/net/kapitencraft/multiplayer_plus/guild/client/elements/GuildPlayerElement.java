package net.kapitencraft.multiplayer_plus.guild.client.elements;

import net.kapitencraft.kap_lib.client.LibClient;
import net.kapitencraft.kap_lib.client.gui.screen.MenuableScreen;
import net.kapitencraft.kap_lib.client.widget.menu.Menu;
import net.kapitencraft.kap_lib.client.widget.menu.drop_down.DropDownMenu;
import net.kapitencraft.kap_lib.client.widget.menu.drop_down.elements.ButtonElement;
import net.kapitencraft.kap_lib.client.widget.menu.drop_down.elements.TimeSelectorElement;
import net.kapitencraft.kap_lib.client.widget.menu.scroll.elements.ScrollElement;
import net.kapitencraft.multiplayer_plus.guild.Guild;
import net.kapitencraft.multiplayer_plus.guild.GuildHandler;
import net.kapitencraft.multiplayer_plus.registry.ModDataRequesters;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GuildPlayerElement extends ScrollElement {
    private final ResourceLocation skin;
    private final String name;
    private final UUID playerUUID;

    public GuildPlayerElement(PlayerInfo info) {
        this.name = info.getProfile().getName();
        this.skin = info.getSkin().texture();
        this.playerUUID = info.getProfile().getId();
    }


    @Override
    public int getWidth() {
        return 12 + Minecraft.getInstance().font.width(this.name);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        PlayerFaceRenderer.draw(pGuiGraphics, this.skin, this.x + 1, this.y + 1, 8);
        pGuiGraphics.drawString(Minecraft.getInstance().font, this.name, this.x + 10, this.y + 1, -1);
    }

    @Override
    public Menu createMenu(int x, int y, MenuableScreen screen) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.getUUID() == playerUUID) return null;
        Guild.IRank rank = GuildHandler.getClientInstance().getGuildForPlayer(player)
                .getRank(player);
        DropDownMenu menu = new DropDownMenu(x, y, this);
        if (rank.getPermissionLevel() >= 2) menu.addElement(TimeSelectorElement.builder()
                .setName(Component.translatable("gui.guild.set_muted")));
                //.setOnTimeSet(aLong ->
                //        handler.createRequest(ModDataRequesters.MUTE_MEMBER.get(), new Pair<>(this.playerUUID, aLong), simpleSuccessResult ->
                //                sendMessage(player, "mute_member", simpleSuccessResult)
                //        )
                //);
        if (rank.getPermissionLevel() >= 3) menu.addElement(ButtonElement.builder()
                .setName(Component.translatable("gui.guild.kick_member"))
                .setExecutor(() ->
                                PacketDistributor.sendToServer(new )
                        handler.createRequest(ModDataRequesters.KICK_MEMBER.get(), this.playerUUID, simpleSuccessResult ->
                                sendMessage(player, "kick_member", simpleSuccessResult)
                        )
                )
        );
        if (rank.getPermissionLevel() >= 4) menu.addElement(TimeSelectorElement.builder()
                .setName(Component.translatable("gui.guild.ban_member"))
                //.setOnTimeSet(aLong ->
                //        handler.createRequest(ModDataRequesters.BAN_PLAYER.get(), new Pair<>(this.playerUUID, aLong), simpleSuccessResult ->
                //                sendMessage(player, "ban_player", simpleSuccessResult)
                //))
        );

        return menu;
    }

    private void sendMessage(Player player, String id, SimpleSuccessResult simpleSuccessResult) {
        player.sendSystemMessage(Component.translatable("gui.guild." + id + "." + simpleSuccessResult.id()).withStyle(simpleSuccessResult.success() ? ChatFormatting.GREEN : ChatFormatting.RED));
    }
}
