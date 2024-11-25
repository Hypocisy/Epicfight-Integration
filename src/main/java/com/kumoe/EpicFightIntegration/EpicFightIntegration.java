package com.kumoe.EpicFightIntegration;

import com.kumoe.EpicFightIntegration.config.Config;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

@Mod(EpicFightIntegration.MODID)
public class EpicFightIntegration {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "efi_mod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    private static EpicFightIntegration instance;

    final Pair<Config, ForgeConfigSpec> configured = (new ForgeConfigSpec.Builder()).configure(Config::new);
    public EpicFightIntegration() {
        instance = this;
        // Register the commonSetup method for modloading
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, configured.getRight());
    }

    public static EpicFightIntegration getInstance() {
        return instance;
    }

    public Config getConfig() {
        return this.configured.getLeft();
    }

    public ForgeConfigSpec getConfigSpec() {
        return configured.getRight();
    }
}
