package com.kumoe.EpicFightIntegration.event;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.commands.CmdPmmoSkillBooksRoot;
import com.kumoe.EpicFightIntegration.config.EFIConfig;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.network.SkillLevelSyncPacket;
import com.kumoe.EpicFightIntegration.network.SkillRequirementSyncPacket;
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

	@SubscribeEvent
	public static void onDatapackSync(net.neoforged.neoforge.event.OnDatapackSyncEvent event) {
		SkillRequirements.SKILL_SETTINGS.subscribeAsSyncable(SkillRequirementSyncPacket::new);
		SkillRequirements.TEMPLATES.subscribeAsSyncable(SkillLevelSyncPacket::new);
	}

	public static void resetData() {
		SkillRequirements.SKILL_SETTINGS.clearData();
		SkillRequirements.TEMPLATES.clearData();
	}

	@SubscribeEvent
	public static void onSkillCast(SkillCastEvent event) {
		var skillContainer = event.getSkillContainer();
		var playerpatch = event.getPlayerPatch();
		var skill = skillContainer.getSkill();

		if (skill != null) {
			Map<String, Integer> conditions = CompactUtil.getSkillCondition(playerpatch, CompactUtil.getRlByCategory(skill));
			if (!conditions.isEmpty()) {
				if (skillContainer.getDataManager().hasData(EpicFightSkillDataKeys.JUMP_COUNT)) {
					skillContainer.getDataManager().setData(EpicFightSkillDataKeys.JUMP_COUNT, 0);
				}
				if (playerpatch.isLogicalClient() && skillContainer.getDataManager().hasData(EpicFightSkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK)) {
					skillContainer.getDataManager().setData(EpicFightSkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK, false);
				}
				CompactUtil.displayMessage(Component.translatable("pmmo.msg.denial.skill", conditions.toString()).withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), playerpatch.getOriginal());
				if (EFIConfig.enableDebug) {
					EpicFightIntegration.LOGGER.debug(conditions.toString());
				}
				event.setCanceled(true);
			}
		}
	}

}
