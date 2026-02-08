package net.kapitencraft.multiplayer_plus.guild;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class GuildUpgradeItem extends Item {
    private final GuildUpgrades upgrade;

    public GuildUpgradeItem(GuildUpgrades upgrade) {
        super(new Properties().rarity(Rarity.RARE));
        this.upgrade = upgrade;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        Guild guild = GuildHandler.getInstance(level).getGuildForPlayer(player);
        if (guild != null && guild.upgrade(this.upgrade)) {
            player.sendSystemMessage(Component.translatable("guild.upgrade.gain", Component.translatable("guild.upgrade.name." + this.upgrade.getName())));
            return InteractionResultHolder.success(player.getItemInHand(hand));
        } else {
            player.sendSystemMessage(Component.translatable("guild.upgrade.gain.failed").withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
    }
}
