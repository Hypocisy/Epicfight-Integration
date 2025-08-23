package com.kumoe.EpicFightIntegration.network;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.config.codecs.CustomReqType;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record SkillLevelSyncPacket(Map<ResourceLocation, CustomReqType> reqTypeMap) implements CustomPacketPayload {
	public static final Type<SkillLevelSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EpicFightIntegration.MODID, "sync_skill_level"));

	public static final StreamCodec<FriendlyByteBuf, SkillLevelSyncPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.map(
					HashMap::new,
					ByteBufCodecs.fromCodec(ResourceLocation.CODEC),
					ByteBufCodecs.fromCodec(CustomReqType.codec())
			),
			SkillLevelSyncPacket::reqTypeMap,    // getter function
			SkillLevelSyncPacket::new    // factory function
	);

	public static Map<ResourceLocation, CustomReqType> SYNCED_DATA = new HashMap<>();

	public static void handle(final SkillLevelSyncPacket data, final IPayloadContext context) {
		context.enqueueWork(() -> {
			Map<ResourceLocation, CustomReqType> map = SkillRequirements.TEMPLATES.getData();
			map.putAll(data.reqTypeMap);
		});

		context.enqueueWork(data::handlePacketOnMainThread);
	}

	private void handlePacketOnMainThread() {
		SYNCED_DATA = this.reqTypeMap;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}