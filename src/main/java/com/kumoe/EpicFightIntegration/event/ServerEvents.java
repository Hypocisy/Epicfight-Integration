package com.kumoe.EpicFightIntegration.event;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.commands.CmdPmmoSkillBooksRoot;
import com.kumoe.EpicFightIntegration.config.EFIConfig;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.network.SkillLevelSyncPacket;
import com.kumoe.EpicFightIntegration.network.SkillRequirementSyncPacket;
import com.kumoe.EpicFightIntegration.util.CompactUtil;
import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

import java.util.Map;

@Mod.EventBusSubscriber(modid = EpicFightIntegration.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {
    private static final String CHANNEL_PROTOCOL = "0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(EpicFightIntegration.MODID, "main"),
            () -> CHANNEL_PROTOCOL,
            CHANNEL_PROTOCOL::equals,
            CHANNEL_PROTOCOL::equals);
    private static final int LEFT_MOUSE = 0;
    //    private static final int RIGHT_MOUSE = 1;
    private static final int START_PRESS_MOUSE = 1;
//    private static final int END_PRESS_MOUSE = 0;

    @SubscribeEvent
    public static void onCommandRegister(final RegisterCommandsEvent event) {
        CmdPmmoSkillBooksRoot.register(event.getDispatcher());
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

    public static void resetData() {
        SkillRequirements.SKILL_SETTINGS.clearData();
        SkillRequirements.TEMPLATES.clearData();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMouseButtonPressed(final InputEvent.MouseButton.Pre event) {
        ControllEngine controllEngine = ClientEngine.getInstance().controllEngine;
        LocalPlayerPatch playerPatch = controllEngine.getPlayerPatch();
        boolean caceled = false;
        // 0 is left mouse button
        // 1 is right mouse button
        if (ClientEngine.getInstance().minecraft.player != null && Minecraft.getInstance().screen == null && event.getButton() == LEFT_MOUSE) {
            Map<String, Integer> pmmoConditions = CompactUtil.checkUnmetSkillRequirements(playerPatch, CompactUtil.getConditions(playerPatch, ReqType.WEAPON));
            if (!pmmoConditions.isEmpty()) {
                if (event.getAction() == START_PRESS_MOUSE && EFIConfig.enableDebug) {
                    playerPatch.getOriginal().sendSystemMessage(Component.translatable("debug.efi_mod.message.3", ReqType.WEAPON).withStyle(ChatFormatting.BLUE));
                    playerPatch.getOriginal().sendSystemMessage(Component.translatable("debug.efi_mod.message.4", pmmoConditions).withStyle(ChatFormatting.BLUE));
                    CompactUtil.displayMessage(Component.translatable("pmmo.msg.denial.skill", pmmoConditions).withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), playerPatch.getOriginal());
                }
                caceled = true;

            }
            event.setCanceled(caceled);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && EFIConfig.enableAutoToggleMode) {
            LocalPlayerPatch playerPatch = ClientEngine.getInstance().controllEngine.getPlayerPatch();
            if (playerPatch != null)
                CompactUtil.autoToggleMode(playerPatch);
        }
    }
}
