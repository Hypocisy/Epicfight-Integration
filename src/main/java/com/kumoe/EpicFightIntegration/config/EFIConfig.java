package com.kumoe.EpicFightIntegration.config;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import net.minecraftforge.fml.config.ModConfig;

public final class EFIConfig {

    public static boolean enableDebug;
    public static boolean enableActionBar;

    public static Config config = EpicFightIntegration.getInstance().getConfig();

    public static void bake(ModConfig config) {
        // general settings
        initGeneralSettings();
    }

    private static void initGeneralSettings() {
        try {
            enableDebug = config.enableDebug.get();
            enableActionBar = config.enableActionBar.get();
        } catch (Exception var) {
            EpicFightIntegration.LOGGER.warn("An exception was caused trying to load the config for GeneralSettings.\n" + var);
        }
    }


}
