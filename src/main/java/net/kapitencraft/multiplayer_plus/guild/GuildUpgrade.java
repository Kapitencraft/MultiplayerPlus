package net.kapitencraft.multiplayer_plus.guild;

import net.minecraft.world.item.ItemStack;

public interface GuildUpgrade {

    String getName();

    String makeRegistryName();

    UpgradeRarity getRarity();

    int getMaxLevel();

    ItemStack mainCostItem();

    int defaultCost();

    enum UpgradeRarity {
        COMMON("common", 1),
        UNCOMMON("uncommon", 2),
        RARE("rare", 3),
        EPIC("epic", 4),
        LEGENDARY("legendary", 5);

        private final String name;
        private final int professionLevel;

        UpgradeRarity(String name, int profession_level) {
            this.name = name;
            this.professionLevel = profession_level;
        }

        public String getName() {
            return name;
        }

        public int getProfessionLevel() {
            return professionLevel;
        }
    }
}
