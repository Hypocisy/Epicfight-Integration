package com.kumoe.EpicFightIntegration.mixin;

import com.kumoe.EpicFightIntegration.util.CompactUtil;
import com.llamalad7.mixinextras.sugar.Local;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.checkerframework.checker.units.qual.C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.Iterator;
import java.util.Map;

@Mixin(value = RenderEngine.class, remap = false)
public abstract class RenderEngineMixin {
	@Inject(method = "lambda$epicfight$itemTooltip$7", at = @At(value = "INVOKE", remap = false, target = "Ljava/util/Iterator;hasNext()Z"))
	private static void epicfight$itemTooltip(LocalPlayerPatch playerpatch, ItemTooltipEvent event, CapabilityItem itemCapability, CallbackInfo ci, @Local Skill weaponInnateSkill, @Local Iterator<?> var5) {
		StringBuilder format = new StringBuilder();
		if (playerpatch != null) {
			var customConditions = CompactUtil.getSkillCondition(playerpatch.getOriginal(), CompactUtil.learnable(weaponInnateSkill.getRegistryName().getPath()));
			var pmmo_conditions = CompactUtil.getRemainMapOrEmpty(APIUtils.getAllLevels(playerpatch.getOriginal()), CompactUtil.getItemCondition(event.getItemStack(), event.getContext().level(), ReqType.WEAPON));
			customConditions.putAll(pmmo_conditions);
			if (!customConditions.isEmpty()) {
				String formatter = "%s: %s";
				for (Map.Entry<String, Long> entry : customConditions.entrySet()) {
					String skillName = entry.getKey();
					Long level = entry.getValue();
					format.append(formatter.formatted(LangProvider.skill(skillName).getString(), level));
				}
				if (!var5.hasNext()) {
					event.getToolTip().add(Component.translatable("pmmo.msg.denial.skill", format.toString()).withStyle(ChatFormatting.DARK_GREEN));
				}
			}
			
		}
	}
}
