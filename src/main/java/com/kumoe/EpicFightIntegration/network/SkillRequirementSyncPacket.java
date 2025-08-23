package com.kumoe.EpicFightIntegration.network;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.config.codecs.SkillSettings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record SkillRequirementSyncPacket(Map<ResourceLocation, SkillSettings> skillSettingsMap) implements CustomPacketPayload {

	public static final Type<SkillRequirementSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EpicFightIntegration.MODID, "sync_skill_req"));
	public static final StreamCodec<FriendlyByteBuf, SkillRequirementSyncPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.map(
					HashMap::new,
					ByteBufCodecs.fromCodec(ResourceLocation.CODEC),
					ByteBufCodecs.fromCodec(SkillSettings.codec())
			),
			SkillRequirementSyncPacket::skillSettingsMap,    // getter function
			SkillRequirementSyncPacket::new    // factory function
	);

	public static Map<ResourceLocation, SkillSettings> SYNCED_DATA;

	public static void handle(final SkillRequirementSyncPacket data, final IPayloadContext context) {
		context.enqueueWork(() -> {
			Map<ResourceLocation, SkillSettings> map = SkillRequirements.SKILL_SETTINGS.getData();
			map.putAll(data.skillSettingsMap);
		});
		context.enqueueWork(data::handlePacketOnMainThread);
	}

	private void handlePacketOnMainThread() {
		SYNCED_DATA = this.skillSettingsMap;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}