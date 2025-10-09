package com.kumoe.EpicFightIntegration.event;


import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.config.EFIConfig;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.network.SkillLevelSyncPacket;
import com.kumoe.EpicFightIntegration.network.SkillRequirementSyncPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = EpicFightIntegration.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEvents {
	@SubscribeEvent
	public static void onModConfigLoad(net.neoforged.fml.event.config.ModConfigEvent event) {
		ModConfig config = event.getConfig();
		if (config.getSpec() == EpicFightIntegration.getInstance().getConfigSpec()) {
			EpicFightIntegration.LOGGER.debug("Loading EpicFightIntegration config");
			EFIConfig.bake();
		}
	}

	@SubscribeEvent
	private static void registerNetwork(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(EpicFightIntegration.MODID);
		registrar.playToClient(SkillRequirementSyncPacket.TYPE, SkillRequirementSyncPacket.STREAM_CODEC, SkillRequirementSyncPacket::handle);
		registrar.playToClient(SkillLevelSyncPacket.TYPE, SkillLevelSyncPacket.STREAM_CODEC, SkillLevelSyncPacket::handle);
		SkillRequirements.SKILL_SETTINGS.subscribeAsSyncable(SkillRequirementSyncPacket::new);
		SkillRequirements.TEMPLATES.subscribeAsSyncable(SkillLevelSyncPacket::new);
	}
}
