package com.kumoe.EpicFightIntegration.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.config.codecs.DataSource;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record SkillSettings(Optional<List<ResourceLocation>> templateNames, Optional<ReqType> defaultLevels,
                            boolean override) implements DataSource<SkillSettings> {
    public static final Codec<SkillSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(ResourceLocation.CODEC).optionalFieldOf("templates").forGetter(SkillSettings::templateNames),
            ReqType.CODEC.optionalFieldOf("default_requirements").forGetter(SkillSettings::defaultLevels),
            Codec.BOOL.fieldOf("override").forGetter(SkillSettings::override)
    ).apply(instance, SkillSettings::new));

    public SkillSettings() {
        this(Optional.of(List.of()), Optional.of(new ReqType()), false);
    }

    @Override
    public SkillSettings combine(SkillSettings two) {
        List<ResourceLocation> combinedTempNames = new ArrayList<>();
        ReqType combinedDefaultLevels = new ReqType();

        // 合并 template names
        this.templateNames.ifPresent(combinedTempNames::addAll);
        two.templateNames.ifPresent(strings -> combinedTempNames.addAll(strings.stream().filter(item -> !combinedTempNames.contains(item)).toList()));
        // 合并override
        boolean combinedOverride = this.override || two.override;
        // 合并levels, 如果 override 为 false 则不必合并

        if (combinedOverride) {
            this.defaultLevels.ifPresent(combinedDefaultLevels::combine);
            two.defaultLevels.ifPresent(combinedDefaultLevels::combine);
        }

        return new SkillSettings(Optional.of(combinedTempNames), Optional.of(combinedDefaultLevels), combinedOverride);
    }

    @Override
    public boolean isUnconfigured() {
        return templateNames().isEmpty() && defaultLevels().isEmpty() && !override();
    }
}
