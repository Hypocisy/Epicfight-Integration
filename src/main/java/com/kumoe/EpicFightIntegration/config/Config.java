package com.kumoe.EpicFightIntegration.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public ForgeConfigSpec.BooleanValue enableAutoToggleMode;
    public ForgeConfigSpec.BooleanValue require_combined_weapon_level;
    protected ForgeConfigSpec.IntValue autoToggleTime;
    protected ForgeConfigSpec.BooleanValue enableDebug;
    protected ForgeConfigSpec.BooleanValue enableActionBar;


    public Config(ForgeConfigSpec.Builder builder) {
        builder.push("General settings");
        {
            enableDebug = builder.comment("Show debug info to player?").define("enableDebug", true);
            enableActionBar = builder.comment("Show Action bar info to player?").define("enableActionBar", true);
	        require_combined_weapon_level = builder
			        .comment("""
				                 If true, players must meet both PMMO weapon level requirements
				                 and custom skill level requirements to use or attack with weapons.
				                 If false, only PMMO requirements are applied.""")
			        .translation("config.yourmod.requireCombinedWeaponLevel")
			        .define("requireCombinedWeaponLevel", false);
            enableAutoToggleMode = builder.comment("Enable auto toggle player mode?").define("enableAutoToggleMode", true);
            autoToggleTime = builder.comment("How much time do we auto toggle player mode?").defineInRange("autoToggleTime", 20,20,1000);
        }
        builder.pop();
    }
}

