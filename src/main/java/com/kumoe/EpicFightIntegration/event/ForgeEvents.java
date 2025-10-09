package com.kumoe.EpicFightIntegration.event;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.commands.CmdPmmoSkillBooksRoot;
import com.kumoe.EpicFightIntegration.config.EFIConfig;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.util.CompactUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import yesman.epicfight.api.neoevent.playerpatch.SkillCastEvent;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.skill.SkillDataManager;

import java.util.Map;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = EpicFightIntegration.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ForgeEvents {

	@SubscribeEvent
	public static void onCommandRegister(final RegisterCommandsEvent event) {
		CmdPmmoSkillBooksRoot.register(event.getDispatcher());
	}

	@SubscribeEvent
	public static void onAddReloadListeners(AddReloadListenerEvent event) {
		event.addListener(SkillRequirements.SKILL_SETTINGS);
		event.addListener(SkillRequirements.TEMPLATES);
	}

	public static void resetData() {
		SkillRequirements.SKILL_SETTINGS.clearData();
		SkillRequirements.TEMPLATES.clearData();
	}

	@SubscribeEvent
	public static void onSkillCast(SkillCastEvent event) {
		var skillContainer = event.getSkillContainer();
		var playerpatch = event.getPlayerPatch();
		var player = playerpatch.getOriginal();
		var skill = skillContainer.getSkill();

		if (skill == null) return;

		Map<String, Long> conditions = CompactUtil.getSkillCondition(player, CompactUtil.getRlByCategory(skill));
		if (!conditions.isEmpty()){
			resetSkillJumpData(skillContainer.getDataManager());

			CompactUtil.displayMessage(
					Component.translatable("pmmo.msg.denial.skillcast", skill.getDisplayName().getString(), conditions.toString())
							.withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)), player);

			if (EFIConfig.enableDebug) {
				EpicFightIntegration.LOGGER.debug(conditions.toString());
			}
			event.setCanceled(true);
		}
	}

	static void resetSkillJumpData(SkillDataManager dataManager) {
		if (dataManager.hasData(EpicFightSkillDataKeys.JUMP_COUNT)) {
			dataManager.setDataSync(EpicFightSkillDataKeys.JUMP_COUNT, 0);
		}
		if (dataManager.hasData(EpicFightSkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK)) {
			dataManager.setDataSync(EpicFightSkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK, false);
		}
	}
}
