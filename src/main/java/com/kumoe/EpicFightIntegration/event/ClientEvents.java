package com.kumoe.EpicFightIntegration.event;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.config.EFIConfig;
import com.kumoe.EpicFightIntegration.util.CompactUtil;
import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = EpicFightIntegration.MODID, value = Dist.CLIENT)
public class ClientEvents {

	/** Handles Epic Fight skill cast denial if PMMO conditions not met */
	private static void handleSkillCast(PlayerPatch<?> playerPatch, Player player) {
		playerPatch.getEventListener().addEventListener(
				PlayerEventListener.EventType.SKILL_CAST_EVENT,
				UUID.fromString("39d977a3-f5f7-4761-bc24-a158d4d3f9f9"),
				skillCastEvent -> {
					SkillContainer skillContainer = skillCastEvent.getSkillContainer();
					if (skillContainer == null) return;

					var skill = skillContainer.getSkill();
					if (skill == null) return;

					Map<String, Integer> conditions = CompactUtil.getSkillCondition(player, CompactUtil.getRlByCategory(skill));
					if (conditions.isEmpty()) return;

					resetSkillJumpData(skillContainer.getDataManager());

					CompactUtil.displayMessage(
							Component.translatable("pmmo.msg.denial.skillcast",skill.getDisplayName().getString(), conditions.toString())
									.withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)), player);

					if (EFIConfig.enableDebug) {
						EpicFightIntegration.LOGGER.debug(conditions.toString());
					}
					skillCastEvent.setCanceled(true);
				});
	}

	private static void resetSkillJumpData(SkillDataManager dataManager) {
		if (dataManager.hasData(SkillDataKeys.JUMP_COUNT.get())) {
			dataManager.setDataSync(SkillDataKeys.JUMP_COUNT.get(), 0);
		}
		if (dataManager.hasData(SkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK.get())) {
			dataManager.setDataSync(SkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK.get(), false);
		}
	}

	@SubscribeEvent
	public static void onMovementUpdate(MovementInputUpdateEvent event) {

		var player = event.getEntity();
		var playerPatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);

		handleSkillCast(playerPatch, player);

	}

	@SubscribeEvent
	public static void mouseClickEvent(InputEvent.InteractionKeyMappingTriggered event) {
		if (!(event.isAttack() || event.isUseItem())) return;

		Player player = Minecraft.getInstance().player;
		if (player == null) return;

		LocalPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, LocalPlayerPatch.class);
		if (playerPatch == null) return;

		ReqType reqType = getReqType(event);

		if (!checkWeaponUsageAllowed(playerPatch, reqType)) {
			event.getKeyMapping().setDown(false);
			event.setSwingHand(false);
			event.setCanceled(true);
		}
	}
	/**
	 * Checks whether player can use weapon under current configuration.
	 * If config.requireCombinedWeaponLevel == false → only PMMO check.
	 * If true → require both PMMO and custom skill conditions.
	 */
	private static boolean checkWeaponUsageAllowed(LocalPlayerPatch playerPatch, ReqType reqType) {
		// PMMO check
		boolean pmmoOk = CompactUtil.isMatchCondition(playerPatch, reqType);

		// If only PMMO check is required
		if (!EFIConfig.require_combined_weapon_level) {
			return pmmoOk;
		}

		// Combined check: PMMO + Skill conditions
		boolean skillOk = CompactUtil.matchCustomSkillCondition(playerPatch, reqType);
		return pmmoOk && skillOk;
	}


	/** Determines the request type based on key input */
	private static @NotNull ReqType getReqType(InputEvent.InteractionKeyMappingTriggered event) {
		if (event.isAttack()) return ReqType.WEAPON;
		if (event.isUseItem()) return ReqType.USE;
		return ReqType.WEAPON;
	}

}
