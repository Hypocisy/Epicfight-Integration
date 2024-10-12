package com.kumoe.EpicFightIntegration.config.writers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.config.codecs.CustomReqType;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.config.codecs.SkillSettings;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.skill.Skill;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;

public class PackGenerator {
    public static final String PACK_NAME = "efi_compat_pack";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static boolean applyOverride = false, applyDefaults = false, applyDisabler = false, applySimple = false;

    public static int generatePack(MinecraftServer server) {
        //create the filepath for our data pack.  this will do nothing if already created
        Path filepath = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(PACK_NAME);
        filepath.toFile().mkdirs();
        Path packPath = filepath.resolve("pack.mcmeta");
        try {
            Files.writeString(
                    packPath,
                    gson.toJson(getPackObject()),
                    Charset.defaultCharset(),
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            EpicFightIntegration.LOGGER.debug("Error While Generating pack.mcmeta for epicfight skill Compat Generated Data: " + e);
        }

        for (Category category : Category.values()) {
            Set<ResourceLocation> locations = category.valueList.apply(server);
            for (ResourceLocation id : locations) {
                int index = id.getPath().lastIndexOf('/');
                String pathRoute = id.getPath().substring(0, Math.max(index, 0));
                Path finalPath = filepath.resolve("data/" + EpicFightIntegration.MODID + "/" + category.route + "/" + pathRoute);
                finalPath.toFile().mkdirs();
                try {
                    Files.writeString(
                            finalPath.resolve(id.getPath().substring(id.getPath().lastIndexOf('/') + 1) + ".json"),
                            category.defaultData.apply(id),
                            Charset.defaultCharset(),
                            StandardOpenOption.CREATE_NEW,
                            StandardOpenOption.WRITE);
                } catch (IOException e) {
                    EpicFightIntegration.LOGGER.debug("Error While Generating Pack File For: " + id + " (" + e + ")");
                }
            }
        }

        return 0;
    }

    public static String prettyPrintJSON(String jsonString) {
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

    public static int getTemp(MinecraftServer server) {

        Map<ResourceLocation, CustomReqType> existing = SkillRequirements.TEMPLATES.getData();
        Map<ResourceLocation, SkillSettings> skillSettingsData = SkillRequirements.SKILL_SETTINGS.getData();
        SkillSettings s = SkillRequirements.SKILL_SETTINGS.getData(new ResourceLocation(EpicFightIntegration.MODID, "learn_able_skills/parrying"));
        EpicFightIntegration.LOGGER.debug(s.toString());
        skillSettingsData.keySet().forEach(resourceLocation -> EpicFightIntegration.LOGGER.debug(resourceLocation.getPath()));
//        server.getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal( ()->).toString())));
        existing.keySet().forEach(resourceLocation -> EpicFightIntegration.LOGGER.debug(resourceLocation.getPath()));
//        server.getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal(existing.toString())));
//        server.getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal(skillSettingsData.toString())));
        return 0;
    }

    private enum Category {
        LEARN_ABLE_SKILLS("skill_settings/learn_able_skills", server -> new HashSet<>(SkillManager.getSkills(skill -> skill.getCategory().learnable()).stream().map(Skill::getRegistryName).toList()), (id) -> {
            SkillSettings existing = SkillRequirements.SKILL_SETTINGS.getData(id);
            return gson.toJson(SkillSettings.CODEC.encodeStart(JsonOps.INSTANCE, applyDefaults ? existing : new SkillSettings()).result().get().getAsJsonObject());
        }), OTHER_SKILLS("skill_settings/other_skills", server -> new HashSet<>(SkillManager.getSkills(skill -> !skill.getCategory().learnable()).stream().map(Skill::getRegistryName).toList()), (id) -> {
            SkillSettings existing = SkillRequirements.SKILL_SETTINGS.getData(id);
            return gson.toJson(SkillSettings.CODEC.encodeStart(JsonOps.INSTANCE, applyDefaults ? existing : new SkillSettings()).result().get().getAsJsonObject());
        }), TEMPLATES("templates/skills", server -> {
            ResourceLocation resourceLocation = new ResourceLocation(EpicFightIntegration.MODID, "test");
            Set<ResourceLocation> temp = new HashSet<>();
            temp.add(resourceLocation);
            return temp;
        }, (id) -> {
            CustomReqType existing = SkillRequirements.TEMPLATES.getData(id);
            return gson.toJson(CustomReqType.CODEC.encodeStart(JsonOps.INSTANCE, applyDefaults ? existing : new CustomReqType()).result().get().getAsJsonObject());
        });
        public final String route;
        public final Function<MinecraftServer, Set<ResourceLocation>> valueList;
        private final Function<ResourceLocation, String> defaultData;

        Category(String route, Function<MinecraftServer, Set<ResourceLocation>> values, Function<ResourceLocation, String> defaultData) {
            this.route = route;
            this.valueList = values;
            this.defaultData = defaultData;
        }
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