package io.github.andyrusso.pvplegacyutils.mixin;

import io.github.andyrusso.pvplegacyutils.api.DeathCallback;
import io.github.andyrusso.pvplegacyutils.api.HideGameMessageCallback;
import io.github.andyrusso.pvplegacyutils.api.NewGameMessageCallback;
import io.github.andyrusso.pvplegacyutils.api.PvPLegacyUtilsAPI;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    /**
     * A hook for every new game message received as a {@link GameMessageS2CPacket}.
     *
     * <p>This fires the {@link HideGameMessageCallback} event.
     *
     * @param packet The new game message, as a {@link GameMessageS2CPacket} packet.
     */
    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void allowGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        if (HideGameMessageCallback.EVENT.invoker().hide(packet.content())) ci.cancel();
    }

    /**
     * A hook for every new game message received as a {@link GameMessageS2CPacket}.
     *
     * <p>This fires the {@link NewGameMessageCallback} event.
     *
     * @param packet The new game message, as a {@link GameMessageS2CPacket} packet.
     */
    @Inject(method = "onGameMessage", at = @At("TAIL"))
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo _ci) {
        NewGameMessageCallback.EVENT.invoker().interact(packet.content());
    }

    /**
     * A simple way to detect and set that the player isn't in any queues anymore whenever they
     * leave/change world/reconnect to the server.
     *
     * <p>Also sets the timeout for proper mode detection.
     *
     * @param ci Unused.
     * @see PvPLegacyUtilsAPI
     */
    @Inject(method = "startWorldLoading*", at = @At(value = "HEAD"))
    private void init(CallbackInfo ci) {
        PvPLegacyUtilsAPI.setIsInQueue(false);
        PvPLegacyUtilsAPI.setTimeout();
    }


    /**
     * A hook for every player going into spectator mode.
     * @param args Arguments passed to {@link PlayerListEntry#setGameMode setGameMode}.
     * @param entry The {@link PlayerListEntry} object for the player.
     * @see DeathCallback
     */
    @ModifyArgs(
            method = "handlePlayerListAction",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/client/network/PlayerListEntry;setGameMode(Lnet/minecraft/world/GameMode;)V"
            )
    )
    private void getChangedGameMode(
            Args args,
            PlayerListS2CPacket.Action _action,
            PlayerListS2CPacket.Entry _receivedEntry,
            PlayerListEntry entry
    ) {
        if (args.get(0) == GameMode.SPECTATOR && PvPLegacyUtilsAPI.isVl()) {
            DeathCallback.EVENT.invoker().interact(entry);
        }
    }
}
