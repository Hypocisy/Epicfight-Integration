package com.kumoe.EpicFightIntegration.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.config.codecs.CodecTypes;

import java.util.Map;

public record SkillRequirement(Map<String, Map<String,  Integer>> reqs, Map<String, Integer> defaultReqs,
                               boolean useDefault) {
    public static final Codec<SkillRequirement> CODEC = RecordCodecBuilder.create(skillRequirementInstance -> skillRequirementInstance.group(
            Codec.unboundedMap(Codec.STRING, CodecTypes.INTEGER_CODEC).fieldOf("requirements").forGetter(SkillRequirement::reqs),
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("default_reqs").forGetter(SkillRequirement::defaultReqs),
            Codec.BOOL.fieldOf("use_default").forGetter(SkillRequirement::useDefault)
    ).apply(skillRequirementInstance, SkillRequirement::new));
}
