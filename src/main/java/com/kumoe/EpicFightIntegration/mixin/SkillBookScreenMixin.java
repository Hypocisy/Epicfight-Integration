package com.kumoe.EpicFightIntegration.mixin;

import com.kumoe.EpicFightIntegration.util.CompactUtil;
import com.kumoe.EpicFightIntegration.util.SkillResult;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = SkillBookScreen.class)
public abstract class SkillBookScreenMixin {

    @Shadow(remap = false)
    @Final
    protected Skill skill;


    @Inject(method = "init()V",
            at = @At(value = "INVOKE", target = "Lyesman/epicfight/client/gui/screen/SkillBookScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"), locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void mixinInit(CallbackInfo ci, SkillContainer thisSkill, SkillContainer priorSkill, boolean isUsing, boolean condition, Component tooltip, Button changeButton) {
        PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(Minecraft.getInstance().player, PlayerPatch.class);

        SkillResult skillResult;
        if (playerpatch != null) {
            skillResult = CompactUtil.getSkillResult(playerpatch, skill.getRegistryName().getPath());
            if (!skillResult.getResult()) {
                changeButton.active = false;
//                changeButton.visible = false;
                List<String> tooltipmsg = new ArrayList<>();

                skillResult.getNoMeetReqs().forEach((reqType, reqs) -> {
                    if (!reqs.isEmpty()) {
                        reqs.forEach((name, level) -> tooltipmsg.add("you need %s type's skill to learn this: (%s: %s)".formatted(reqType, LangProvider.skill(name).getString(), level)));
                    }
                });
                changeButton.setTooltip(Tooltip.create(Component.nullToEmpty(tooltipmsg.toString())));

            }
        }

    }


}
