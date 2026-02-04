package io.github.andyrusso.pvplegacyutils;

import com.mojang.brigadier.CommandDispatcher;
import io.github.andyrusso.pvplegacyutils.api.*;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.List;
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
					SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(),
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

			SoundManager sm = client.getSoundManager();
            try {
				// This is basically sm.play(sound)
				// Done for multiversion support.
                Method m = SoundManager.class.getMethod("method_4873", SoundInstance.class);
				m.invoke(sm, sound);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
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
					MinecraftClient.getInstance().execute(() -> player.sendMessage(Text.translatable(key), false));

			checkForDream = false;
			if (text.contains("find a player by")) {
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
		var networkHandler = client.getNetworkHandler();
		if (client.player == null || client.world == null || networkHandler == null) return;

		// Get the player's team.
		Team team = client.world
				.getScoreboard().getScoreHolderTeam(Versioned.getProfileName(client.player.getGameProfile()));
		// Fighters in Versus Duels are always in a team with a [R] or [B] prefix,
		// even after they die and become spectators.
		// The point of this variable is to prevent external spectators,
		// that join after the game has started, to say things.
		boolean isPartOfDuel = team != null;

		boolean isStartOfGame = text.contains("You can use \"/leave\" to leave the game.");
		isStartOfGame = isStartOfGame || text.matches("^Game (is )?starting in.*");

		boolean isStartOfRound = text.matches(".*has started[.!].*");

		if (config.autoggStartGame && isStartOfGame) {
			networkHandler.sendChatMessage(config.autoggStartGameText);
			return;
		}

		if (!isPartOfDuel) return;

		if (
				config.autoggStartRound && isStartOfRound &&
				!(config.autoggSkipFirstRound && config.autoggStartGame && text.contains("Round 1 has started."))
		) {
			networkHandler.sendChatMessage(config.autoggStartRoundText);
			return;
		}

		if (!text.contains("was a draw") && !text.contains("won the")) return;

		boolean isEndOfGame = text.contains("game");

		// If `config.autoggEndGame` is false and `config.autoggEndRound` is true,
		// there will be a `config.autoggEndRoundText` message sent.
		if (config.autoggEndGame && isEndOfGame) {
			networkHandler.sendChatMessage(config.autoggEndGameText);
		} else if (config.autoggEndRound && (text.toLowerCase().contains("round") || isEndOfGame)) {
			networkHandler.sendChatMessage(config.autoggEndRoundText);
		}
	}

	public static boolean onMiddleClick() {
		if (
				!(
					PvPLegacyUtilsConfig.getInstance().statsMiddleClick && PvPLegacyUtilsAPI.isInLobby() ||
					PvPLegacyUtilsConfig.getInstance().statsMiddleClickFFA && PvPLegacyUtilsAPI.isInFFA()
				) ||
				// The feature isn't supposed to work if the hud is hidden.
				MinecraftClient.getInstance().options.hudHidden
		) return true;

		if (MinecraftClient.getInstance().getNetworkHandler() != null) {
			MinecraftClient.getInstance().getNetworkHandler().sendChatCommand("stats");
		}

		// Set the cooldown to 10 ticks or 0.5s to not spam the server in case of a ping spike.
		cooldowns.put(CooldownNames.STATS_MIDDLECLICK, 10);

		return false;
	}

	private static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand,
											Entity entity, HitResult hitResult) {
		if (!(PvPLegacyUtilsConfig.getInstance().statsRightClick && PvPLegacyUtilsAPI.isInLobby() ||
				PvPLegacyUtilsConfig.getInstance().statsRightClickFFA && PvPLegacyUtilsAPI.isInFFA()) ||
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
						),
						false
				);
			}

			// Set the cooldown for the notification for 10 ticks or 0.5 seconds.
			cooldowns.put(CooldownNames.NOTIFICATION, 10);

			return ActionResult.PASS;
		}

		String name = Versioned.getProfileName(((PlayerEntity) entity).getGameProfile());
		if (MinecraftClient.getInstance().getNetworkHandler() != null) {
			MinecraftClient.getInstance().getNetworkHandler().sendChatCommand("stats " + name);
		}
		// Set the cooldown of this feature to 10 ticks or 0.5 seconds.
		cooldowns.put(CooldownNames.STATS, 10);

		// Set the notification cooldown to 5 ticks or 0.25 seconds,
		// this is needed so the chat wouldn't get spammed with notifications right away.
		cooldowns.put(CooldownNames.NOTIFICATION, 5);

		return ActionResult.PASS;
	}

	private static ActionResult onBlockLeftClick(BlockPos blockPos) {
		ClientWorld world = MinecraftClient.getInstance().world;
		if (!PvPLegacyUtilsAPI.isInLobby() || world == null) return ActionResult.PASS;

		Block block = world.getBlockState(blockPos).getBlock();
		if (block instanceof AbstractSignBlock) {
			PvPLegacyUtilsAPI.setTemporarySignBlock(blockPos);
		}

		if (!PvPLegacyUtilsAPI.isInQueue()) return ActionResult.PASS;

		if (
				PvPLegacyUtilsConfig.getInstance().leaveLeftClick &&
				// Check if the clicked block is the sign that the player has queued in.
				blockPos.equals(PvPLegacyUtilsAPI.getQueuedSignBlock())
		) {
			if (cooldowns.get(CooldownNames.LEFTCLICK_LEAVE) > 0) {
				return ActionResult.FAIL;
			}

			if (MinecraftClient.getInstance().getNetworkHandler() != null) {
				MinecraftClient.getInstance().getNetworkHandler().sendChatCommand("leave");
			}
			// Put it on a cooldown of 10 ticks or 0.5 seconds to not spam the server with "/leave"s.
			cooldowns.put(CooldownNames.LEFTCLICK_LEAVE, 10);

			return ActionResult.SUCCESS;
		}

		if (PvPLegacyUtilsConfig.getInstance().leaveExplicitly) return ActionResult.FAIL;

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
		if (client.world == null) return;

		PlayerEntity player = client.world.getPlayerByUuid(Versioned.getProfileId(playerListEntry.getProfile()));
		if (player == null) return;

		client.world.addFireworkParticle(
				player.getX(),
				player.getY() + 1,
				player.getZ(),
				0,
				0,
				0,
				List.of(
						new FireworkExplosionComponent(
								FireworkExplosionComponent.Type.SMALL_BALL,
								IntList.of(44782, 157249),
								IntList.of(),
								false,
								false
						)
				)
		);
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

					if (MinecraftClient.getInstance().getNetworkHandler() != null) {
						MinecraftClient.getInstance().getNetworkHandler().sendChatCommand("fp Dream");
					}
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
