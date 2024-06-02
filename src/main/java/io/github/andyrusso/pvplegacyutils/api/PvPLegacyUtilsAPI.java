package io.github.andyrusso.pvplegacyutils.api;

import io.github.andyrusso.pvplegacyutils.Versioned;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * This class contains the basic functionality of the mod, that isn't directly linked with any actual features of it.
 * For example, knowing if the player is in the lobby.
 *
 * <p>Because of this reason, I made this a separate class. An additional bonus of this being a standalone class, is
 * that other mods could potentially use this as an API for their own PvP Legacy client-side mods.
 */
public abstract class PvPLegacyUtilsAPI {
    /**
     * Timeout for mode detection.
     *
     * <p>When going from a Versus duel to the Lobby, the scoreboard takes some time to appear.
     * So, to properly detect that the player is in a Versus duel, there is a 4-second timeout before making a decision,
     * to let the scoreboard appear if the player is in the lobby.
     */
    private static int timeout = 0;

    /**
     * Constant for scoreboard slot that is responsible for numbers near players' names in the player list.
     *
     * <p>This is required because in 1.20.2+ the scoreboard slots are enums and in <1.20.2 they are ints.
     * I can construct the enums out of ints, so the middle ground is using access by int and constructing the enum
     * in the 1.20.2 version-specific implementation.
     */
    private static final int SCOREBOARD_SLOT_LIST = 0;
    /**
     * Constant for scoreboard slot list that is responsible for the sidebar. What many people know as "the scoreboard".
     *
     * @see PvPLegacyUtilsAPI#SCOREBOARD_SLOT_LIST
     */
    private static final int SCOREBOARD_SLOT_SIDEBAR = 1;

    // Whether the player is in a versus duel, FFA, or in the versus lobby
    private static boolean isInDuel = false;
    private static boolean isInFFA = false;
    private static boolean isInLobby = false;

    /**
     * Whether the player is in a queue or not.
     *
     * <p>Updated independently of {@link io.github.andyrusso.pvplegacyutils.PvPLegacyUtils},
     * but is changed from a mixin ({@link io.github.andyrusso.pvplegacyutils.mixin.MixinDownloadingTerrainScreen}),
     * so both, the getter and the setter are available.
     */
    private static boolean isInQueue = false;

    /**
     * An internal field to update {@code queuedSignBlock}. Unfortunately it is updated from
     * {@link io.github.andyrusso.pvplegacyutils.PvPLegacyUtils}, which makes this class not fully independent of
     * the base mod. I might fix it later on by including the API as a Jar-In-Jar.
     *
     * <p>Only the setter for this field is available.
     */
    private static BlockPos temporarySignBlock;

    /**
     * Field to make left-click to leave possible. Updated internally, only the getter is available.
     */
    private static BlockPos queuedSignBlock;

    public static void setTimeout() {
        // Set timeout to 80 ticks or 4 seconds
        timeout = 80;
    }

    /**
     * Although not used yet, could be used with new features.
     * @return whether the player is in a Versus Duel or not.
     */
    public static boolean isInDuel() {
        return isVl() && isInDuel;
    }

    /**
     * Although not used yet, could be used with new features.
     * @return whether the player is in FFA or not.
     */
    public static boolean isInFFA() {
        return isVl() && isInFFA;
    }

    public static boolean isInLobby() {
        return isVl() && isInLobby;
    }

    public static boolean isInQueue() {
        return isInQueue;
    }

    public static void setIsInQueue(boolean isInQueue) {
        PvPLegacyUtilsAPI.isInQueue = isInQueue;
    }

    public static BlockPos getQueuedSignBlock() {
        return queuedSignBlock;
    }

    public static void setTemporarySignBlock(BlockPos temporarySignBlock) {
        PvPLegacyUtilsAPI.temporarySignBlock = temporarySignBlock;
    }

    public static boolean isVl() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) return true;

        if (MinecraftClient.getInstance().getCurrentServerEntry() == null) return false;
        String address = MinecraftClient.getInstance().getCurrentServerEntry().address;
        // If you didn't know, vanillalegacy.com used to be the old ip for PvP Legacy, and it still works!
        return address.toLowerCase().endsWith("pvplegacy.net") || address.toLowerCase().endsWith("vanillalegacy.com");
    }

    /**
     * Whether a chat message is sent from a player or not.
     *
     * <p>Player messages always start with "<", and personal messages (/w) always have the "-" character.
     * <p>For example, they start with: {@code [AndyRusso -> You]} or {@code [You -> AndyRusso]}.
     * <p>Also, when a player uses the party chat, the message will be of form: {@code [X's Party] <...}
     * so, we detect all messages falling in that pattern.
     * <p>And, if player has global chat enabled, or there is a spectator,
     * player messages can be like: {@code [Lobby]<...}, {@code [Game]<...}, {@code [Spectator]<...} with formatting.
     * @param message The string to check.
     * @return {@code true} or {@code false}.
     */
    public static boolean isPlayerMessage(String message) {
        return message.startsWith("<") ||
                message.contains("-") ||
                message.matches("^\\[[a-zA-Z0-9_]{2,16}'s Party] <.*") ||
                message.matches("\\[(Lobby|Spectator|Game)](§f)?<.*");
    }

    /**
     * Fired before any other {@link NewGameMessageCallback} event.
     * @param message The game message to process.
     */
    public static void onGameMessage(Text message) {
        if (!isVl()) return;

        String text = message.getString();
        // Ignore player messages.
        if (isPlayerMessage(text)) return;
        if (MinecraftClient.getInstance().player == null) return;

        String name = MinecraftClient.getInstance().player.getGameProfile().getName();
        if (text.contains(name + " has joined the queue.")) {
            // Set the queuedSignBlock to the latest temporarySignBlock,
            // so that left-click to leave has something to compare with.
            PvPLegacyUtilsAPI.queuedSignBlock = temporarySignBlock;
            setIsInQueue(true);
        } else if (text.contains("The host left, the queue has ended.") ||
                text.startsWith("You have left the queue.") ||
                // Happens very rarely in 10v10 queues, after this message the queue is disbanded
                text.contains("Game failed to start.")) {
            // Set the queuedSignBlock to 0, 0, 0, to ensure that it won't be mistaken for any other sign.
            PvPLegacyUtilsAPI.queuedSignBlock = BlockPos.ORIGIN;
            setIsInQueue(false);
        }
    }

    public static void detectMode(ClientWorld world) {
        // Reset all the states
        isInDuel = false;
        isInFFA = false;
        isInLobby = false;

        if (!isVl()) return;

        if (timeout > 0) {
            timeout--;
        }

        Scoreboard scoreboard = world.getScoreboard();

        // If there is a scoreboard that is displayed in the player list, then the player is in FFA.
        // (it always shows the amount of kills that a player has, even if it's 0)
        if (Versioned.getObjectiveForSlot(scoreboard, PvPLegacyUtilsAPI.SCOREBOARD_SLOT_LIST) != null) {
            isInFFA = true;
            return;
        }

        ScoreboardObjective scoreboardObjective =
                Versioned.getObjectiveForSlot(scoreboard, PvPLegacyUtilsAPI.SCOREBOARD_SLOT_SIDEBAR);

        // If the scoreboard is empty, this means the player is in a Versus Duel
        if (scoreboardObjective == null) {
            if (timeout == 0) isInDuel = true;
            return;
        }

        // For every "player" in the sidebar scoreboard, check its decorated name for "Server",
        // because only the Versus Lobby sidebar scoreboard has the "Server" field, and it can not be turned off.
        for (Object player : Versioned.getAllScoreHolders(scoreboard, scoreboardObjective)) {
            String scoreHolderName = Versioned.getScoreHolderName(player);
            Team team = scoreboard.getPlayerTeam(scoreHolderName);
            String name = Team.decorateName(team, Text.literal(scoreHolderName)).getString();
            if (name.contains("Server")) {
                isInLobby = true;
                return;
            }
        }

        // In any other case, the player is in a Versus Duel, like a Solo pop UHC (unreleased game-mode)
        if (timeout == 0) {
            isInDuel = true;
        }
    }
}
