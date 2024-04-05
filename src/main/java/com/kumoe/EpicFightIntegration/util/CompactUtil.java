package com.kumoe.EpicFightIntegration.util;


import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.config.EFIConfig;
import com.kumoe.EpicFightIntegration.config.codecs.ReqType;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.config.codecs.SkillSettings;
import harmonised.pmmo.api.APIUtils;
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
//            if (this.minecraft.crosshairPickEntity instanceof Mob && this.minecraft.crosshairPickEntity.isAlive() && !playerpatch.isBattleMode()) {
            if (playerPatch.isBattleMode() && playerPatch.getTarget() == null) {
                playerPatch.toMiningMode(true);
                CompactUtil.send(Component.translatable("debug.efi_mod.toggle", playerPatch.getPlayerMode().toString()).withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)), playerPatch.getOriginal());
            } else if (!playerPatch.isBattleMode() && playerPatch.getTarget() != null && playerPatch.getTarget().isAlive()) {
                playerPatch.toBattleMode(true);
                CompactUtil.send(Component.translatable("debug.efi_mod.toggle", playerPatch.getPlayerMode().toString()).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA)), playerPatch.getOriginal());
            }
        }
    }

    public static Map<String, Integer> getConditions(PlayerPatch<?> caster, ResourceLocation resourceLocation) {

        // project mmo ignoreReqs command integration
        if (caster.isLogicalClient()) {
            ResourceLocation playerID = new ResourceLocation(caster.getOriginal().getUUID().toString());
            Core core = Core.get(LogicalSide.CLIENT);
            PlayerData existing = core.getLoader().PLAYER_LOADER.getData().get(playerID);
            if (existing != null) {
                if (existing.ignoreReq()) {
                    return Map.of();
                }
            }
        }

        SkillSettings skillSettingsData = SkillRequirements.SKILL_SETTINGS.getData(resourceLocation);
        Map<ResourceLocation, ReqType> templateData = SkillRequirements.TEMPLATES.getData();
        Map<String, Integer> reqList = new HashMap<>();
        Map<String, Integer> unMatchedList = new HashMap<>();
        // Project mmo skill level
        Map<String, Integer> playerLevels = APIUtils.getAllLevels(caster.getOriginal());
        skillSettingsData.templateNames().ifPresent(resourceLocations -> resourceLocations.forEach(rl -> {
            if (templateData.get(rl) != null)
                templateData.get(rl).levels().ifPresent(tempMap -> tempMap.forEach((s, integer) -> reqList.merge(s, integer, Math::max)));
        }));

        if (!reqList.isEmpty()) {
            reqList.forEach((skillName, requiredLevel) -> {
                if (playerLevels.containsKey(skillName)) {
                    int playerLevel = playerLevels.get(skillName);
                    boolean isSatisfied = playerLevel >= requiredLevel;
                    if (!isSatisfied) {
                        unMatchedList.put(skillName, requiredLevel);
                    }
                } else {
                    unMatchedList.put(skillName, requiredLevel);
                }
            });
        }

        return unMatchedList;
    }

    public static void send(Component text, Player player) {
        if (EFIConfig.enableActionBar)
            player.displayClientMessage(text, true);
    }

    public static ResourceLocation learnAble(String val) {
        return new ResourceLocation(EpicFightIntegration.MODID, "learn_able_skills/" + val);
    }

    public static boolean dosePlayerMeetReqs(PlayerPatch<?> playerPatch, harmonised.pmmo.api.enums.ReqType reqType) {
        Map<String, Integer> reqs = APIUtils.getRequirementMap(playerPatch.getValidItemInHand(playerPatch.getOriginal().getUsedItemHand()), reqType, LogicalSide.CLIENT);
        if (EFIConfig.enableDebug) {
            playerPatch.getOriginal().sendSystemMessage(Component.translatable("debug.efi_mod.message.3", reqType.getName()).withStyle(ChatFormatting.BLUE));
            playerPatch.getOriginal().sendSystemMessage(Component.translatable("debug.efi_mod.message.4", reqs.toString()).withStyle(ChatFormatting.BLUE));
        }
        return Core.get(LogicalSide.CLIENT).doesPlayerMeetReq(playerPatch.getOriginal().getUUID(), reqs);
    }

    public static ResourceLocation innate(String val) {
//        EpicFightIntegration.LOGGER.debug(val);
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
                playerpatch.getOriginal().sendSystemMessage(Component.translatable("debug.efi_mod.message.2", conditions.toString()).withStyle(ChatFormatting.DARK_AQUA));
            }
            if (!conditions.isEmpty()) {
                CompactUtil.send(Component.translatable("pmmo.msg.denial.skill", conditions.toString()).withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.DARK_GRAY)), playerpatch.getOriginal());
                return false;
            }
        }
        return true;
    }
}
