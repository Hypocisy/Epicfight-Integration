package com.kumoe.EpicFightIntegration.event;

import com.kumoe.EpicFightIntegration.EFIMod;
import com.kumoe.EpicFightIntegration.commands.CmdPmmoSkillBooksRoot;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.util.CompactUtil;
import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

@Mod.EventBusSubscriber(modid = EFIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EFIEventHandler {
    @SubscribeEvent
    public static void itemUseStartEvent(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() != null) {
            Player player = (Player) event.getEntity();
            PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), PlayerPatch.class);

            if (playerpatch == null) {
                return;
            }
//            playerpatch.getOriginal().sendSystemMessage(Component.literal("does player meet req: " + EFISkillManager.dosePlayerMeetReqs(hand, playerpatch, ReqType.WEAPON)));
            if (!CompactUtil.dosePlayerMeetReqs(playerpatch, ReqType.WEAPON)) {
                playerpatch.getEntityState().setState(EntityState.CAN_SKILL_EXECUTION, false);
                playerpatch.getOriginal().sendSystemMessage(Component.literal("你不满足使用技能的条件"));
                playerpatch.cancelAnyAction();
                event.setCanceled(true);
            }

        }
    }
    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        CmdPmmoSkillBooksRoot.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event)
    {
        event.addListener(SkillRequirements.DATA_LOADER);
    }

    @SubscribeEvent
    public static void rightClickItemEvent(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() != null) {
            Player player = event.getEntity();
            ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), ServerPlayerPatch.class);

            if (playerpatch == null) {
                return;
            }

            ItemStack stack = player.getItemInHand(event.getHand());
            if (stack.getTag() == null || !stack.getTag().contains("skill")) {
                return;
            }

            String skillName = stack.getTag().getString("skill");
            stack.getTag().putString("skill", "epicfight:hypervitality");

            EFIMod.LOGGER.debug(skillName);
            if (!CompactUtil.dosePlayerMeetReqs(playerpatch, ReqType.WEAPON)) {
                playerpatch.getEntityState().setState(EntityState.CAN_SKILL_EXECUTION, false);
                playerpatch.getOriginal().sendSystemMessage(Component.literal("你不满足使用技能的条件"));
                playerpatch.cancelAnyAction();
                event.setCanceled(true);
            }

        }
    }

}
