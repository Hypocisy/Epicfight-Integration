package com.kumoe.EpicFightIntegration;

import com.kumoe.EpicFightIntegration.config.EFIConfig;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.event.ServerEvents;
import com.kumoe.EpicFightIntegration.network.SkillLevelSyncPacket;
import com.kumoe.EpicFightIntegration.network.SkillRequirementSyncPacket;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import static com.kumoe.EpicFightIntegration.event.ServerEvents.CHANNEL;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EpicFightIntegration.MODID)
public class EpicFightIntegration {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "efi_mod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();


    public EpicFightIntegration() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EFIConfig.clientSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, EFIConfig.serverSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EFIConfig.commonSpec);
        modEventBus.register(EFIConfig.class);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ServerEvents::init);
        SkillRequirements.SKILL_SETTINGS.subscribeAsSyncable(CHANNEL, SkillRequirementSyncPacket::new);
        SkillRequirements.TEMPLATES.subscribeAsSyncable(CHANNEL, SkillLevelSyncPacket::new);
    }
}
