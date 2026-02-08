package net.kapitencraft.multiplayer_plus.guild.client.elements;

import net.kapitencraft.kap_lib.client.BannerPatternRenderer;
import net.kapitencraft.kap_lib.client.gui.screen.MenuableScreen;
import net.kapitencraft.kap_lib.client.widget.menu.Menu;
import net.kapitencraft.kap_lib.client.widget.menu.scroll.elements.ScrollElement;
import net.kapitencraft.multiplayer_plus.guild.Guild;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GuildElement extends ScrollElement {
    private final Guild guild;

    public GuildElement(Guild guild) {
        this.guild = guild;
    }

    @Override
    public void render(@NotNull GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        BannerPatternRenderer.renderBannerFromStack(pPoseStack, this.x + 1, this.y + 1, guild.getBanner(), 18);
        Component name = guild.getName();
        pPoseStack.drawCenteredString(Minecraft.getInstance().font, name, this.x + 11, this.y + 1, -1);
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public Menu createMenu(int x, int y, MenuableScreen screen) {
        return null;
    }
}
