package com.kumoe.EpicFightIntegration.util;


import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.config.codecs.ReqType;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.config.codecs.SkillSettings;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.config.codecs.PlayerData;
import harmonised.pmmo.core.Core;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.HashMap;
import java.util.Map;


public class CompactUtil {

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
        player.displayClientMessage(text, true);
    }

    public static ResourceLocation learnAble(String val) {
        return new ResourceLocation(EpicFightIntegration.MODID, "learn_able_skills/" + val);
    }

    public static ResourceLocation rl(String val) {
//        EpicFightIntegration.LOGGER.debug(val);
        return new ResourceLocation(EpicFightIntegration.MODID, "other_skills/" + val);
    }

    public static ItemStack getValidItem(PlayerPatch<?> playerPatch) {
        return !playerPatch.isOffhandItemValid() || playerPatch.getValidItemInHand(InteractionHand.OFF_HAND).isEmpty() ?
                playerPatch.getOriginal().getMainHandItem() : playerPatch.getOriginal().getOffhandItem();
    }
}
