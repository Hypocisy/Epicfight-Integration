package com.kumoe.EpicFightIntegration.event;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.config.EFIConfig;
import com.kumoe.EpicFightIntegration.util.CompactUtil;
import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@EventBusSubscriber(modid = EpicFightIntegration.MODID, value = Dist.CLIENT)
public class ClientEvent {
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
	 * Determines the request type based on key input
	 */
	private static @NotNull ReqType getReqType(InputEvent.InteractionKeyMappingTriggered event) {
		if (event.isAttack()) return ReqType.WEAPON;
		if (event.isUseItem()) return ReqType.USE;
		return ReqType.WEAPON;
	}
}
