package com.kumoe.EpicFightIntegration.config;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;

public final class EFIConfig {

    public static boolean enableDebug;
    public static boolean enableActionBar;

    public static Config config = EpicFightIntegration.getInstance().getConfig();
    public static boolean enableAutoToggleMode;
    public static int autoToggleTime;

    public static void bake() {
        // general settings
        initGeneralSettings();
    }

    private static void initGeneralSettings() {
        try {
            enableDebug = config.enableDebug.get();
            enableActionBar = config.enableActionBar.get();
            enableAutoToggleMode = config.enableAutoToggleMode.get();
            autoToggleTime = config.autoToggleTime.get();
        } catch (Exception var) {
            EpicFightIntegration.LOGGER.warn("An exception was caused trying to load the config for GeneralSettings.\n" + var);
        }
    }


}
