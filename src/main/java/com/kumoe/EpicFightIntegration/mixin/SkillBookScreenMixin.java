package com.kumoe.EpicFightIntegration.mixin;

import com.kumoe.EpicFightIntegration.EFIMod;
import com.kumoe.EpicFightIntegration.api.EFISkillManager;
import com.kumoe.EpicFightIntegration.config.EFIConfig;
import com.llamalad7.mixinextras.sugar.Local;
import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

@Mixin(SkillBookScreen.class)
public abstract class SkillBookScreenMixin {

    @Shadow(remap = false)
    @Final
    protected LocalPlayerPatch playerpatch;

    @Inject(method = "init",
            at = @At(value = "INVOKE", target = "Lyesman/epicfight/client/gui/screen/SkillBookScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;")
    )
    private void mixinInit(CallbackInfo ci, @Local Button button) {
//        EFIMod.LOGGER.debug("button active: " + button.active);
//        EFIMod.LOGGER.debug("button visible: " + button.visible);
        boolean useMultiReqTypes = EFIConfig.CLIENT.useMultiReqTypes.get();


        if (useMultiReqTypes && EFISkillManager.dosePlayerMeetReqs(playerpatch, ReqType.ENTITY_INTERACT)) {
            button.active = false;
            button.visible = false;
        }
        EFIMod.LOGGER.debug("button active: " + button.active);
        EFIMod.LOGGER.debug("button visible: " + button.visible);
    }

}
