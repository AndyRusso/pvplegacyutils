package io.github.andyrusso.pvplegacyutils.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

/**
 * This is an event for when you attack (by default left-click) a block.
 *
 * <p>It's different from the {@code Fabric API}'s {@link net.fabricmc.fabric.api.event.player.AttackBlockCallback}
 * because it runs if the player isn't in adventure mode too, and as a side effect it also runs when the block is
 * outside the border, but this shouldn't be an issue for PvP Legacy Utils and mods using the API.
 *
 * @see io.github.andyrusso.pvplegacyutils.mixin.MixinMinecraftClient
 */
public interface LeftClickBlockCallback {
    Event<LeftClickBlockCallback> EVENT = EventFactory.createArrayBacked(LeftClickBlockCallback.class,
        (listeners) -> (BlockPos pos) -> {
            for (LeftClickBlockCallback listener: listeners) {
                ActionResult result = listener.interact(pos);

                if (result != ActionResult.PASS) {
                    return result;
                }
            }

            return ActionResult.PASS;
        }
    );

    ActionResult interact(BlockPos pos);
}
