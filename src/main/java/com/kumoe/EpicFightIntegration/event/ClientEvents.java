package com.kumoe.EpicFightIntegration.event;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.config.EFIConfig;
import com.kumoe.EpicFightIntegration.util.CompactUtil;
import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = EpicFightIntegration.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMouseButtonPressed(final InputEvent.MouseButton.Pre event) {
        ControllEngine controllEngine = ClientEngine.getInstance().controllEngine;
        LocalPlayerPatch playerPatch = controllEngine.getPlayerPatch();
        boolean caceled = false;
        // 0 is left mouse button
        // 1 is right mouse button
        if (ClientEngine.getInstance().minecraft.player != null && Minecraft.getInstance().screen == null && EpicFightKeyMappings.ATTACK.matchesMouse(event.getButton())) {
            Map<String, Integer> pmmoConditions = CompactUtil.getRemainMapOrEmpty(playerPatch, CompactUtil.getHandItemCondition(playerPatch, ReqType.WEAPON));
            if (!pmmoConditions.isEmpty()) {
                if (EFIConfig.enableDebug) {
                    playerPatch.getOriginal().sendSystemMessage(Component.translatable("debug.efi_mod.message.3", ReqType.WEAPON).withStyle(ChatFormatting.BLUE));
                    playerPatch.getOriginal().sendSystemMessage(Component.translatable("debug.efi_mod.message.4", pmmoConditions).withStyle(ChatFormatting.BLUE));
                    CompactUtil.displayMessage(Component.translatable("pmmo.msg.denial.skill", pmmoConditions).withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), playerPatch.getOriginal());
                }
                caceled = true;

            }
            event.setCanceled(caceled);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onKeyBoardInput(final InputEvent.InteractionKeyMappingTriggered event) {
        if (isKeySameAndDown(EpicFightKeyMappings.ATTACK, event.getKeyMapping())) {
            ControllEngine controllEngine = ClientEngine.getInstance().controllEngine;
            LocalPlayerPatch playerPatch = controllEngine.getPlayerPatch();
            boolean caceled = false;
            // 0 is left mouse button
            // 1 is right mouse button
            if (ClientEngine.getInstance().minecraft.player != null && Minecraft.getInstance().screen == null) {
                Map<String, Integer> pmmoConditions = CompactUtil.getRemainMapOrEmpty(playerPatch, CompactUtil.getHandItemCondition(playerPatch, ReqType.WEAPON));
                if (!pmmoConditions.isEmpty()) {
                    if (EFIConfig.enableDebug) {
                        CompactUtil.displayMessage(Component.translatable("pmmo.msg.denial.skill", pmmoConditions).withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), playerPatch.getOriginal());
                    }
                    caceled = true;

                }
                event.setCanceled(caceled);
            }
        } else if (isKeySameAndDown(EpicFightKeyMappings.WEAPON_INNATE_SKILL, event.getKeyMapping())) {
            ControllEngine controllEngine = ClientEngine.getInstance().controllEngine;
            LocalPlayerPatch playerPatch = controllEngine.getPlayerPatch();
            boolean caceled = false;
            // 0 is left mouse button
            // 1 is right mouse button
            if (ClientEngine.getInstance().minecraft.player != null && Minecraft.getInstance().screen == null) {
                if (!CompactUtil.isMatchCondition(playerPatch)) {
                    caceled = true;
                }
                event.setSwingHand(false);
                event.setCanceled(caceled);
            }
        }
    }

    public static boolean isKeySameAndDown(KeyMapping modKey, KeyMapping eventKey) {
        return modKey.isDown() && modKey.same(eventKey);
    }

    @SubscribeEvent
    public static void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && EFIConfig.enableAutoToggleMode) {
            LocalPlayerPatch playerPatch = ClientEngine.getInstance().controllEngine.getPlayerPatch();
            if (playerPatch != null)
                CompactUtil.autoToggleMode(playerPatch);
        }
    }
}
