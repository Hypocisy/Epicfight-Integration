
package com.kumoe.EpicFightIntegration.util;


import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.config.EFIConfig;
import com.kumoe.EpicFightIntegration.config.codecs.CustomReqType;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.config.codecs.SkillSettings;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.PlayerData;
import harmonised.pmmo.core.Core;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.HashMap;
import java.util.Map;


public class CompactUtil {
	public static Map<String, Long> getSkillCondition(Player player, ResourceLocation resourceLocation) {

		// project mmo ignoreReqs command integration
		if (player.level().isClientSide()) {
			ResourceLocation playerID = ResourceLocation.parse(player.getStringUUID());
			Core core = Core.get(LogicalSide.CLIENT);
			PlayerData playerData = core.getLoader().PLAYER_LOADER.getData().get(playerID);
			if (playerData != null) {
				if (playerData.ignoreReq()) {
					return new HashMap<>();
				}
			}
		}

		SkillSettings skillSettingsData = SkillRequirements.SKILL_SETTINGS.getData(resourceLocation);
		Map<ResourceLocation, CustomReqType> templateData = SkillRequirements.TEMPLATES.getData();
		Map<String, Long> conditions = new HashMap<>();
		// Project mmo skill level
		skillSettingsData.templateNames().ifPresent(resourceLocations -> resourceLocations.forEach(rl -> {
			CustomReqType customReqType = templateData.get(rl);
			if (customReqType != null)
				customReqType.levels().ifPresent(tempMap -> tempMap.forEach((s, integer) -> conditions.merge(s, integer, Math::max)));
		}));
		skillSettingsData.defaultLevels().flatMap(CustomReqType::levels).ifPresent(pLevels -> pLevels.forEach((string, integer) -> conditions.merge(string, integer, Math::max)));
		return getRemainMapOrEmpty(APIUtils.getAllLevels(player), conditions);
	}

	/**
	 * Get remain condition if player not matches.<br>
	 *
	 * @param playerLevels    who's level need to check this requirement
	 * @param requiredSkills The requirement skills
	 * @return Empty HashMap if requiredSkills is empty, else return remain unmet requirement
	 */
	public static Map<String, Long> getRemainMapOrEmpty(Map<String, Long> playerLevels, Map<String, Long> requiredSkills) {
		if (requiredSkills.isEmpty()) {
			return new HashMap<>();
		}
		Map<String, Long> unmetSkillReqs = new HashMap<>();
		requiredSkills.forEach((skillName, requiredLevel) -> {
			// skips process required level < 0
			if (requiredLevel <= 0) return;
			if (playerLevels.containsKey(skillName)) {
				long playerLevel = playerLevels.get(skillName);
				boolean isSatisfied = playerLevel >= requiredLevel;
				if (!isSatisfied) {
					unmetSkillReqs.put(skillName, requiredLevel);
				}
			} else {
				unmetSkillReqs.put(skillName, requiredLevel);
			}
		});
		return unmetSkillReqs;
	}

	public static Map<String, Long> getHandItemCondition(PlayerPatch<?> playerPatch, ReqType reqType) {
		// project mmo requirement map
		var usedItemHand = playerPatch.getOriginal().getUsedItemHand();
		var validItem = playerPatch.getValidItemInHand(usedItemHand);
		var level = playerPatch.getLevel();
		return getItemCondition(validItem, level, reqType);
	}

	public static Map<String, Long> getItemCondition(ItemStack itemStack, Level level, ReqType reqType) {
		return APIUtils.getRequirementMap(itemStack, level, reqType, LogicalSide.CLIENT);
	}

	public static void displayMessage(Component text, Player player) {
		player.displayClientMessage(text, EFIConfig.enableActionBar);
	}

	public static ResourceLocation learnable(String val) {
		return ResourceLocation.fromNamespaceAndPath(EpicFightIntegration.MODID, "learn_able_skills/" + val);
	}

	public static ResourceLocation innate(String val) {
		return ResourceLocation.fromNamespaceAndPath(EpicFightIntegration.MODID, "other_skills/" + val);
	}

	public static ItemStack getValidItem(PlayerPatch<?> playerPatch) {
		return !playerPatch.isOffhandItemValid() || playerPatch.getValidItemInHand(InteractionHand.OFF_HAND).isEmpty() ? playerPatch.getOriginal().getMainHandItem() : playerPatch.getOriginal().getOffhandItem();
	}

	/**
	 * Checks if player meets custom skill conditions defined for weapon.
	 */
	public static boolean matchCustomSkillCondition(LocalPlayerPatch playerPatch, ReqType reqType) {
		ItemStack itemStack = CompactUtil.getValidItem(playerPatch);
		CapabilityItem item = EpicFightCapabilities.getItemStackCapability(itemStack);
		Skill innateSkill = item.getInnateSkill(playerPatch, itemStack);

		if (innateSkill == null) return true;

		Map<String, Long> conditions = CompactUtil.getSkillCondition(playerPatch.getOriginal(), getRlByCategory(innateSkill));
		if (conditions.isEmpty()) return true;

		if (EFIConfig.enableDebug) {
			playerPatch.getOriginal().sendSystemMessage(Component.translatable(
					"debug.efi_mod.message.1", innateSkill.getRegistryName().getPath()
			).withStyle(ChatFormatting.DARK_AQUA));
			playerPatch.getOriginal().sendSystemMessage(Component.translatable(
					"debug.efi_mod.message.2", reqType
			).withStyle(ChatFormatting.BLUE));
		}

		CompactUtil.displayMessage(
				Component.translatable("pmmo.msg.denial.skill", conditions)
						.withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)),
				playerPatch.getOriginal());

		return false;
	}

	public static ResourceLocation getRlByCategory(Skill skill) {
		return skill.getCategory().learnable() ? learnable(skill.getRegistryName().getPath()) : innate(skill.getRegistryName().getPath());
	}

	public static boolean isMatchCondition(LocalPlayerPatch playerPatch, ReqType reqType) {
		ItemStack itemStack = CompactUtil.getValidItem(playerPatch);
		CapabilityItem item = EpicFightCapabilities.getItemStackCapability(itemStack);
		Skill innateSkill = item.getInnateSkill(playerPatch, itemStack);

		Map<String, Long> handItemCondition = CompactUtil.getHandItemCondition(playerPatch, reqType);
		var remainCondition = getRemainMapOrEmpty(APIUtils.getAllLevels(playerPatch.getOriginal()), handItemCondition);

		if (innateSkill == null || remainCondition.isEmpty()) {
			return true;
		}
		if (EFIConfig.enableDebug) {
			playerPatch.getOriginal().sendSystemMessage(Component.translatable("debug.efi_mod.message.3", innateSkill.getRegistryName().getPath()).withStyle(ChatFormatting.DARK_AQUA));
			playerPatch.getOriginal().sendSystemMessage(Component.translatable("debug.efi_mod.message.4", reqType).withStyle(ChatFormatting.BLUE));
		}
		CompactUtil.displayMessage(Component.translatable("pmmo.msg.denial.skill", handItemCondition.toString()).withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), playerPatch.getOriginal());
		return false;
	}
}
