package com.kumoe.EpicFightIntegration.event;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.commands.CmdPmmoSkillBooksRoot;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.network.SkillLevelSyncPacket;
import com.kumoe.EpicFightIntegration.network.SkillRequirementSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid = EpicFightIntegration.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {
    private static final String CHANNEL_PROTOCOL = "0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(EpicFightIntegration.MODID, "main"),
            () -> CHANNEL_PROTOCOL,
            CHANNEL_PROTOCOL::equals,
            CHANNEL_PROTOCOL::equals);

    @SubscribeEvent
    public static void onCommandRegister(final RegisterCommandsEvent event) {
        CmdPmmoSkillBooksRoot.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onTagLoad(TagsUpdatedEvent event) {
        SkillRequirements.SKILL_SETTINGS.postProcess(event.getRegistryAccess());
        SkillRequirements.TEMPLATES.postProcess(event.getRegistryAccess());
    }

    public static void registerPackets() {
        int id = 0;
        CHANNEL.registerMessage(++id, SkillRequirementSyncPacket.class,
                SkillRequirementSyncPacket::encode,
                SkillRequirementSyncPacket::decode,
                SkillRequirementSyncPacket::onPacketReceived);
        CHANNEL.registerMessage(++id, SkillLevelSyncPacket.class,
                SkillLevelSyncPacket::encode,
                SkillLevelSyncPacket::decode,
                SkillLevelSyncPacket::onPacketReceived);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(SkillRequirements.SKILL_SETTINGS);
        event.addListener(SkillRequirements.TEMPLATES);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        SkillRequirements.SKILL_SETTINGS.subscribeAsSyncable(CHANNEL, SkillRequirementSyncPacket::new);
        SkillRequirements.TEMPLATES.subscribeAsSyncable(CHANNEL, SkillLevelSyncPacket::new);
    }

    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            registerPackets();
            SkillRequirements.SKILL_SETTINGS.subscribeAsSyncable(CHANNEL, SkillRequirementSyncPacket::new);
            SkillRequirements.TEMPLATES.subscribeAsSyncable(CHANNEL, SkillLevelSyncPacket::new);
        });
    }

    public static void resetData() {
        SkillRequirements.SKILL_SETTINGS.clearData();
        SkillRequirements.TEMPLATES.clearData();
    }
}
