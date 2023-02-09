package io.github.andyrusso.pvplegacyutils;

import io.github.andyrusso.pvplegacyutils.api.LeftClickBlockCallback;
import io.github.andyrusso.pvplegacyutils.api.NewGameMessageCallback;
import io.github.andyrusso.pvplegacyutils.api.PvPLegacyUtilsAPI;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.Map;

/**
 * {@code enum} for different cooldowns in the cooldowns {@code EnumMap}.
 */
enum CooldownNames {
	STATS,
	STATS_MIDDLECLICK,
	NOTIFICATION,
	LEFTCLICK_LEAVE,
}


public class PvPLegacyUtils implements ClientModInitializer {

	public static final String MOD_ID = "PvPLegacyUtils";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * {@code EnumMap} containing different cooldowns.
	 */
	public static final EnumMap<CooldownNames, Integer> cooldowns = new EnumMap<>(
			Map.of(
					CooldownNames.STATS, 0,
					CooldownNames.STATS_MIDDLECLICK, 0,
					CooldownNames.NOTIFICATION, 0,
					CooldownNames.LEFTCLICK_LEAVE, 0
			)
	);

	/**
	 * Common code for playing a sound for notification features.
	 */
	private static void playSound() {
		MinecraftClient client = MinecraftClient.getInstance();

		// Check if the player isn't null.
		if (client.player != null) {
			// Create the sound.
			PositionedSoundInstance sound = new PositionedSoundInstance(
					Versioned.getNoteBlockBell(),
					SoundCategory.MASTER,
					// Set this to prevent the sound getting quieter as the player goes further away.
					Float.MAX_VALUE,
					1f,
					SoundInstance.createRandom(),
					// Set the sound position at the current player's coordinates to avoid weird behavior.
					client.player.getX(),
					client.player.getY(),
					client.player.getZ()
			);

			client.getSoundManager().play(sound);
		}
	}

	/**
	 * A method that gets called every new chat message. This method is registered in a custom event.
	 *
	 * <p>Used for different chat-based features.
	 * @param message The {@code Text} object of a new chat message.
	 */
	private static void onChatMessage(Text message) {
		if (!PvPLegacyUtilsAPI.isVl()) return;

		String text = message.getString();
		// Ignore player messages.
		if (PvPLegacyUtilsAPI.isPlayerMessage(text)) return;

		if (PvPLegacyUtilsConfig.get(ConfigOptions.AUTOGG) && text.contains("has won the")) {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player != null && client.world != null) {
				// Get the player's team.
				Team team = client.world.getScoreboard().getPlayerTeam(client.player.getGameProfile().getName());
				// Fighters in Versus Duels are always in a team with a [R] or [B] prefix,
				// even after they die and become spectators.
				// The point of this check is to prevent external spectators,
				// that join after the game has started, to say "gg".
				if (team == null) return;

				Versioned.sendChatMessage("gg");
			}
		} else if (PvPLegacyUtilsConfig.get(ConfigOptions.DUEL) && text.contains("wants to duel")) {
			playSound();
		} else if (PvPLegacyUtilsConfig.get(ConfigOptions.INVITE) && text.contains("You have been invited to join")) {
			playSound();
		} else if (PvPLegacyUtilsConfig.get(ConfigOptions.TEN_VS_TEN) &&
				text.contains("?v?") &&
				text.contains("game is starting in")) {
			playSound();
		}
	}

	private static ItemStack onMiddleClick(PlayerEntity player, HitResult result) {
		// Although isInLobby() already contains the isVl() check, we still need the isVl() check as a separate thing,
		// to prevent spectators on other servers to run the /stats command.
		if (!PvPLegacyUtilsAPI.isVl() ||
				!PvPLegacyUtilsConfig.get(ConfigOptions.STATS_MIDDLECLICK) ||
				// The feature isn't supposed to work if the hud is hidden.
				MinecraftClient.getInstance().options.hudHidden ||
				!PvPLegacyUtilsAPI.isInLobby() &&
						MinecraftClient.getInstance().player != null &&
						// This is done so spectators can middle-click in versus duels to see their stats, why not?
						!MinecraftClient.getInstance().player.isSpectator()
		) return ItemStack.EMPTY;

		Versioned.sendCommand("stats");
		// Set the cooldown to 10 ticks or 0.5s to not spam the server in case of a ping spike.
		cooldowns.put(CooldownNames.STATS_MIDDLECLICK, 10);

		return ItemStack.EMPTY;
	}

	private static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand,
											Entity entity, HitResult hitResult) {
		if (!PvPLegacyUtilsConfig.get(ConfigOptions.STATS) ||
				!PvPLegacyUtilsAPI.isInLobby() ||
				// Check if the clicked entity is a player or not.
				!(entity instanceof PlayerEntity) ||
				// The feature isn't supposed to work if the hud is hidden.
				MinecraftClient.getInstance().options.hudHidden
		) return ActionResult.PASS;

		// If the feature is on a cooldown.
		if (cooldowns.get(CooldownNames.STATS) != 0) {
			// And the cooldown of the notification message hasn't expired stop executing.
			if (cooldowns.get(CooldownNames.NOTIFICATION) != 0) return ActionResult.PASS;

			// Otherwise send a client-side message in chat, containing the time left on cooldown.
			String cooldown_in_secs = new DecimalFormat("#.#")
					.format((double) cooldowns.get(CooldownNames.STATS) / 20);

			if (MinecraftClient.getInstance().player != null) {
				MinecraftClient.getInstance().player.sendMessage(
						Text.literal(
								 "[" + Formatting.GREEN + MOD_ID + Formatting.RESET + "]:" +
										" This feature is on a cooldown, for another " +
										 Formatting.DARK_GREEN + Formatting.BOLD + cooldown_in_secs + "s"
						)
				);
			}

			// Set the cooldown for the notification for 10 ticks or 0.5 seconds.
			cooldowns.put(CooldownNames.NOTIFICATION, 10);

			return ActionResult.PASS;
		}

		String name = ((PlayerEntity) entity).getGameProfile().getName();
		Versioned.sendCommand("stats " + name);
		// Set the cooldown of this feature to 100 ticks or 5 seconds.
		cooldowns.put(CooldownNames.STATS, 100);

		// Set the notification cooldown to 5 ticks or 0.25 seconds,
		// this is needed so the chat wouldn't get spammed with notifications right away.
		cooldowns.put(CooldownNames.NOTIFICATION, 5);

		return ActionResult.PASS;
	}

	private static void onBlockLeftClick(BlockPos blockPos) {
		if (!PvPLegacyUtilsAPI.isInLobby()) return;

		if (cooldowns.get(CooldownNames.LEFTCLICK_LEAVE) == 0 &&
				PvPLegacyUtilsConfig.get(ConfigOptions.LEAVE_LEFTCLICK) &&
				PvPLegacyUtilsAPI.isInQueue() &&
				// Check if the clicked block is the sign that the player has queued in.
				blockPos.equals(PvPLegacyUtilsAPI.getQueuedSignBlock()) &&
				MinecraftClient.getInstance().player != null) {
			Versioned.sendCommand("leave");
			// Put it on a cooldown of 20 ticks or 1 second to not spam the server with "/leave"s.
			cooldowns.put(CooldownNames.LEFTCLICK_LEAVE, 20);
		}
	}

	public static ActionResult onBlockRightClick(PlayerEntity player,
												 World world,
												 Hand hand,
												 BlockHitResult hitResult) {
		if (!PvPLegacyUtilsAPI.isInLobby()) return ActionResult.PASS;

		Block block = world.getBlockState(hitResult.getBlockPos()).getBlock();
		if (block instanceof AbstractSignBlock) {
			PvPLegacyUtilsAPI.setTemporarySignBlock(hitResult.getBlockPos());
			if (PvPLegacyUtilsConfig.get(ConfigOptions.LEAVE_EXPLICIT) &&
					PvPLegacyUtilsAPI.isInQueue() &&
					!player.isSneaking())
				return ActionResult.FAIL;
		}

		return ActionResult.PASS;
	}

	private static void worldTick(ClientWorld world) {
		// Every tick check if a cooldown is set, and if it is decrease it
		cooldowns.forEach(
				(key, cooldown) -> {
					if (cooldown > 0) {
						cooldowns.put(key, cooldown - 1);
					}
				}
		);
	}

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing {}...", MOD_ID);

		Versioned.load();
		LOGGER.info("Loaded the version dependent implementations...");

		PvPLegacyUtilsConfig.open();
		LOGGER.info("Opened the config...");

		// Responsible for chat-based features,
		// such as: auto gg, duel notification, party invite notification, ?v? notification.
		NewGameMessageCallback.EVENT.register(PvPLegacyUtils::onChatMessage);
		LOGGER.info("Registered the new chat message event...");

		// Responsible for the right-click for stats feature.
		UseEntityCallback.EVENT.register(PvPLegacyUtils::onUseEntity);
		LOGGER.info("Registered right-click on entity event...");

		// Responsible for middle-click for own stats feature.
		ClientPickBlockGatherCallback.EVENT.register(PvPLegacyUtils::onMiddleClick);
		LOGGER.info("Registered the middle-click event...");

		// Responsible for left-click to leave feature.
		LeftClickBlockCallback.EVENT.register(PvPLegacyUtils::onBlockLeftClick);
		LOGGER.info("Registered the right-click on block event...");

		// Responsible for explicit /leave feature, and is also a data source for the API field "temporarySignBlock".
		UseBlockCallback.EVENT.register(PvPLegacyUtils::onBlockRightClick);
		LOGGER.info("Registered the right-click on block event...");

		// Responsible for counting down cooldowns.
		ClientTickEvents.START_WORLD_TICK.register(PvPLegacyUtils::worldTick);
		LOGGER.info("Registered the every tick event...");

		LOGGER.info("Initialization of {} done!", MOD_ID);
	}
}
