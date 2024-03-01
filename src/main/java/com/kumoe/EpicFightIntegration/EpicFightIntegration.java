package com.kumoe.EpicFightIntegration;

import com.kumoe.EpicFightIntegration.config.EFIConfig;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.network.SkillRequirementSyncPacket;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EpicFightIntegration.MODID)
public class EpicFightIntegration {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "efi_mod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final String CHANNEL_PROTOCOL = "0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> CHANNEL_PROTOCOL,
            CHANNEL_PROTOCOL::equals,
            CHANNEL_PROTOCOL::equals);

    public EpicFightIntegration() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EFIConfig.clientSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, EFIConfig.serverSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EFIConfig.commonSpec);
        modEventBus.register(EFIConfig.class);
        this.registerPackets();
        SkillRequirements.DATA_LOADER.subscribeAsSyncable(CHANNEL, SkillRequirementSyncPacket::new);
    }

    void registerPackets() {
        int id = 0;
        CHANNEL.registerMessage(id++, SkillRequirementSyncPacket.class,
                SkillRequirementSyncPacket::encode,
                SkillRequirementSyncPacket::decode,
                SkillRequirementSyncPacket::onPacketReceived);
    }
}
