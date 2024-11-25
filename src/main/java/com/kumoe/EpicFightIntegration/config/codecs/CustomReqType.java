package com.kumoe.EpicFightIntegration.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.config.codecs.DataSource;

import java.util.*;

public record CustomReqType(Optional<Map<String, Integer>> levels) implements DataSource<CustomReqType> {
    public static final Codec<CustomReqType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("level").forGetter(CustomReqType::levels)
    ).apply(instance, CustomReqType::new));

    public CustomReqType() {
        this(Optional.of(Map.of()));
    }

    @Override
    public CustomReqType combine(CustomReqType two) {
        Map<String, Integer> combinedLevels = new HashMap<>();
        this.levels.ifPresent(combinedLevels::putAll);
        two.levels.ifPresent(stringIntegerMap -> stringIntegerMap.forEach((key, value) -> combinedLevels.merge(key, value, Math::max)));
        return new CustomReqType(Optional.of(combinedLevels));
    }

    @Override
    public boolean isUnconfigured() {
        return levels().isEmpty();
    }
}
