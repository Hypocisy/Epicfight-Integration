package com.kumoe.EpicFightIntegration.event;

import com.kumoe.EpicFightIntegration.EpicFightIntegration;
import com.kumoe.EpicFightIntegration.commands.CmdPmmoSkillBooksRoot;
import com.kumoe.EpicFightIntegration.config.EFIConfig;
import com.kumoe.EpicFightIntegration.config.codecs.SkillRequirements;
import com.kumoe.EpicFightIntegration.network.SkillLevelSyncPacket;
import com.kumoe.EpicFightIntegration.network.SkillRequirementSyncPacket;
import com.kumoe.EpicFightIntegration.util.CompactUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = EpicFightIntegration.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
	private static final String CHANNEL_PROTOCOL = "0";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			ResourceLocation.fromNamespaceAndPath(EpicFightIntegration.MODID, "main"),
			() -> CHANNEL_PROTOCOL,
			CHANNEL_PROTOCOL::equals,
			CHANNEL_PROTOCOL::equals);

	@SubscribeEvent
	public static void onCommandRegister(final RegisterCommandsEvent event) {
		CmdPmmoSkillBooksRoot.register(event.getDispatcher());
	}

	public static void registerPackets() {
		int id = 0;
		CHANNEL.registerMessage(++id, SkillRequirementSyncPacket.class,
				SkillRequirementSyncPacket::encode,
				SkillRequirementSyncPacket::decode,
				SkillRequirementSyncPacket::onPacketReceived);
		CHANNEL.registerMessage(++id, SkillLevelSyncPacket.class,
				SkillLevelSyncPacket::encode,
				SkillLevelSyncPacket::decode,
				SkillLevelSyncPacket::onPacketReceived);
		SkillRequirements.SKILL_SETTINGS.subscribeAsSyncable(CHANNEL, SkillRequirementSyncPacket::new);
		SkillRequirements.TEMPLATES.subscribeAsSyncable(CHANNEL, SkillLevelSyncPacket::new);
	}

	@SubscribeEvent
	public static void onAddReloadListeners(AddReloadListenerEvent event) {
		event.addListener(SkillRequirements.SKILL_SETTINGS);
		event.addListener(SkillRequirements.TEMPLATES);
	}

	/*@SubscribeEvent
	public static void onDatapackSync(OnDatapackSyncEvent event) {
		SkillRequirements.SKILL_SETTINGS.subscribeAsSyncable(CHANNEL, SkillRequirementSyncPacket::new);
		SkillRequirements.TEMPLATES.subscribeAsSyncable(CHANNEL, SkillLevelSyncPacket::new);
	}*/

	public static void resetData() {
		SkillRequirements.SKILL_SETTINGS.clearData();
		SkillRequirements.TEMPLATES.clearData();
	}

	@SubscribeEvent
	public static void onPlayerJoinLevel(PlayerEvent.PlayerLoggedInEvent event) {

		var player = event.getEntity();
		var playerPatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);

		handelSkillCast(playerPatch, player);

	}

	static void handelSkillCast(PlayerPatch<?> playerPatch, Player player) {
		playerPatch.getEventListener().addEventListener(
				PlayerEventListener.EventType.SKILL_CAST_EVENT,
				UUID.randomUUID(),
				skillCastEvent -> {
					SkillContainer skillContainer = skillCastEvent.getSkillContainer();
					var skill = skillContainer.getSkill();
					if (skill != null) {
						Map<String, Integer> conditions = CompactUtil.getSkillCondition(playerPatch, CompactUtil.getRlByCategory(skill));
						if (!conditions.isEmpty()) {
							if (skillContainer.getDataManager().hasData(SkillDataKeys.JUMP_COUNT.get())) {
								skillContainer.getDataManager().setData(SkillDataKeys.JUMP_COUNT.get(), 0);
							}
							if (playerPatch.isLogicalClient() && skillContainer.getDataManager().hasData(SkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK.get())) {
								skillContainer.getDataManager().setData(SkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK.get(), false);
							}
							CompactUtil.displayMessage(Component.translatable("pmmo.msg.denial.skill", conditions.toString()).withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), player);
							if (EFIConfig.enableDebug) {
								EpicFightIntegration.LOGGER.debug(conditions.toString());
							}
							skillCastEvent.setCanceled(true);
						}
					}
				}
		);
	}
}
