package com.kumoe.EpicFightIntegration.config.codecs;

import harmonised.pmmo.config.codecs.DataSource;
import harmonised.pmmo.config.readers.MergeableCodecDataManager;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class SkillRequirements {
    private static final Logger DATA_LOGGER = LogManager.getLogger();

    public static final MergeableCodecDataManager<SkillSettings, SkillSettings> SKILL_SETTINGS = new MergeableCodecDataManager<>(
            "skill_settings",
            DATA_LOGGER,
            SkillSettings.CODEC,
            SkillRequirements::mergeLoaderData,
            SkillRequirements::printData,
            SkillSettings::new, null);
    public static final MergeableCodecDataManager<ReqType, ReqType> TEMPLATES = new MergeableCodecDataManager<>(
            "templates",
            DATA_LOGGER,
            ReqType.CODEC,
            SkillRequirements::mergeLoaderData,
            SkillRequirements::printData,
            ReqType::new, null);


    private static <T extends DataSource<T>> T mergeLoaderData(final List<T> raws) {
        T out = raws.stream().reduce(DataSource::combine).get();
        return out.isUnconfigured() ? null : out;
    }

    private static void printData(Map<ResourceLocation, ? extends Record> data) {
        data.forEach((id, value) -> {
            if (id == null || value == null) return;
            MsLoggy.INFO.log(MsLoggy.LOG_CODE.DATA, "Object: {} with Data: {}", id.toString(), value.toString());
        });
    }
}
