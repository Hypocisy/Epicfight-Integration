package com.kumoe.EpicFightIntegration.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.config.codecs.DataSource;

import java.util.*;

public record ReqType(Optional<Map<String, Integer>> levels) implements DataSource<ReqType> {
    public static final Codec<ReqType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("level").forGetter(ReqType::levels)
    ).apply(instance, ReqType::new));

    public ReqType() {
        this(Optional.of(Map.of()));
    }

    @Override
    public ReqType combine(ReqType two) {
        Map<String, Integer> combinedLevels = new HashMap<>();
        this.levels.ifPresent(combinedLevels::putAll);
        two.levels.ifPresent(stringIntegerMap -> stringIntegerMap.forEach((key, value) -> combinedLevels.merge(key, value, Math::max)));
        return new ReqType(Optional.of(combinedLevels));
    }

    @Override
    public boolean isUnconfigured() {
        return levels().isEmpty();
    }
}
