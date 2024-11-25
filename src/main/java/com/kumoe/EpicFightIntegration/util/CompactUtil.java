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
import net.minecraftforge.fml.LogicalSide;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.HashMap;
import java.util.Map;


public class CompactUtil {

    public static void autoToggleMode(PlayerPatch<?> playerPatch) {
        if (playerPatch.getOriginal().tickCount % EFIConfig.autoToggleTime == 0) {
            if (playerPatch.isBattleMode() && playerPatch.getTarget() == null) {
                playerPatch.toMiningMode(true);
                CompactUtil.displayMessage(Component.translatable("debug.efi_mod.toggle", playerPatch.getPlayerMode()).withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)), playerPatch.getOriginal());
            } else if (!playerPatch.isBattleMode() && playerPatch.getTarget() != null && playerPatch.getTarget().isAlive()) {
                playerPatch.toBattleMode(true);
                CompactUtil.displayMessage(Component.translatable("debug.efi_mod.toggle", playerPatch.getPlayerMode()).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA)), playerPatch.getOriginal());
            }
        }
    }

    public static Map<String, Integer> getSkillCondition(PlayerPatch<?> playerPatch, ResourceLocation resourceLocation) {

        // project mmo ignoreReqs command integration
        if (playerPatch.isLogicalClient()) {
            ResourceLocation playerID = new ResourceLocation(playerPatch.getOriginal().getUUID().toString());
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
        Map<String, Integer> conditions = new HashMap<>();
        // Project mmo skill level
        skillSettingsData.templateNames().ifPresent(resourceLocations -> resourceLocations.forEach(rl -> {
            CustomReqType customReqType = templateData.get(rl);
            if (customReqType != null)
                customReqType.levels().ifPresent(tempMap -> tempMap.forEach((s, integer) -> conditions.merge(s, integer, Math::max)));
        }));
        skillSettingsData.defaultLevels().flatMap(CustomReqType::levels).ifPresent(pLevels -> pLevels.forEach((string, integer) -> conditions.merge(string, integer, Math::max)));
        return getRemainMapOrEmpty(playerPatch, conditions);
    }

    /**
     * Get remain condition if player not matches.<br>
     *
     * @param playerPatch    who need to check this requirement
     * @param requiredSkills The requirement skills
     * @return Empty HashMap if requiredSkills is empty, else return remain unmet requirement
     */
    public static Map<String, Integer> getRemainMapOrEmpty(PlayerPatch<?> playerPatch, Map<String, Integer> requiredSkills) {
        if (requiredSkills.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Integer> playerLevels = APIUtils.getAllLevels(playerPatch.getOriginal());
        Map<String, Integer> unmetSkillReqs = new HashMap<>();
        if (!requiredSkills.isEmpty()) {
            requiredSkills.forEach((skillName, requiredLevel) -> {
                if (playerLevels.containsKey(skillName)) {
                    int playerLevel = playerLevels.get(skillName);
                    boolean isSatisfied = playerLevel >= requiredLevel;
                    if (!isSatisfied) {
                        unmetSkillReqs.put(skillName, requiredLevel);
                    }
                } else {
                    unmetSkillReqs.put(skillName, requiredLevel);
                }
            });
        }
        return unmetSkillReqs;
    }

    public static Map<String, Integer> getHandItemCondition(PlayerPatch<?> playerPatch, ReqType reqType) {
        // project mmo requirement map
        var usedItemHand = playerPatch.getOriginal().getUsedItemHand();
        var validItem = playerPatch.getValidItemInHand(usedItemHand);
        return getItemCondition(validItem, reqType);
    }

    public static Map<String, Integer> getItemCondition(ItemStack itemStack, ReqType reqType) {
        return APIUtils.getRequirementMap(itemStack, reqType, LogicalSide.CLIENT);
    }

    public static void displayMessage(Component text, Player player) {
        player.displayClientMessage(text, EFIConfig.enableActionBar);
    }

    public static ResourceLocation learnable(String val) {
        return new ResourceLocation(EpicFightIntegration.MODID, "learn_able_skills/" + val);
    }

    public static ResourceLocation innate(String val) {
        return new ResourceLocation(EpicFightIntegration.MODID, "other_skills/" + val);
    }

    public static ItemStack getValidItem(PlayerPatch<?> playerPatch) {
        return !playerPatch.isOffhandItemValid() || playerPatch.getValidItemInHand(InteractionHand.OFF_HAND).isEmpty() ? playerPatch.getOriginal().getMainHandItem() : playerPatch.getOriginal().getOffhandItem();
    }

    /**
     * Is player match custom datapack conditions?
     *
     * @param playerPatch packed player by epicfight
     * @return true if player match, otherwise false.
     */
    public static boolean isMatchCondition(LocalPlayerPatch playerPatch) {
        ItemStack itemStack = CompactUtil.getValidItem(playerPatch);
        CapabilityItem item = EpicFightCapabilities.getItemStackCapability(itemStack);
        Skill innateSkill = item.getInnateSkill(playerPatch, itemStack);

        if (innateSkill != null) {
            // custom skill conditions
            Map<String, Integer> conditions = CompactUtil.getSkillCondition(playerPatch, CompactUtil.innate(innateSkill.getRegistryName().getPath()));
            if (EFIConfig.enableDebug) {
                playerPatch.getOriginal().sendSystemMessage(Component.translatable("debug.efi_mod.message.1", innateSkill.getRegistryName().getPath()).withStyle(ChatFormatting.DARK_AQUA));
                playerPatch.getOriginal().sendSystemMessage(Component.translatable("debug.efi_mod.message.2", ReqType.WEAPON).withStyle(ChatFormatting.BLUE));
            }
            if (!conditions.isEmpty()) {
                CompactUtil.displayMessage(Component.translatable("pmmo.msg.denial.skill", conditions).withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), playerPatch.getOriginal());
                return false;
            }
        }
        return true;
    }
}
