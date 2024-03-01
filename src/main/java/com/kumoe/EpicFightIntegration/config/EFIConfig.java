package com.kumoe.EpicFightIntegration.config;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
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
            this.useMultiReqTypes = builder.comment("Whether or not you use more ReqTypes allows your EpicFight skill book to fulfill one or more types of requirements in order to learn it", ", and also affects EpicFight's skill release.").translation(EpicFightIntegration.MODID + ".config.use_multi_req_types").define("useMultiReqTypes", true);
            builder.pop();
        }
    }

    public static class Common {
        Common(ForgeConfigSpec.Builder builder) {
//            builder.pop();
        }
    }

    public static class Server {
        Server(ForgeConfigSpec.Builder builder) {

        }
    }
}
