package io.github.andyrusso.pvplegacyutils.mixin;

import io.github.andyrusso.pvplegacyutils.api.PvPLegacyUtilsAPI;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DownloadingTerrainScreen.class)
public abstract class MixinDownloadingTerrainScreen {
    /**
     * A simple way to detect and set that the player isn't in any queues anymore whenever they
     * leave/change world/reconnect to the server.
     *
     * <p>Also sets the timeout for proper mode detection.
     *
     * @param ci Unused.
     * @see PvPLegacyUtilsAPI
     */
    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void init(CallbackInfo ci) {
        PvPLegacyUtilsAPI.setIsInQueue(false);
        PvPLegacyUtilsAPI.setTimeout();
    }
}
