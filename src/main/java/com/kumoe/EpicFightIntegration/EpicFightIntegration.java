package com.kumoe.EpicFightIntegration;

import com.kumoe.EpicFightIntegration.config.Config;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

@Mod(EpicFightIntegration.MODID)
public class EpicFightIntegration {
	// Define mod id in a common place for everything to reference
	public static final String MODID = "efi_mod";
	// Directly reference a slf4j logger
	public static final Logger LOGGER = LogUtils.getLogger();
	private static EpicFightIntegration instance;

	final Pair<Config, ModConfigSpec> configured = (new ModConfigSpec.Builder()).configure(Config::new);

	public EpicFightIntegration(IEventBus modEventBus, ModContainer modContainer) {
		instance = this;
		// Register the commonSetup method for modloading
		modContainer.registerConfig(ModConfig.Type.COMMON, configured.getRight());
	}

	public static EpicFightIntegration getInstance() {
		return instance;
	}

	public Config getConfig() {
		return this.configured.getLeft();
	}

	public ModConfigSpec getConfigSpec() {
		return configured.getRight();
	}
}
