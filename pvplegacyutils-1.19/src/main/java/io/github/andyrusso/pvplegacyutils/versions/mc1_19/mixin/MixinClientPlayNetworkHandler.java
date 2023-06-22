package io.github.andyrusso.pvplegacyutils.versions.mc1_19.mixin;

import io.github.andyrusso.pvplegacyutils.api.DeathCallback;
import io.github.andyrusso.pvplegacyutils.api.PvPLegacyUtilsAPI;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    /**
     * A hook for every player going into spectator mode.
     * @param entry The {@link PlayerListEntry} object for the player.
     * @param gameMode The game mode that the player is switching to.
     * @see DeathCallback
     */
    @Redirect(
            method = "onPlayerList",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/client/network/PlayerListEntry;setGameMode(Lnet/minecraft/world/GameMode;)V"
            )
    )
    private void getChangedGameMode(PlayerListEntry entry, GameMode gameMode) {
        if (gameMode == GameMode.SPECTATOR && PvPLegacyUtilsAPI.isVl()) {
            DeathCallback.EVENT.invoker().interact(entry);
        }

        entry.setGameMode(gameMode);
    }
}
