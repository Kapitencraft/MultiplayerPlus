package net.kapitencraft.multiplayer_plus.guild.client;

import net.kapitencraft.kap_lib.client.gui.screen.DefaultBackgroundScreen;
import net.kapitencraft.kap_lib.client.widget.Checkbox;
import net.kapitencraft.kap_lib.client.widget.select.CreateBannerWidget;
import net.kapitencraft.multiplayer_plus.guild.GuildHandler;
import net.kapitencraft.multiplayer_plus.network.C2S.CreateGuildPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class CreateGuildScreen extends DefaultBackgroundScreen {
    private static final Component JOIN_GUILD_MSG = Component.translatable("gui.join_guild");
    private static final Component TITLE_MSG = Component.translatable("gui.create_guild");
    private static final Component GUILD_PUBLIC_MSG = Component.translatable("gui.guild_public").withStyle(Style.EMPTY
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("gui.guild_public.desc")))
    );

    public CreateGuildScreen() {
        super(TITLE_MSG);
    }

    private CreateBannerWidget widget;
    private EditBox nameBox;
    private Checkbox shouldBePublic;

    @Override
    protected void init() {
        super.init();
        this.children().clear();
        int halfWidth = this.leftPos + this.getImageWidth() / 2;
        this.updateJoinButton(halfWidth);
        this.updatePublicSetting(halfWidth);
        this.updateNameBox();
        this.updateCreateButton(halfWidth);
        this.updateBannerEditor();
    }

    @SuppressWarnings("all")
    private void updateJoinButton(int halfWidth) {
        int width = this.font.width(JOIN_GUILD_MSG);
        int xStart = halfWidth - width / 2;
        Button joinGuildButton = new PlainTextButton(xStart, this.topPos + this.getImageHeight() - 12, width, 10, JOIN_GUILD_MSG, pButton ->
                this.minecraft.setScreen(new SelectGuildScreen(this)),
                this.font
        );
        this.addRenderableWidget(joinGuildButton);
    }

    private void updatePublicSetting(int halfWidth) {
        int publicBoxX = halfWidth - (12 + this.font.width(GUILD_PUBLIC_MSG)) / 2;
        boolean wasChecked = shouldBePublic != null && shouldBePublic.isChecked();
        shouldBePublic = new Checkbox(publicBoxX, this.topPos + this.getImageHeight() - 32, GUILD_PUBLIC_MSG, true, font, wasChecked);
        this.addRenderableWidget(shouldBePublic);
    }

    private void updateNameBox() {
        this.nameBox = new EditBox(this.font, this.leftPos + 5, this.topPos + 15, this.getImageWidth() - 10, 10, this.nameBox, Component.empty());
        this.nameBox.setFilter(s -> GuildHandler.getClientInstance().getGuild(s) == null);
        nameBox.setHint(Component.translatable("gui.set_guild_name"));
        this.addRenderableWidget(nameBox);
    }

    private void updateBannerEditor() {
        this.widget = new CreateBannerWidget(this.leftPos + 5, this.topPos + 26, this.getImageWidth() - 10, this.getImageHeight() - 61);
        this.addRenderableWidget(this.widget);
    }

    @SuppressWarnings("all")
    private void updateCreateButton(int halfWidth) {
        int width = this.font.width(this.title);
        int xStart = halfWidth - width / 2;
        Button createGuildButton = new PlainTextButton(xStart, this.topPos + this.getImageHeight() - 22, this.width, 10, this.title, pButton -> {
            if (this.nameBox.getValue().isEmpty())
                Minecraft.getInstance().player.sendSystemMessage(Component.translatable("gui.guild.missing_name").withStyle(ChatFormatting.RED));
            else {
                PacketDistributor.sendToServer(new CreateGuildPacket(this.nameBox.getValue(), this.shouldBePublic.isChecked()));
            }
        }, this.font
        );
        this.addRenderableWidget(createGuildButton);
    }

    @Override
    public void render(@NotNull GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        pPoseStack.drawCenteredString(this.font, this.title, this.leftPos + this.getImageWidth() / 2, this.topPos + 5, -1);
    }
}