package net.kapitencraft.multiplayer_plus.guild;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.kapitencraft.kap_lib.helpers.ExtraStreamCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

public class GuildUpgradeContainer {
    public static final StreamCodec<ByteBuf, GuildUpgradeContainer> STREAM_CODEC = ByteBufCodecs.INT.apply(ExtraStreamCodecs.map(GuildUpgrades.STREAM_CODEC)).map(GuildUpgradeContainer::new, GuildUpgradeContainer::getUpgrades);
    public static final Codec<GuildUpgradeContainer> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(GuildUpgrades.CODEC, Codec.INT).fieldOf("Content").forGetter(GuildUpgradeContainer::getUpgrades)
    ).apply(i, GuildUpgradeContainer::new));

    private final Map<GuildUpgrades, Integer> upgrades = new HashMap<>();

    private GuildUpgradeContainer(Map<GuildUpgrades, Integer> map) {
        upgrades.putAll(map);
    }

    GuildUpgradeContainer() {}

    public boolean upgrade(GuildUpgrades toUpgrade) {
        int value = upgrades.get(toUpgrade);
        if (value < toUpgrade.getMaxLevel()) {
            upgrades.remove(toUpgrade);
            upgrades.put(toUpgrade, value + 1);
            return true;
        }
        return false;
    }

    public Map<GuildUpgrades, Integer> getUpgrades() {
        return upgrades;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        upgrades.keySet().forEach(guildUpgrade -> tag.putInt(guildUpgrade.getName(), upgrades.get(guildUpgrade)));
        return tag;
    }

    public int getUpgradeLevel(GuildUpgrades upgrade) {
        return upgrades.get(upgrade);
    }

    public static GuildUpgradeContainer load(CompoundTag tag) {
        GuildUpgradeContainer instance = new GuildUpgradeContainer();
        for (GuildUpgrades upgrade : GuildUpgrades.values()) {
            if (tag.contains(upgrade.getName(), 3)) {
                instance.upgrades.put(upgrade, tag.getInt(upgrade.getName()));
            }
        }
        return instance;
    }
}
