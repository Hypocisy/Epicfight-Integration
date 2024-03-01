package com.kumoe.EpicFightIntegration.mixin;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.util.CompactUtil;
import com.kumoe.EpicFightIntegration.util.SkillResult;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.Skill;

@Mixin(ControllEngine.class)
public abstract class ControlEngineMixin {
    @Shadow(remap = false)
    private LocalPlayerPatch playerpatch;

    @Shadow(remap = false)
    private boolean attackLightPressToggle;

    @Shadow(remap = false)
    private int weaponInnatePressCounter;

    @Shadow(remap = false)
    private boolean weaponInnatePressToggle;

    @Shadow(remap = false)
    private int moverPressCounter;

    @Shadow(remap = false)
    private boolean moverPressToggle;

//    @Inject(at = @At(value = "HEAD"), method = "attackKeyPressed", remap = false, cancellable = true)
//    private void mixinAttackKeyPressed(@NotNull KeyMapping key, int action, @NotNull CallbackInfoReturnable<Boolean> cir) {
//        if (playerpatch.getOriginal() != null) {
//            playerpatch.getOriginal().getMainHandItem();
//        }
//    }

    @Shadow(remap = false)
    protected abstract void releaseAllServedKeys();

    @Inject(at = @At(value = "HEAD"), method = "moverKeyPressed", remap = false, cancellable = true)
    private void mixinMoverKeyPressed(@NotNull KeyMapping key, int action, @NotNull CallbackInfoReturnable<Boolean> cir) {
        if (playerpatch.isLogicalClient()) {
            if (playerpatch.isChargingSkill()) {
                Skill skill = playerpatch.getChargingSkill().asSkill();

                SkillResult skillResult = CompactUtil.getSkillResult(playerpatch, skill.getRegistryName().getPath());
//                playerpatch.getOriginal().sendSystemMessage(Component.literal("dosePlayerMeetReqs: " + skillResult.getResult()));
//                playerpatch.getOriginal().sendSystemMessage(Component.literal(skill.getRegistryName().toString()));
                if (!skillResult.getResult()) {
                    // 客户端通知服务器, 已经取消技能
                    EpicFightNetworkManager.sendToServer(SPSkillExecutionFeedback.expired(playerpatch.getSkill(skill).getSlotId()));
                    // 客户端取消技能释放，取消使用物品以及物品充能
                    playerpatch.cancelAnyAction();
                    moverPressToggle = false;
                    moverPressCounter = 0;
                    releaseAllServedKeys();
                    playerpatch.getClientAnimator().resetLivingAnimations();
                    EpicFightIntegration.LOGGER.debug("cancel mover skill"+skillResult.getNoMeetReqs());
                    cir.setReturnValue(false);
                }
            }
        }
    }


    @Inject(at = @At(value = "HEAD"), method = "weaponInnateSkillKeyPressed", remap = false, cancellable = true)
    private void mixinWeaponInnateSkillKeyPressed(@NotNull KeyMapping key, int action,
                                                  @NotNull CallbackInfoReturnable<Boolean> cir) {
        if (playerpatch.isLogicalClient()) {
            if (playerpatch.isChargingSkill()) {
                Skill skill = playerpatch.getChargingSkill().asSkill();
                SkillResult skillResult = CompactUtil.getSkillResult(playerpatch, skill.getRegistryName().getPath());
                EpicFightIntegration.LOGGER.debug("dosePlayerMeetReqs: " + skillResult.getResult());
                if (!skillResult.getResult()) {

                    // 客户端取消技能释放，取消使用物品以及物品充能
                    // 客户端通知服务器, 已经取消技能
                    // EpicFightNetworkManager.sendToServer(SPSkillExecutionFeedback.expired(playerpatch.getSkill(skill).getSlotId()));
                    skill.cancelOnClient(playerpatch, null);
                    playerpatch.cancelAnyAction();
                    attackLightPressToggle = true;
                    weaponInnatePressCounter = 0;
                    weaponInnatePressToggle = false;
                    EpicFightIntegration.LOGGER.debug("cancel weapon innate skill");
                    cir.setReturnValue(false);
                }
            }
        }
//        else {
//            ServerPlayer serverPlayer = playerpatch.getOriginal().getServer().getPlayerList().getPlayer(playerpatch.getOriginal().getUUID());
//            ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getEntityPatch(serverPlayer, ServerPlayerPatch.class);
//            if (serverPlayerPatch != null && serverPlayerPatch.isChargingSkill()) {
//                ChargeableSkill skill = playerpatch.getChargingSkill();
//                skill.asSkill().cancelOnServer(serverPlayerPatch, null);
//                attackLightPressToggle = true;
//                weaponInnatePressCounter = 0;
//                weaponInnatePressToggle = false;
//                serverPlayerPatch.cancelAnyAction();
//            }
//
//        }
    }
}
