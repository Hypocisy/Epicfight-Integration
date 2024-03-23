package com.kumoe.EpicFightIntegration.mixin;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.util.CompactUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.mover.PhantomAscentSkill;
import yesman.epicfight.world.entity.eventlistener.MovementInputEvent;

import java.util.Map;

@Mixin(value = PhantomAscentSkill.class, remap = false)
public abstract class PhantomAscentSkillMixin {

    @Inject(method = "lambda$onInitiate$3", at = @At(value = "INVOKE", target = "Lyesman/epicfight/skill/SkillDataManager;getDataValue(Lyesman/epicfight/skill/SkillDataKey;)Ljava/lang/Object;", ordinal = 0, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onInitiateMixin(SkillContainer container, MovementInputEvent event, CallbackInfo ci, boolean jumpPressed) {
        int jumpCounter = container.getDataManager().getDataValue(SkillDataKeys.JUMP_COUNT.get());

        if (jumpCounter > 0 || event.getPlayerPatch().currentLivingMotion == LivingMotions.FALL) {
            Skill skill = container.getSkill();
            if (skill != null) {
                Map<String, Integer> conditions = CompactUtil.getConditions(event.getPlayerPatch(), CompactUtil.learnAble(skill.getRegistryName().getPath()));
                if (!conditions.isEmpty()) {
                    container.getDataManager().setData(SkillDataKeys.JUMP_COUNT.get(), 0);
                    if (event.getPlayerPatch().isLogicalClient()) {
                        container.getDataManager().setData(SkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK.get(), false);
                    }
                    EpicFightIntegration.LOGGER.debug(conditions.toString());
                }
            }
        }
    }

}
