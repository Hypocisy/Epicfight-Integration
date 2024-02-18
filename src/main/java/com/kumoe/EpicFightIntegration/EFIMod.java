package com.kumoe.EpicFightIntegration;

import com.kumoe.EpicFightIntegration.config.EFIConfig;
import com.mojang.logging.LogUtils;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import org.slf4j.Logger;
import yesman.epicfight.world.item.SkillBookItem;

import java.util.HashMap;
import java.util.Map;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EFIMod.MODID)
public class EFIMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "efi_mod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public EFIMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
//        modEventBus.addListener(this::onServerStart);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EFIConfig.clientSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, EFIConfig.serverSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EFIConfig.commonSpec);
        modEventBus.register(EFIConfig.class);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStart(ServerStartingEvent event) {
        ResourceLocation resourceLocation = new ResourceLocation("EFI_SKILL_REQ");
        Map<String, Integer> testReqs = new HashMap<>();
        APIUtils.registerActionPredicate(resourceLocation, ReqType.INTERACT, (player, itemStack) -> {
            if (!itemStack.isEmpty() && player.getMainHandItem().getItem() instanceof SkillBookItem skillBookItem) {
                return skillBookItem.isFoil(player.getMainHandItem());
            }
            return false;
        });
        APIUtils.registerRequirement(ObjectType.ITEM, resourceLocation, ReqType.RIDE, testReqs, false);
//        boolean predicateExists = Core.get(LogicalSide.SERVER).getPredicateRegistry().predicateExists();
//        LOGGER.debug("is predicate exists {}", predicateExists);
        LOGGER.debug("predicate resource location {} {}", resourceLocation.getNamespace(), resourceLocation.getPath());
    }
}
