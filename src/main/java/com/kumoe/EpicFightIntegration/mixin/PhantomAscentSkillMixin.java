package com.kumoe.EpicFightIntegration.mixin;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.config.EFIConfig;
import com.kumoe.EpicFightIntegration.util.CompactUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.mover.PhantomAscentSkill;
import yesman.epicfight.world.entity.eventlistener.MovementInputEvent;

import java.util.Map;

@Mixin(value = PhantomAscentSkill.class, remap = false)
public abstract class PhantomAscentSkillMixin {

    @Inject(method = "lambda$onInitiate$3", at = @At(value = "INVOKE", target = "Lyesman/epicfight/skill/SkillDataManager;getDataValue(Lyesman/epicfight/skill/SkillDataKey;)Ljava/lang/Object;", ordinal = 0, shift = At.Shift.AFTER))
    private void onInitiateMixin(SkillContainer container, MovementInputEvent event, CallbackInfo ci) {
        int jumpCounter = container.getDataManager().getDataValue(SkillDataKeys.JUMP_COUNT.get());
        var playerpatch = event.getPlayerPatch();
        if (jumpCounter > 0 || playerpatch.currentLivingMotion == LivingMotions.FALL) {
            Skill skill = container.getSkill();
            if (skill != null) {
                Map<String, Integer> conditions = CompactUtil.getSkillCondition(playerpatch, CompactUtil.learnable(skill.getRegistryName().getPath()));
                if (!conditions.isEmpty()) {
                    container.getDataManager().setData(SkillDataKeys.JUMP_COUNT.get(), 0);
                    if (playerpatch.isLogicalClient()) {
                        container.getDataManager().setData(SkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK.get(), false);
                    }
                    CompactUtil.displayMessage(Component.translatable("pmmo.msg.denial.skill", conditions).withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), playerpatch.getOriginal());
                    if (EFIConfig.enableDebug) {
                        EpicFightIntegration.LOGGER.debug(conditions.toString());
                    }
                }
            }
        }
    }

}
