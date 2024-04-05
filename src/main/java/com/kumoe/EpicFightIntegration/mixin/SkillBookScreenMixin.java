package com.kumoe.EpicFightIntegration.mixin;

import com.kumoe.EpicFightIntegration.util.CompactUtil;
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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.Map;

@Mixin(value = SkillBookScreen.class)
public abstract class SkillBookScreenMixin {

    @Shadow(remap = false)
    @Final
    protected Skill skill;
    @Unique
    Map<String, Integer> eFIMod$skillResult;

    @Inject(method = "init()V", at = @At(value = "INVOKE", target = "Lyesman/epicfight/client/gui/screen/SkillBookScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void mixinInit(CallbackInfo ci, SkillContainer thisSkill, SkillContainer priorSkill, boolean isUsing, boolean condition, Component tooltip, Button changeButton) {
        PlayerPatch<?> playerpatch = ClientEngine.getInstance().getPlayerPatch();
        StringBuilder format = new StringBuilder();
        if (playerpatch != null && condition) {
            eFIMod$skillResult = CompactUtil.getConditions(playerpatch, CompactUtil.learnAble(skill.getRegistryName().getPath()));
            if (!eFIMod$skillResult.isEmpty()) {
                changeButton.active = false;
                String formatter = "%s: %s\n";
                for (Map.Entry<String, Integer> entry : eFIMod$skillResult.entrySet()) {
                    String skillName = entry.getKey();
                    Integer level = entry.getValue();
                    format.append(formatter.formatted(LangProvider.skill(skillName).getString(), level));
                }

                tooltip = Component.translatable("pmmo.msg.denial.skill", format.toString()).withStyle(ChatFormatting.DARK_GREEN);

                changeButton.setTooltip(Tooltip.create(tooltip));
            }
        }
    }
}
