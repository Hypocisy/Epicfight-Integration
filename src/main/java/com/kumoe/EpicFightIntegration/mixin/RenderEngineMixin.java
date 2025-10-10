package com.kumoe.EpicFightIntegration.mixin;

import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.checkerframework.checker.units.qual.C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

@Mixin(value = RenderEngine.class, remap = false)
public abstract class RenderEngineMixin {
	@Inject(method = "lambda$epicfight$itemTooltip$7", at = @At(value = "INVOKE", remap = false, target = "Ljava/util/Iterator;hasNext()Z"))
	private static void epicfight$itemTooltip(LocalPlayerPatch playerPatch, ItemTooltipEvent event, CapabilityItem itemCapability, CallbackInfo ci) {
	
	}
}
