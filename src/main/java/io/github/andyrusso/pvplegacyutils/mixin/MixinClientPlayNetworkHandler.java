package io.github.andyrusso.pvplegacyutils.mixin;

import io.github.andyrusso.pvplegacyutils.api.NewGameMessageCallback;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    /**
     * A hook for every new game message received as a {@link GameMessageS2CPacket}.
     *
     * <p>I inject into this class, instead of {@link net.minecraft.client.gui.hud.ChatHud}, because there was some
     * incompatibility.
     *
     * <p>This fires the {@link NewGameMessageCallback} event.
     *
     * @param packet The new game message, as a {@link GameMessageS2CPacket} packet.
     * @return Un-altered {@link GameMessageS2CPacket} object
     */
    @ModifyVariable(method = "onGameMessage", at = @At(value = "TAIL"), argsOnly = true)
    private GameMessageS2CPacket onGameMessage(GameMessageS2CPacket packet) {
        NewGameMessageCallback.EVENT.invoker().interact(packet.content());
        return packet;
    }
}
