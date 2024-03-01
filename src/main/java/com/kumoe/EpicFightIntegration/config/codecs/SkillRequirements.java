package com.kumoe.EpicFightIntegration.config.codecs;

import java.util.List;

public class SkillRequirements {
    public static final MergeableCodecDataManager<SkillRequirement, SkillRequirement> DATA_LOADER = new MergeableCodecDataManager<>(
            "epicfight/skills",
            SkillRequirement.CODEC,
            SkillRequirements::processSkillRequirements);

    private static SkillRequirement processSkillRequirements(final List<SkillRequirement> raws) {
        if (raws.isEmpty()) {
            // 返回一个默认的 SkillRequirement 或者抛出一个异常
            throw new RuntimeException("No SkillRequirement provided");
        }
        return raws.get(0);
    }
}