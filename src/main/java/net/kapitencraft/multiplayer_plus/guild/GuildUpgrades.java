package net.kapitencraft.multiplayer_plus.guild;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.kapitencraft.kap_lib.helpers.ExtraStreamCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public enum GuildUpgrades implements GuildUpgrade, StringRepresentable {
    RANGE("range", UpgradeRarity.COMMON, 3, new ItemStack(Items.GUNPOWDER), 14),
    TELEPORT_SAFETY("teleport_safety", UpgradeRarity.EPIC, 1, new ItemStack(Items.ENDER_EYE).copyWithCount(10), 25);

    public static final StreamCodec<ByteBuf, GuildUpgrades> STREAM_CODEC = ExtraStreamCodecs.enumCodec(GuildUpgrades.values());
    public static final Codec<GuildUpgrades> CODEC = StringRepresentable.fromEnum(GuildUpgrades::values);

    private final String name;
    private final UpgradeRarity rarity;

    private final int maxLevel;

    private final ItemStack defaultItem;

    private final int emeraldCost;

    GuildUpgrades(String name, UpgradeRarity rarity, int maxLevel, ItemStack defaultItem, int emeraldCost) {
        this.name = name;
        this.rarity = rarity;
        this.maxLevel = maxLevel;
        this.defaultItem = defaultItem;
        this.emeraldCost = emeraldCost;
    }


    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putString("rarity", rarity.getName());
        return tag;
    }

    //public static HashMap<GuildUpgrades, DeferredItem<GuildUpgradeItem>> createRegistry() {
    //    return ModItems.createRegistry(GuildUpgradeItem::new, GuildUpgrade::makeRegistryName, List.of(values()), GUILD_GROUP);
    //}

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String makeRegistryName() {
        return "guild_upgrade." + name;
    }

    @Override
    public UpgradeRarity getRarity() {
        return rarity;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public ItemStack mainCostItem() {
        return null;
    }

    @Override
    public int defaultCost() {
        return 0;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
