package io.github.andyrusso.pvplegacyutils.mixin;

import io.github.andyrusso.pvplegacyutils.api.LeftClickBlockCallback;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager {
    /**
     * @param pos The block being attacked as a {@link BlockPos}.
     * @return Un-altered pos.
     * @see LeftClickBlockCallback
     */
    @ModifyVariable(method = "attackBlock", at = @At(value = "HEAD", ordinal = 0), ordinal = 0, argsOnly = true)
    private BlockPos attackBlock(BlockPos pos) {
        LeftClickBlockCallback.EVENT.invoker().interact(pos);
        return pos;
    }
}
