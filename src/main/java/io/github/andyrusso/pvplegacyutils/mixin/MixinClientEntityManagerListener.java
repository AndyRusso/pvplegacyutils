package io.github.andyrusso.pvplegacyutils.mixin;

import io.github.andyrusso.pvplegacyutils.api.DeathCallback;
import io.github.andyrusso.pvplegacyutils.api.PvPLegacyUtilsAPI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientEntityManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientEntityManager.Listener.class)
public class MixinClientEntityManagerListener {
    /**
     * A hook to detect players getting removed from the world.
     * <p>Used to spawn death particles in Versus duels.
     *
     * @see DeathCallback
     */
    @Inject(method = "remove", at = @At("HEAD"))
    private void getRemovedEntity(CallbackInfo ci) {
        // Ignore this warning, IntelliJ doesn't understand mixins fully.
        ClientEntityManager.Listener listener = (ClientEntityManager.Listener) (Object) this;
        if (!(listener.entity instanceof PlayerEntity) || !PvPLegacyUtilsAPI.isVl()) return;
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) return;

        DeathCallback.EVENT.invoker().interact(networkHandler.getPlayerListEntry(listener.entity.getUuid()));
    }
}
