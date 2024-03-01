package com.kumoe.EpicFightIntegration.util;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirement;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import harmonised.pmmo.api.APIUtils;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.HashMap;
import java.util.Map;


public class CompactUtil {
    public static SkillResult getSkillResult(PlayerPatch<?> caster, String skillName) {
        Map<ResourceLocation, SkillRequirement> raw = SkillRequirements.DATA_LOADER.getData();
        // Project mmo skill level
        Map<String, Integer> pmmoLevels = APIUtils.getAllLevels(caster.getOriginal());
        SkillResult skillResult = new SkillResult(new HashMap<>(), true);

        SkillRequirement skillRequirement = raw.get(new ResourceLocation(EpicFightIntegration.MODID, skillName));
        if (raw.isEmpty() || skillRequirement == null) {
            return skillResult;
        }

        if (skillRequirement.useDefault()) {
            skillRequirement.defaultReqs().forEach((pmmoSkillName, level) -> {
                int playerLevel = pmmoLevels.getOrDefault(pmmoSkillName,0);
                if (playerLevel < level) {
                    skillResult.noMeetReqs.put("default_reqs", Map.of(pmmoSkillName, level));
                }
            });
        } else {
            skillRequirement.reqs().forEach((req, skills) -> {
                EpicFightIntegration.LOGGER.debug("processing req of: " + req);

                skills.forEach((pmmoSkillName, level) -> {
                    EpicFightIntegration.LOGGER.debug("processing skill name of: " + pmmoSkillName);
                    EpicFightIntegration.LOGGER.debug("processing skill level of: " + level);

                    int playerLevel = pmmoLevels.getOrDefault(pmmoSkillName, 0);

                    if (playerLevel < level) {
                        skillResult.noMeetReqs.put(req, Map.of(pmmoSkillName, level));
                        skillResult.result = false;
                    }
                });
            });
        }

        return skillResult;
    }
}
