package com.kumoe.EpicFightIntegration.mixin;

import com.kumoe.EpicFightIntegration.util.CompactUtil;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

@Mixin(value = ControllEngine.class, remap = false)
public abstract class ControllEngineMixin {

    @Shadow
    private LocalPlayerPatch playerpatch;
    @Shadow
    private boolean weaponInnatePressToggle;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private int weaponInnatePressCounter;
    @Shadow
    private boolean attackLightPressToggle;

    @Shadow
    protected abstract void releaseAllServedKeys();

    @Inject(method = "tick()V", at = @At(value = "HEAD"))
    private void tickMixin(CallbackInfo ci) {

        if (ControllEngine.isKeyDown(EpicFightKeyMappings.WEAPON_INNATE_SKILL) && this.minecraft.screen == null
                && this.playerpatch.isBattleMode() && this.weaponInnatePressToggle) {
            boolean isMatchConditions = CompactUtil.isPlayerMatchConditions(this.playerpatch);
            if (!isMatchConditions) {
                this.attackLightPressToggle = false;
                this.weaponInnatePressToggle = true;
                this.weaponInnatePressCounter = 0;
                releaseAllServedKeys();
            }
        }
    }

    /*@Inject(method = "moverKeyPressed(Lnet/minecraft/client/KeyMapping;I)Z", at = @At(value = "HEAD"), cancellable = true)
    private void moverKeyPressedMixin(KeyMapping key, int action, CallbackInfoReturnable<Boolean> cir) {
        if (ControllEngine.isKeyDown(EpicFightKeyMappings.MOVER_SKILL) && action == 1 && this.playerpatch.isBattleMode()) {
            SkillContainer skillContainer = this.playerpatch.getSkill(SkillSlots.MOVER);
            if (skillContainer != null && skillContainer.getSkill() != null) {
                Map<String, Integer> conditions = CompactUtil.getConditions(this.playerpatch, CompactUtil.learnable(skillContainer.getSkill().getRegistryName().getPath()));
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
