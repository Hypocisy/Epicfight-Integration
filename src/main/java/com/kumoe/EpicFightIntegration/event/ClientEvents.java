package com.kumoe.EpicFightIntegration.event;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import static com.kumoe.EpicFightIntegration.event.ForgeEvents.handelSkillCast;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = EpicFightIntegration.MODID, value = Dist.CLIENT)
public class ClientEvents {

	@SubscribeEvent
	public static void onMovementUpdate(MovementInputUpdateEvent event) {

		var player = event.getEntity();
		var playerPatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);

		handelSkillCast(playerPatch, player);

	}
	/*
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
		}*/
}
