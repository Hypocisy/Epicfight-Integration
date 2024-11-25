package com.kumoe.EpicFightIntegration.event;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.config.EFIConfig;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static com.kumoe.EpicFightIntegration.event.ForgeEvents.registerPackets;

@Mod.EventBusSubscriber(modid = EpicFightIntegration.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void onModConfigLoad(ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getSpec() == EpicFightIntegration.getInstance().getConfigSpec()) {
            EpicFightIntegration.LOGGER.debug("Loading EpicFightIntegration config");
            EFIConfig.bake();
        }
    }
    @SubscribeEvent
    public static void onFMLCommonSetup(final FMLCommonSetupEvent event) {
        registerPackets();
    }
}
