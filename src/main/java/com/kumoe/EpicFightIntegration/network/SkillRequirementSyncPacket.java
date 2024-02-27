package com.kumoe.EpicFightIntegration.network;

import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirement;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SkillRequirementSyncPacket {
    private static final Codec<Map<ResourceLocation, SkillRequirement>> MAPPER =
            Codec.unboundedMap(ResourceLocation.CODEC, SkillRequirement.CODEC);
    public static Map<ResourceLocation, SkillRequirement> SYNCED_DATA = new HashMap<>();

    private final Map<ResourceLocation, SkillRequirement> map;

    public SkillRequirementSyncPacket(Map<ResourceLocation, SkillRequirement> map) {
        this.map = map;
    }

    public static SkillRequirementSyncPacket decode(FriendlyByteBuf buffer) {
        return new SkillRequirementSyncPacket(MAPPER.parse(NbtOps.INSTANCE, buffer.readNbt()).result().orElse(new HashMap<>()));
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeNbt((CompoundTag) (MAPPER.encodeStart(NbtOps.INSTANCE, this.map).result().orElse(new CompoundTag())));
    }

    public void onPacketReceived(Supplier<NetworkEvent.Context> contextGetter) {
        NetworkEvent.Context context = contextGetter.get();
        context.enqueueWork(this::handlePacketOnMainThread);
        context.setPacketHandled(true);
    }

    private void handlePacketOnMainThread() {
        SYNCED_DATA = this.map;
    }
}