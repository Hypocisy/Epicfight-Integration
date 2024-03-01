package com.kumoe.EpicFightIntegration.event;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.commands.CmdPmmoSkillBooksRoot;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.util.CompactUtil;
import com.kumoe.EpicFightIntegration.util.SkillResult;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.item.WeaponItem;

@Mod.EventBusSubscriber(modid = EpicFightIntegration.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EFIEventHandler {
//    @SubscribeEvent
//    public static void itemUseStartEvent(LivingEntityUseItemEvent.Start event) {
//        if (event.getEntity() instanceof Player player) {
//            PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
//
//            if (playerpatch == null) {
//                return;
//            }
//            InteractionHand hand = player.getItemInHand(InteractionHand.MAIN_HAND).equals(event.getItem()) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
//            CapabilityItem itemCap = playerpatch.getHoldingItemCapability(hand);
//
//
//            ItemStack stack = player.getItemInHand(hand);
//            if (stack.getTag() == null || !stack.getTag().contains("skill")) {
//                return;
//            }
//
//            String skillName = stack.getTag().getString("skill").replace("epicfight:", "");
//            SkillResult skillResult = CompactUtil.getSkillResult(playerpatch, skillName);
//            if (!skillResult.getResult()) {
//                playerpatch.getEntityState().setState(EntityState.INACTION, false);
//                playerpatch.getEntityState().setState(EntityState.CAN_SKILL_EXECUTION, false);
//                event.setCanceled(true);
//            }
//        }
//    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        CmdPmmoSkillBooksRoot.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
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

            String skillName = stack.getTag().getString("skill").replace("epicfight:", "");

            EpicFightIntegration.LOGGER.debug(skillName);
            SkillResult skillResult = CompactUtil.getSkillResult(playerpatch, skillName);
            if (stack.getItem() instanceof WeaponItem && !skillResult.getResult()) {
                playerpatch.getEntityState().setState(EntityState.CAN_SKILL_EXECUTION, false);
                playerpatch.getOriginal().sendSystemMessage(Component.literal("你不满足使用技能的条件" + skillResult.getNoMeetReqs()).withStyle(ChatFormatting.RED));
                playerpatch.cancelAnyAction();
                event.setCanceled(true);
            }

        }
    }

}
