package com.kumoe.EpicFightIntegration.mixin;

import com.kumoe.EpicFightIntegration.util.CompactUtil;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.Map;

@Mixin(value = SkillBookScreen.class, remap = false)
public abstract class SkillBookScreenMixin {

	@Shadow(remap = false)
	@Final
	protected Skill skill;
	@Unique
	Map<String, Long> eFIMod$skillResult;


	@Definition(id = "addRenderableWidget", method = "addRenderableWidget")
//	@Definition(id = "learnButton", local = @Local(name = "learnButton"))
	@Expression("this.addRenderableWidget(?)")
	@Inject(method = "init()V", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
	private void addSkillRequirementToolTip(CallbackInfo ci, @Local(name = "condition") boolean condition, @Local(name = "learnButton") Button learnButton) {
		PlayerPatch<?> playerpatch = ClientEngine.getInstance().getPlayerPatch();
		StringBuilder format = new StringBuilder();
		if (playerpatch != null && condition) {
			eFIMod$skillResult = CompactUtil.getSkillCondition(playerpatch.getOriginal(), CompactUtil.learnable(skill.getRegistryName().getPath()));
			if (!eFIMod$skillResult.isEmpty()) {
				learnButton.active = false;
				String formatter = "%s: %s";
				for (Map.Entry<String, Long> entry : eFIMod$skillResult.entrySet()) {
					String skillName = entry.getKey();
					Long level = entry.getValue();
					format.append(formatter.formatted(LangProvider.skill(skillName).getString(), level));
				}

				Component tooltip = Component.translatable("inventory.epicfight.guide_innate_tooltip.required_skill", format.toString()).withStyle(ChatFormatting.DARK_GREEN);

				learnButton.setTooltip(Tooltip.create(tooltip));
				learnButton.setMessage(Component.translatable("gui.epicfight.unusable"));
			}
		}
	}
}
