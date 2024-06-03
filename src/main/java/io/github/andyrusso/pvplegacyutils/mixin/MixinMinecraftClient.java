package io.github.andyrusso.pvplegacyutils.mixin;

import io.github.andyrusso.pvplegacyutils.api.LeftClickBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Shadow @Nullable public HitResult crosshairTarget;

    @Shadow @Nullable public ClientPlayerInteractionManager interactionManager;

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void preventBlockAttack(CallbackInfoReturnable<Boolean> cir) {
        if (this.crosshairTarget == null || this.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult blockHitResult = (BlockHitResult) this.crosshairTarget;

        ActionResult result = LeftClickBlockCallback.EVENT.invoker().interact(blockHitResult.getBlockPos());

        if (result != ActionResult.PASS) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void preventBlockBreaking(boolean breaking, CallbackInfo ci) {
        if (!breaking) return;

        if (this.crosshairTarget == null || this.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult blockHitResult = (BlockHitResult) this.crosshairTarget;

        ActionResult result = LeftClickBlockCallback.EVENT.invoker().interact(blockHitResult.getBlockPos());

        if (result != ActionResult.PASS) {
            if (this.interactionManager != null) {
                this.interactionManager.cancelBlockBreaking();
            }

            ci.cancel();
        }
    }
}
