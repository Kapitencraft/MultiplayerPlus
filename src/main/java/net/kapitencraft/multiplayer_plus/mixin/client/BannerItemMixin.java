package net.kapitencraft.multiplayer_plus.mixin.client;

import net.kapitencraft.multiplayer_plus.guild.Guild;
import net.kapitencraft.multiplayer_plus.guild.GuildHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(BannerItem.class)
public abstract class BannerItemMixin extends StandingAndWallBlockItem {

    public BannerItemMixin(Block p_248873_, Block p_251044_, Properties p_249308_, Direction p_250800_) {
        super(p_248873_, p_251044_, p_249308_, p_250800_);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        Guild guild = GuildHandler.getClientInstance().getGuildForBanner(stack);
        return guild != null ? Component.translatable("guild.banner.name", guild.getGuildName()).withStyle(ChatFormatting.GREEN) : super.getName(stack);
    }
}