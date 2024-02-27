package com.kumoe.EpicFightIntegration.mixin;

import com.kumoe.EpicFightIntegration.EFIMod;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirement;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.util.CompactUtil;
import com.llamalad7.mixinextras.sugar.Local;
import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.main.EpicFightMod;

import java.util.Map;

@Mixin(SkillBookScreen.class)
public abstract class SkillBookScreenMixin {

    @Shadow(remap = false)
    @Final
    protected LocalPlayerPatch playerpatch;

    @Inject(method = "init",
            at = @At(value = "INVOKE", target = "Lyesman/epicfight/client/gui/screen/SkillBookScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", args = "")
    )
    private void mixinInit(CallbackInfo ci, @Local Button button) {
        if (!CompactUtil.dosePlayerMeetReqs(playerpatch, ReqType.USE)) {
            button.active = false;
            button.visible = false;
        }
        Map<ResourceLocation, SkillRequirement> s = SkillRequirements.DATA_LOADER.getData();
        
        EFIMod.LOGGER.debug("map s: " + s.toString());

        EFIMod.LOGGER.debug("button active: " + button.active);
        EFIMod.LOGGER.debug("button visible: " + button.visible);
    }

}
