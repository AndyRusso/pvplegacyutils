package io.github.andyrusso.pvplegacyutils.mixin;

import io.github.andyrusso.pvplegacyutils.api.HideGameMessageCallback;
import io.github.andyrusso.pvplegacyutils.api.NewGameMessageCallback;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}
