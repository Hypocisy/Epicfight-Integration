package com.kumoe.EpicFightIntegration.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
	public ModConfigSpec.BooleanValue enableAutoToggleMode;
	protected ModConfigSpec.IntValue autoToggleTime;
	protected ModConfigSpec.BooleanValue enableDebug;
	protected ModConfigSpec.BooleanValue enableActionBar;


	public Config(ModConfigSpec.Builder builder) {
		builder.push("General settings");
		{
			enableDebug = builder.comment("Show debug info to player?").define("enableDebug", true);
			enableActionBar = builder.comment("Show Action bar info to player?").define("enableActionBar", true);
			enableAutoToggleMode = builder.comment("Enable auto toggle player mode?").define("enableAutoToggleMode", true);
			autoToggleTime = builder.comment("How much time do we auto toggle player mode?").defineInRange("autoToggleTime", 20, 20, 1000);
		}
		builder.pop();
	}
}

