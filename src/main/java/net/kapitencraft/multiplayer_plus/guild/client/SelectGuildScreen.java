package net.kapitencraft.multiplayer_plus.guild.client;

import net.kapitencraft.kap_lib.client.gui.screen.DefaultBackgroundScreen;
import net.kapitencraft.kap_lib.client.widget.menu.scroll.ScrollableMenu;
import net.kapitencraft.multiplayer_plus.guild.Guild;
import net.kapitencraft.multiplayer_plus.guild.GuildHandler;
import net.kapitencraft.multiplayer_plus.guild.client.elements.GuildElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;


//TODO add search bar
public class SelectGuildScreen extends DefaultBackgroundScreen {
    private final CreateGuildScreen source;
    protected SelectGuildScreen(CreateGuildScreen source) {
        super(Component.translatable("gui.select_guild"));
        this.source = source;
    }

    private EditBox searchField;

    @Override
    protected void init() {
        super.init();
        ScrollableMenu menu = new ScrollableMenu(this.leftPos + 5, this.topPos + 25, this, 10, this.getImageWidth() - 10);
        List<Guild> guilds = fetchGuilds();
        List<String> guildNames = guilds.stream().map(Guild::display).toList();
        guilds.stream().map(GuildElement::new).forEach(menu::addScrollable);
        this.addRenderableWidget(menu);
        this.searchField = new EditBox(this.font, this.leftPos + 30, this.topPos + 15, this.getImageWidth() - 60, 10, Component.empty());
        this.searchField.setFilter(guildNames::contains);
        this.searchField.setHint(Component.translatable("gui.search"));
        this.addRenderableWidget(searchField);
    }

    private static List<Guild> fetchGuilds() {
        GuildHandler handler = GuildHandler.getClientInstance();
        return handler.allGuilds().stream().filter(Guild::isPublic).sorted(NAME_SORTER).toList();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        graphics.drawCenteredString(this.font, this.title, this.leftPos + this.getImageWidth() / 2, this.topPos + 5, -1);
    }

    private static final Comparator<Guild> NAME_SORTER = Comparator.comparing(Guild::getGuildName);
}
