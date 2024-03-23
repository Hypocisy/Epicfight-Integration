package com.kumoe.EpicFightIntegration.mixin;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.util.CompactUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.SkillExecuteEvent;

import java.util.Map;

@Mixin(value = ControllEngine.class, remap = false)
public abstract class ControllEngineMixin {

    @Shadow
    private boolean weaponInnatePressToggle;

    @Shadow
    private LocalPlayerPatch playerpatch;
    @Shadow
    private int weaponInnatePressCounter;
    @Shadow
    private boolean attackLightPressToggle;
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract boolean isKeyDown(KeyMapping key);

    @Shadow
    protected abstract void releaseAllServedKeys();

    @Inject(method = "tick()V", at = @At(value = "HEAD"))
    private void tickMixin(CallbackInfo ci) {
        if (this.isKeyDown(EpicFightKeyMappings.WEAPON_INNATE_SKILL) && this.minecraft.screen == null && this.playerpatch.isBattleMode()) {
            eFIMod$processWeaponSkill();
        }
    }

    @Inject(method = "moverKeyPressed(Lnet/minecraft/client/KeyMapping;I)Z", at = @At(value = "HEAD"), cancellable = true)
    private void moverKeyPressedMixin(KeyMapping key, int action, CallbackInfoReturnable<Boolean> cir) {
        if (this.isKeyDown(EpicFightKeyMappings.MOVER_SKILL) && action == 1 && this.playerpatch.isBattleMode()) {
            SkillContainer skillContainer = this.playerpatch.getSkill(SkillSlots.MOVER);
            if (skillContainer != null && skillContainer.getSkill() != null) {
                Map<String, Integer> conditions = CompactUtil.getConditions(this.playerpatch, CompactUtil.learnAble(skillContainer.getSkill().getRegistryName().getPath()));
                EpicFightIntegration.LOGGER.debug("You need pmmo level of " + conditions.toString());
                SkillExecuteEvent event = new SkillExecuteEvent(this.playerpatch, skillContainer);

                if (skillContainer.canExecute(playerpatch, event) && this.playerpatch.getOriginal().getVehicle() == null) {
                    if (!conditions.isEmpty()) {
                        CompactUtil.send(Component.translatable("pmmo.msg.denial.skill", conditions.toString()).withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.RED)), playerpatch.getOriginal());
                        playerpatch.getOriginal().playSound(SoundEvents.ANVIL_FALL);
                        this.releaseAllServedKeys();
                        cir.setReturnValue(false);
                    }
                }

            }
        }
    }

    @Unique
    private void eFIMod$processWeaponSkill() {
        ItemStack itemStack = CompactUtil.getValidItem(playerpatch);
        CapabilityItem item = EpicFightCapabilities.getItemStackCapability(itemStack);
        Skill innateSkill = item.getInnateSkill(this.playerpatch, itemStack);
        if (innateSkill != null) {
            Map<String, Integer> conditions = CompactUtil.getConditions(this.playerpatch, CompactUtil.rl(innateSkill.getRegistryName().getPath()));
            EpicFightIntegration.LOGGER.debug("skill path: " + innateSkill.getRegistryName().getPath());
            EpicFightIntegration.LOGGER.debug("conditions: " + conditions);
            if (!conditions.isEmpty() && this.weaponInnatePressToggle) {
                CompactUtil.send(Component.translatable("pmmo.msg.denial.skill", conditions.toString()).withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.AQUA)), playerpatch.getOriginal());
                this.attackLightPressToggle = true;
                this.weaponInnatePressToggle = false;
                this.weaponInnatePressCounter = 0;
                this.releaseAllServedKeys();
            }
        }
    }


}
