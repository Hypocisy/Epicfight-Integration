package com.kumoe.EpicFightIntegration.config;

import com.kumoe.EpicFightIntegration.EFIMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.Logging;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

public class EFIConfig {
    public static final EFIConfig.Client CLIENT;
    public static final EFIConfig.Common COMMON;
    public static final EFIConfig.Server SERVER;
    public static final ForgeConfigSpec clientSpec;
    public static final ForgeConfigSpec commonSpec;
    public static final ForgeConfigSpec serverSpec;

    static {
        Pair<Client, ForgeConfigSpec> clientSpecPair = (new ForgeConfigSpec.Builder()).configure(Client::new);
        clientSpec = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();
        var commonSpecPair = (new ForgeConfigSpec.Builder()).configure(Common::new);
        commonSpec = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();
        var serverSpecPair = (new ForgeConfigSpec.Builder()).configure(Server::new);
        serverSpec = serverSpecPair.getRight();
        SERVER = serverSpecPair.getLeft();
    }

    public EFIConfig() {
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading configEvent) {
        LogManager.getLogger().debug(Logging.FORGEMOD, "Loaded forge config file {}", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(ModConfigEvent.Reloading configEvent) {
        LogManager.getLogger().debug(Logging.FORGEMOD, "Forge config just got changed on the file system!");
    }

    public static class Client {
        public final ForgeConfigSpec.BooleanValue useMultiReqTypes;

        Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Client only settings, mostly things related to rendering").push("client");
            this.useMultiReqTypes = builder.comment("Whether or not you use more ReqTypes allows your EpicFight skill book to fulfill one or more types of requirements in order to learn it", ", and also affects EpicFight's skill release.").translation(EFIMod.MODID + ".config.use_multi_req_types").define("useMultiReqTypes", true);
            builder.pop();
        }
    }

    public static class Common {
        Common(ForgeConfigSpec.Builder builder) {
            builder.comment("[DEPRECATED / NO EFFECT]: General configuration settings").push("general");
            builder.pop();
        }
    }

    public static class Server {
//        public final ForgeConfigSpec.BooleanValue removeErroringBlockEntities;
//        public final ForgeConfigSpec.BooleanValue removeErroringEntities;
//        public final ForgeConfigSpec.BooleanValue fullBoundingBoxLadders;
//        public final ForgeConfigSpec.DoubleValue zombieBaseSummonChance;
//        public final ForgeConfigSpec.DoubleValue zombieBabyChance;
//        public final ForgeConfigSpec.ConfigValue<String> permissionHandler;
//        public final ForgeConfigSpec.BooleanValue advertiseDedicatedServerToLan;

        Server(ForgeConfigSpec.Builder builder) {
//            builder.comment("Server configuration settings").push("server");
//            this.removeErroringBlockEntities = builder.comment("Set this to true to remove any BlockEntity that throws an error in its update method instead of closing the server and reporting a crash log. BE WARNED THIS COULD SCREW UP EVERYTHING USE SPARINGLY WE ARE NOT RESPONSIBLE FOR DAMAGES.").translation("forge.configgui.removeErroringBlockEntities").worldRestart().define("removeErroringBlockEntities", false);
//            this.removeErroringEntities = builder.comment("Set this to true to remove any Entity (Note: Does not include BlockEntities) that throws an error in its tick method instead of closing the server and reporting a crash log. BE WARNED THIS COULD SCREW UP EVERYTHING USE SPARINGLY WE ARE NOT RESPONSIBLE FOR DAMAGES.").translation("forge.configgui.removeErroringEntities").worldRestart().define("removeErroringEntities", false);
//            this.fullBoundingBoxLadders = builder.comment("Set this to true to check the entire entity's collision bounding box for ladders instead of just the block they are in. Causes noticeable differences in mechanics so default is vanilla behavior. Default: false.").translation("forge.configgui.fullBoundingBoxLadders").worldRestart().define("fullBoundingBoxLadders", false);
//            this.zombieBaseSummonChance = builder.comment("Base zombie summoning spawn chance. Allows changing the bonus zombie summoning mechanic.").translation("forge.configgui.zombieBaseSummonChance").worldRestart().defineInRange("zombieBaseSummonChance", 0.1, 0.0, 1.0);
//            this.zombieBabyChance = builder.comment("Chance that a zombie (or subclass) is a baby. Allows changing the zombie spawning mechanic.").translation("forge.configgui.zombieBabyChance").worldRestart().defineInRange("zombieBabyChance", 0.05, 0.0, 1.0);
//            this.permissionHandler = builder.comment("The permission handler used by the server. Defaults to forge:default_handler if no such handler with that name is registered.").translation("forge.configgui.permissionHandler").define("permissionHandler", "forge:default_handler");
//            this.advertiseDedicatedServerToLan = builder.comment("Set this to true to enable advertising the dedicated server to local LAN clients so that it shows up in the Multiplayer screen automatically.").translation("forge.configgui.advertiseDedicatedServerToLan").define("advertiseDedicatedServerToLan", true);
//            builder.pop();
        }
    }
}
