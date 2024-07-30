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
                CompactUtil.displayMessage(Component.translatable("debug.efi_mod.toggle", playerPatch.getPlayerMode().toString()).withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)), playerPatch.getOriginal());
            } else if (!playerPatch.isBattleMode() && playerPatch.getTarget() != null && playerPatch.getTarget().isAlive()) {
                playerPatch.toBattleMode(true);
                CompactUtil.displayMessage(Component.translatable("debug.efi_mod.toggle", playerPatch.getPlayerMode().toString()).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA)), playerPatch.getOriginal());
            }
        }
    }

    public static Map<String, Integer> getConditions(PlayerPatch<?> caster, ResourceLocation resourceLocation) {

        // project mmo ignoreReqs command integration
        if (caster.isLogicalClient()) {
            ResourceLocation playerID = new ResourceLocation(caster.getOriginal().getUUID().toString());
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
        // project mmo requirement map
        Map<String, Integer> requirement = APIUtils.getRequirementMap(caster.getValidItemInHand(caster.getOriginal().getUsedItemHand()), ReqType.WEAPON, LogicalSide.CLIENT);
        Map<String, Integer> customReq = new HashMap<>(requirement);
        Map<String, Integer> conditions = new HashMap<>();
        // Project mmo skill level
        Map<String, Integer> playerLevels = APIUtils.getAllLevels(caster.getOriginal());
        skillSettingsData.templateNames().ifPresent(resourceLocations -> resourceLocations.forEach(rl -> {
            CustomReqType customReqType = templateData.get(rl);
            if (customReqType != null)
                customReqType.levels().ifPresent(tempMap -> tempMap.forEach((s, integer) -> customReq.merge(s, integer, Math::max)));
        }));
        skillSettingsData.defaultLevels().flatMap(CustomReqType::levels).ifPresent(pLevels -> pLevels.forEach((string, integer) -> customReq.merge(string, integer, Math::max)));

        if (!customReq.isEmpty()) {
            customReq.forEach((skillName, requiredLevel) -> {
                if (playerLevels.containsKey(skillName)) {
                    int playerLevel = playerLevels.get(skillName);
                    boolean isSatisfied = playerLevel >= requiredLevel;
                    if (!isSatisfied) {
                        conditions.put(skillName, requiredLevel);
                    }
                } else {
                    conditions.put(skillName, requiredLevel);
                }
            });
        }

        return conditions;
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
        return !playerPatch.isOffhandItemValid() || playerPatch.getValidItemInHand(InteractionHand.OFF_HAND).isEmpty() ?
                playerPatch.getOriginal().getMainHandItem() : playerPatch.getOriginal().getOffhandItem();
    }

    public static boolean processWeaponSkill(LocalPlayerPatch playerpatch) {
        ItemStack itemStack = CompactUtil.getValidItem(playerpatch);
        CapabilityItem item = EpicFightCapabilities.getItemStackCapability(itemStack);
        Skill innateSkill = item.getInnateSkill(playerpatch, itemStack);

        if (innateSkill != null) {
            Map<String, Integer> conditions = CompactUtil.getConditions(playerpatch, CompactUtil.innate(innateSkill.getRegistryName().getPath()));

            if (EFIConfig.enableDebug) {
                playerpatch.getOriginal().sendSystemMessage(Component.translatable("debug.efi_mod.message.1", innateSkill.getRegistryName().getPath()).withStyle(ChatFormatting.DARK_AQUA));
                playerpatch.getOriginal().sendSystemMessage(Component.translatable("debug.efi_mod.message.2", ReqType.WEAPON).withStyle(ChatFormatting.BLUE));
                playerpatch.getOriginal().sendSystemMessage(Component.translatable("debug.efi_mod.message.3", conditions.toString()).withStyle(ChatFormatting.BLUE));
            }
            if (!conditions.isEmpty()) {
                CompactUtil.displayMessage(Component.translatable("pmmo.msg.denial.skill", conditions.toString()).withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), playerpatch.getOriginal());
                return false;
            }
        }
        return true;
    }
}
