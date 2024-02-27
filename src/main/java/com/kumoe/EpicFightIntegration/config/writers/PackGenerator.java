package com.kumoe.EpicFightIntegration.config.writers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kumoe.EpicFightIntegration.EFIMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.config.codecs.SkillData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.Skill;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PackGenerator {
    public static final String PACKNAME = "efi_compat_pack";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static int generatePack(MinecraftServer server) {
        //create the filepath for our data pack.  this will do nothing if already created
        Path filepath = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(PACKNAME);
        filepath.toFile().mkdirs();
        Path packPath = filepath.resolve("pack.mcmeta");
        try {
            Files.writeString(
                    packPath,
                    gson.toJson(getPackObject()),
                    Charset.defaultCharset(),
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            EFIMod.LOGGER.debug("Error While Generating pack.mcmeta for epicfight skill Compat Generated Data: " + e.toString());
        }

        Map<String, SkillData> skillData = SkillsConfig.SKILLS.get();
        Map<String, Map<String, Integer>> result = new HashMap<>();
        Map<String, Integer> requirements = new HashMap<>();
        skillData.entrySet().parallelStream().forEach(entry -> {
            String skillName = entry.getKey();
            requirements.put(skillName, 0);
            result.put("requirements", requirements);
        });

        Path skillPath = filepath.resolve("data/" + EFIMod.MODID + "/" + EpicFightMod.MODID + "/skills/");
        skillPath.toFile().mkdirs();
        SkillManager.getLearnableSkillNames(Skill.Builder::isLearnable).toList().forEach(rl -> {

            String prettyPrinted = prettyPrintJSON(result.toString());
            try {
                Files.writeString(
                        skillPath.resolve(rl.getPath() + ".json"),
                        prettyPrinted,
                        Charset.defaultCharset(),
                        StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                EFIMod.LOGGER.debug("Error While Generating " + rl.getPath() + " for epicfight skill Compat Generated Data: " + e);
            }
        });

        return 0;
    }

    private static String prettyPrintJSON(String jsonString) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = JsonParser.parseString(jsonString);
        return gson.toJson(jsonElement);
    }

    private static JsonElement getPackObject() {
        McMeta pack = new McMeta(
                new Pack("Generated Resources", 9),
                Optional.empty());

        return McMeta.CODEC.encodeStart(JsonOps.INSTANCE, pack).result().get();
    }

    private record Pack(String description, int format) {
        public static final Codec<Pack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("description").forGetter(Pack::description),
                Codec.INT.fieldOf("pack_format").forGetter(Pack::format)
        ).apply(instance, Pack::new));
    }

    private record BlockFilter(Optional<String> namespace, Optional<String> path) {
        public static final Codec<BlockFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("namespace").forGetter(BlockFilter::namespace),
                Codec.STRING.optionalFieldOf("path").forGetter(BlockFilter::path)
        ).apply(instance, BlockFilter::new));
    }

    private record Filter(List<BlockFilter> block) {
        public static final Codec<Filter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockFilter.CODEC.listOf().fieldOf("block").forGetter(Filter::block)
        ).apply(instance, Filter::new));
    }

    private record McMeta(Pack pack, Optional<Filter> filter) {
        public static final Codec<McMeta> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Pack.CODEC.fieldOf("pack").forGetter(McMeta::pack),
                Filter.CODEC.optionalFieldOf("filter").forGetter(McMeta::filter)
        ).apply(instance, McMeta::new));
    }
}