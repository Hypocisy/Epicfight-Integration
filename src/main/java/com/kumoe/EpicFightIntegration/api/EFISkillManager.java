package com.kumoe.EpicFightIntegration.api;

import com.kumoe.EpicFightIntegration.EFIMod;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.fml.LogicalSide;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;


public class EFISkillManager {
    public EFISkillManager() {
    }

    public static boolean dosePlayerMeetReqs(PlayerPatch<?> caster, ReqType reqType) {
        var reqs = APIUtils.getRequirementMap(caster.getValidItemInHand(InteractionHand.MAIN_HAND), reqType, LogicalSide.CLIENT);
        EFIMod.LOGGER.debug(reqs.toString());
        return Core.get(LogicalSide.CLIENT).doesPlayerMeetReq(caster.getOriginal().getUUID(), reqs);
    }
}
