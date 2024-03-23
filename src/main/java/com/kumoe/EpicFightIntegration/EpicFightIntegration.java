package com.kumoe.EpicFightIntegration;

import com.kumoe.EpicFightIntegration.config.Config;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.event.ServerEvents;
import com.kumoe.EpicFightIntegration.network.SkillLevelSyncPacket;
import com.kumoe.EpicFightIntegration.network.SkillRequirementSyncPacket;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import static com.kumoe.EpicFightIntegration.event.ServerEvents.CHANNEL;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EpicFightIntegration.MODID)
public class EpicFightIntegration {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "efi_mod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    private static EpicFightIntegration instance;

    final Pair<Config, ForgeConfigSpec> configured = (new ForgeConfigSpec.Builder()).configure(Config::new);

    public EpicFightIntegration() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        instance = this;
        // Register the commonSetup method for modloading
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, configured.getRight());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ServerEvents::init);
        SkillRequirements.SKILL_SETTINGS.subscribeAsSyncable(CHANNEL, SkillRequirementSyncPacket::new);
        SkillRequirements.TEMPLATES.subscribeAsSyncable(CHANNEL, SkillLevelSyncPacket::new);
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
