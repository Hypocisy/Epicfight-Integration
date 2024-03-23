package com.kumoe.EpicFightIntegration.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    protected ForgeConfigSpec.BooleanValue enableDebug;
    protected ForgeConfigSpec.BooleanValue enableActionBar;
    public Config(ForgeConfigSpec.Builder builder) {
        builder.push("General settings");
        {
            enableDebug = builder.comment("Show debug info to player?").define("enableDebug", true);
            enableActionBar = builder.comment("Show Action bar info to player?").define("enableActionBar", true);
        }
        builder.pop();
    }
}

