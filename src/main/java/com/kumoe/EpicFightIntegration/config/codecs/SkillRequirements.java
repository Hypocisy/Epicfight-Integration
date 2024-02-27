package com.kumoe.EpicFightIntegration.config.codecs;

import java.util.List;

public class SkillRequirements {
    public static final MergeableCodecDataManager<SkillRequirement, SkillRequirement> DATA_LOADER = new MergeableCodecDataManager<>(
            "epicfight/skills",
            SkillRequirement.CODEC,
            SkillRequirements::processSpellRequirements);
    public static SkillRequirement processSpellRequirements(final List<SkillRequirement> raws) {
        return raws.get(0);
    }
}