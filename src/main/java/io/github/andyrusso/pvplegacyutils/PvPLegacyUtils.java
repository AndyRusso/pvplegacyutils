package io.github.andyrusso.pvplegacyutils;

import com.mojang.brigadier.CommandDispatcher;
import io.github.andyrusso.pvplegacyutils.api.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandRegistryAccess;
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
import java.util.function.Consumer;

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

	private static boolean checkForDream = false;

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

	private static boolean filter(Text message) {
		String text = message.getString();
		PvPLegacyUtilsConfig config = PvPLegacyUtilsConfig.getInstance();

		boolean isPlayer = PvPLegacyUtilsAPI.isPlayerMessage(text);

		if (config.hideNoSF &&
				// Check this so sf discussion in lobbies and FFA would still be shown,
				// whilst bypassing the accurate 4-second duel check.
				!PvPLegacyUtilsAPI.isInLobby() &&
				!PvPLegacyUtilsAPI.isInFFA() &&
				isPlayer &&
				text.toLowerCase().contains("no sf")) return true;

		if (config.hideTips && text.startsWith("\n[PvPLegacy]")) {
			// Hide the message if it's not either of these,
			// because these are notifications and are actually useful
			return !text.contains("You have been invited to join") &&
					!text.contains("is starting in");
		}

		if (checkForDream && !isPlayer) {
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if (player == null) return false;

			Consumer<String> send = (String key) ->
					MinecraftClient.getInstance().execute(() -> player.sendMessage(Text.translatable(key)));

			checkForDream = false;
			if (text.equals("Could not find player by the name of dream.")) {
				send.accept("pvplegacyutils.isdreamonline.no");
				return true;
			} else if (text.startsWith("Dream is not") || text.toLowerCase().startsWith("dream has disabled")) {
				send.accept("pvplegacyutils.isdreamonline.yes");
				return true;
			} else if (text.startsWith("Dream is on") || text.startsWith("Dream is in")) {
				send.accept("pvplegacyutils.isdreamonline.yes");
				return false;
			}
			checkForDream = true;
		}

		return false;
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

		PvPLegacyUtilsConfig config = PvPLegacyUtilsConfig.getInstance();

		if (config.autogg) {
			autogg(text, config);
		}

		if (config.pingDuel && text.contains("wants to duel")) {
			playSound();
		} else if (config.pingInvite && text.contains("You have been invited to join")) {
			playSound();
		} else if (text.contains("is starting in")) {
			if (config.pingGodGame && text.contains("God Game") || config.pingTenVSTen && text.contains("?v?")) {
				playSound();
			}
		}
	}

	private static void autogg(String text, PvPLegacyUtilsConfig config) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.world == null) return;

		// Get the player's team.
		Team team = client.world.getScoreboard().getPlayerTeam(client.player.getGameProfile().getName());
		// Fighters in Versus Duels are always in a team with a [R] or [B] prefix,
		// even after they die and become spectators.
		// The point of this variable is to prevent external spectators,
		// that join after the game has started, to say things.
		boolean isPartOfDuel = team != null;

		boolean isStartOfGame = text.contains("You can use \"/leave\" to leave the game.");
		isStartOfGame = isStartOfGame || text.matches("^Game (is )?starting in.*");

		boolean isStartOfRound = text.matches(".*has started[.!].*");

		if (config.autoggStartGame && isStartOfGame) {
			Versioned.sendChatMessage(config.autoggStartGameText);
			return;
		}

		if (!isPartOfDuel) return;

		if (config.autoggStartRound && isStartOfRound) {
			Versioned.sendChatMessage(config.autoggStartRoundText);
			return;
		}

		if (!text.contains("was a draw") && !text.contains("won the")) return;

		boolean isEndOfGame = text.contains("game");

		// If `config.autoggEndGame` is false and `config.autoggEndRound` is true,
		// there will be a `config.autoggEndRoundText` message sent.
		if (config.autoggEndGame && isEndOfGame) {
			Versioned.sendChatMessage(config.autoggEndGameText);
		} else if (config.autoggEndRound && (text.toLowerCase().contains("round") || isEndOfGame)) {
			Versioned.sendChatMessage(config.autoggEndRoundText);
		}
	}

	private static ItemStack onMiddleClick(PlayerEntity player, HitResult result) {
		// Although isInLobby() already contains the isVl() check, we still need the isVl() check as a separate thing,
		// to prevent spectators on other servers to run the /stats command.
		if (!PvPLegacyUtilsAPI.isVl() ||
				!PvPLegacyUtilsConfig.getInstance().statsMiddleClick ||
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
		if (!PvPLegacyUtilsConfig.getInstance().statsRightClick ||
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
		// Set the cooldown of this feature to 10 ticks or 0.5 seconds.
		cooldowns.put(CooldownNames.STATS, 10);

		// Set the notification cooldown to 5 ticks or 0.25 seconds,
		// this is needed so the chat wouldn't get spammed with notifications right away.
		cooldowns.put(CooldownNames.NOTIFICATION, 5);

		return ActionResult.PASS;
	}

	private static ActionResult onBlockLeftClick(BlockPos blockPos) {
		if (!PvPLegacyUtilsAPI.isInLobby()) return ActionResult.PASS;

		ClientWorld world = MinecraftClient.getInstance().world;
		if (world == null) return ActionResult.PASS;

		Block block = world.getBlockState(blockPos).getBlock();
		if (block instanceof AbstractSignBlock) {
			PvPLegacyUtilsAPI.setTemporarySignBlock(blockPos);
		}

		if (
				PvPLegacyUtilsConfig.getInstance().leaveLeftClick &&
				PvPLegacyUtilsAPI.isInQueue() &&
				// Check if the clicked block is the sign that the player has queued in.
				blockPos.equals(PvPLegacyUtilsAPI.getQueuedSignBlock())
		) {
			if (cooldowns.get(CooldownNames.LEFTCLICK_LEAVE) > 0) {
				return ActionResult.FAIL;
			}

			Versioned.sendCommand("leave");
			// Put it on a cooldown of 10 ticks or 0.5 seconds to not spam the server with "/leave"s.
			cooldowns.put(CooldownNames.LEFTCLICK_LEAVE, 10);

			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	public static ActionResult onBlockRightClick(PlayerEntity player,
												 World world,
												 Hand hand,
												 BlockHitResult hitResult) {
		if (!PvPLegacyUtilsAPI.isInLobby()) return ActionResult.PASS;

		Block block = world.getBlockState(hitResult.getBlockPos()).getBlock();
		if (block instanceof AbstractSignBlock) {
			PvPLegacyUtilsAPI.setTemporarySignBlock(hitResult.getBlockPos());
			if (PvPLegacyUtilsConfig.getInstance().leaveExplicitly &&
					PvPLegacyUtilsAPI.isInQueue() &&
					!player.isSneaking())
				return ActionResult.FAIL;
		}

		return ActionResult.PASS;
	}

	private static void onDeath(PlayerListEntry playerListEntry) {
		if (!PvPLegacyUtilsAPI.isInDuel() ||
				!PvPLegacyUtilsConfig.getInstance().deathParticles ||
				playerListEntry == null) return;

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) return;

		PlayerEntity player = client.player.clientWorld.getPlayerByUuid(playerListEntry.getProfile().getId());
		if (player == null) return;

		Versioned.addFireworkParticle(player.getX(), player.getY() + 1, player.getZ());
	}

	private static void isdreamonline(
			CommandDispatcher<FabricClientCommandSource> dispatcher,
			CommandRegistryAccess _commandRegistryAccess
	) {
		dispatcher.register(ClientCommandManager.literal("isdreamonline").executes(
				context -> {
					if (!PvPLegacyUtilsAPI.isVl()) {
						context.getSource().sendFeedback(Text.translatable("pvplegacyutils.isdreamonline.notvl"));
						return 0;
					}

					if (!PvPLegacyUtilsAPI.isInLobby()) {
						context.getSource().sendFeedback(
								Text.translatable("pvplegacyutils.isdreamonline.notlobby")
						);
						return 0;
					}

					Versioned.sendCommand("fp Dream");
					checkForDream = true;

					return 0;
				}
		));
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

		// Responsible for chat-based features,
		// such as: auto gg, duel notification, party invite notification, ?v? notification.
		NewGameMessageCallback.EVENT.register(PvPLegacyUtils::onChatMessage);
		LOGGER.info("Registered the new chat message event...");

		// Responsible for filter features
		HideGameMessageCallback.EVENT.register(PvPLegacyUtils::filter);
		LOGGER.info("Registered the hide chat message event...");

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

		// Responsible for death particles feature
		DeathCallback.EVENT.register(PvPLegacyUtils::onDeath);

		// Responsible for counting down cooldowns.
		ClientTickEvents.START_WORLD_TICK.register(PvPLegacyUtils::worldTick);
		// Responsible for detecting whether the player is in Lobby, Versus duel, FFA.
		ClientTickEvents.END_WORLD_TICK.register(PvPLegacyUtilsAPI::detectMode);
		LOGGER.info("Registered the every tick event...");

		ClientCommandRegistrationCallback.EVENT.register(PvPLegacyUtils::isdreamonline);

		LOGGER.info("Initialization of {} done!", MOD_ID);
	}
}
