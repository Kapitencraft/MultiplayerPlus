package net.kapitencraft.multiplayer_plus;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MultiplayerPlusMod.MOD_ID)
public class MultiplayerPlusMod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "name_id";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static <T> DeferredRegister<T> registry(ResourceKey<Registry<T>> reg) {
        return DeferredRegister.create(reg, MOD_ID);
    }

    public MultiplayerPlusMod(IEventBus bus, ModContainer container) {

        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static ResourceLocation res(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
