package io.github.andyrusso.pvplegacyutils.api;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
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

    /**
     * Although not used yet, could be used with new features.
     * @return whether the player is in a Versus Duel or not.
     */
    public static boolean isInDuel() {
        return isVl() && isInDuel;
    }

    public static void setIsInDuel(boolean isInDuel) {
        PvPLegacyUtilsAPI.isInDuel = isInDuel;
    }

    /**
     * Although not used yet, could be used with new features.
     * @return whether the player is in FFA or not.
     */
    public static boolean isInFFA() {
        return isVl() && isInFFA;
    }

    public static void setIsInFFA(boolean isInFFA) {
        PvPLegacyUtilsAPI.isInFFA = isInFFA;
    }

    public static boolean isInLobby() {
        return isVl() && isInLobby;
    }

    public static void setIsInLobby(boolean isInLobby) {
        PvPLegacyUtilsAPI.isInLobby = isInLobby;
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
     * <p>Also, when a player uses the party chat, the message will be of form: "[X's Party] <..."
     * so, we detect all messages falling in that pattern.
     * @param message The string to check.
     * @return {@code true} or {@code false}.
     */
    public static boolean isPlayerMessage(String message) {
        return message.startsWith("<") ||
                message.contains("-") ||
                message.matches("^\\[[a-zA-Z0-9_]{2,16}'s Party] <.*");
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
                text.startsWith("You have left the queue.")) {
            // Set the queuedSignBlock to 0, 0, 0, to ensure that it won't be mistaken for any other sign.
            PvPLegacyUtilsAPI.queuedSignBlock = BlockPos.ORIGIN;
            setIsInQueue(false);
        }
    }
}
