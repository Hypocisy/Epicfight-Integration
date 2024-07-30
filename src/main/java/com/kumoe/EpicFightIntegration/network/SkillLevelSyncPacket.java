package com.kumoe.EpicFightIntegration.network;

import com.kumoe.EpicFightIntegration.config.codecs.CustomReqType;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SkillLevelSyncPacket {
    private static final Codec<Map<ResourceLocation, CustomReqType>> MAPPER =
            Codec.unboundedMap(ResourceLocation.CODEC, CustomReqType.CODEC);
    public static Map<ResourceLocation, CustomReqType> SYNCED_DATA = new HashMap<>();

    private final Map<ResourceLocation, CustomReqType> map;

    public SkillLevelSyncPacket(Map<ResourceLocation, CustomReqType> map) {
        this.map = map;
    }

    public static SkillLevelSyncPacket decode(FriendlyByteBuf buffer) {
        return new SkillLevelSyncPacket(MAPPER.parse(NbtOps.INSTANCE, buffer.readNbt()).result().orElse(new HashMap<>()));
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeNbt((CompoundTag) (MAPPER.encodeStart(NbtOps.INSTANCE, this.map).result().orElse(new CompoundTag())));
    }

    public void onPacketReceived(Supplier<NetworkEvent.Context> contextGetter) {
        NetworkEvent.Context context = contextGetter.get();
        context.enqueueWork(() -> {
            Map<ResourceLocation, CustomReqType> map = SkillRequirements.TEMPLATES.getData();
            map.putAll(this.map);
        });

        context.enqueueWork(this::handlePacketOnMainThread);
        context.setPacketHandled(true);
    }

    private void handlePacketOnMainThread() {
        SYNCED_DATA = this.map;
    }
}