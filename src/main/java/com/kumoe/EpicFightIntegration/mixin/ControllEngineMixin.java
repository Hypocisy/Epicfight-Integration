package com.kumoe.EpicFightIntegration.mixin;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.util.CompactUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.entity.eventlistener.SkillExecuteEvent;

import java.util.Map;

@Mixin(value = ControllEngine.class, remap = false)
public abstract class ControllEngineMixin {

    @Shadow
    private LocalPlayerPatch playerpatch;
    @Shadow
    private boolean moverPressToggle;

    @Shadow
    protected abstract void releaseAllServedKeys();

    @Inject(method = "tick()V", at = @At(value = "HEAD"), cancellable = true)
    private void tickMixin(CallbackInfo ci) {

        if (this.playerpatch.isBattleMode()) {
            SkillContainer skillContainer = this.playerpatch.getSkill(SkillSlots.MOVER);
            if (skillContainer != null && !skillContainer.isEmpty() && ControllEngine.isKeyDown(EpicFightKeyMappings.MOVER_SKILL)) {
                Map<String, Integer> conditions = CompactUtil.getSkillCondition(this.playerpatch, CompactUtil.learnable(skillContainer.getSkill().getRegistryName().getPath()));
                EpicFightIntegration.LOGGER.debug("You need pmmo level of {}", conditions);
                SkillExecuteEvent event = new SkillExecuteEvent(this.playerpatch, skillContainer);

                if (skillContainer.canExecute(playerpatch, event) && this.playerpatch.getOriginal().getVehicle() == null) {
                    // if player have conditions
                    if (!conditions.isEmpty()) {
                        CompactUtil.displayMessage(Component.translatable("pmmo.msg.denial.skill", conditions.toString()).withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.YELLOW)), playerpatch.getOriginal());
                        this.releaseAllServedKeys();
                        playerpatch.cancelAnyAction();
                        this.moverPressToggle = false;
                        ci.cancel();
                    }
                }
            }
        }
    }


    /*@Inject(method = "moverKeyPressed(Lnet/minecraft/client/KeyMapping;I)Z", at = @At(value = "HEAD"), cancellable = true)
    private void moverKeyPressedMixin(KeyMapping key, int action, CallbackInfoReturnable<Boolean> cir) {
        if (ControllEngine.isKeyDown(EpicFightKeyMappings.MOVER_SKILL) && action == 1 && this.playerpatch.isBattleMode()) {
            SkillContainer skillContainer = this.playerpatch.getSkill(SkillSlots.MOVER);
            if (skillContainer != null && skillContainer.getSkill() != null) {
                Map<String, Integer> conditions = CompactUtil.getSkillCondition(this.playerpatch, CompactUtil.learnable(skillContainer.getSkill().getRegistryName().getPath()));
                EpicFightIntegration.LOGGER.debug("You need pmmo level of {}", conditions);
                SkillExecuteEvent event = new SkillExecuteEvent(this.playerpatch, skillContainer);

                if (skillContainer.canExecute(playerpatch, event) && this.playerpatch.getOriginal().getVehicle() == null) {
                    // if player have conditions
                    if (!conditions.isEmpty()) {
                        CompactUtil.displayMessage(Component.translatable("pmmo.msg.denial.skill", conditions.toString()).withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.YELLOW)), playerpatch.getOriginal());
                        playerpatch.getOriginal().playSound(SoundEvents.ANVIL_FALL);
                        this.releaseAllServedKeys();
                        cir.setReturnValue(false);
                    }
                }

            }
        }
    }*/
}
