package com.kumoe.EpicFightIntegration.mixin;

import com.kumoe.EpicFightIntegration.util.CompactUtil;
import com.llamalad7.mixinextras.sugar.Local;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.Skill;

import java.util.Iterator;
import java.util.Map;

@Mixin(value = RenderEngine.Events.class, remap = false)
public abstract class RenderEngineMixin {
    @Inject(method = "itemTooltip", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z"))
    private static void onToolTip(ItemTooltipEvent event, CallbackInfo ci, @Local(name = "var5") Iterator<?> var5, @Local(name = "weaponInnateSkill") Skill skill, @Local(name = "playerpatch") LocalPlayerPatch playerPatch) {
        StringBuilder format = new StringBuilder();
        if (playerPatch != null) {
            var customConditions = CompactUtil.getSkillCondition(playerPatch, CompactUtil.learnable(skill.getRegistryName().getPath()));
            var pmmo_conditions = CompactUtil.getRemainMapOrEmpty(playerPatch, CompactUtil.getItemCondition(event.getItemStack(), ReqType.WEAPON));
            customConditions.putAll(pmmo_conditions);
            if (!customConditions.isEmpty()) {
                String formatter = "%s: %s\n";
                for (Map.Entry<String, Integer> entry : customConditions.entrySet()) {
                    String skillName = entry.getKey();
                    Integer level = entry.getValue();
                    format.append(formatter.formatted(LangProvider.skill(skillName).getString(), level));
                }
                if (!var5.hasNext()) {
                    event.getToolTip().add(Component.translatable("pmmo.msg.denial.skill", format.toString()).withStyle(ChatFormatting.DARK_GREEN));
                }
            }

        }
    }
}