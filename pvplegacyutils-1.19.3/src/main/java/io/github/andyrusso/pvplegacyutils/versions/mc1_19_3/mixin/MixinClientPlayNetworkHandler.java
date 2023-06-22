package io.github.andyrusso.pvplegacyutils.versions.mc1_19_3.mixin;

import io.github.andyrusso.pvplegacyutils.api.DeathCallback;
import io.github.andyrusso.pvplegacyutils.api.PvPLegacyUtilsAPI;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    /**
     * A hook for every player going into spectator mode.
     * @param args Arguments passed to {@link net.minecraft.client.network.PlayerListEntry#setGameMode setGameMode}.
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
