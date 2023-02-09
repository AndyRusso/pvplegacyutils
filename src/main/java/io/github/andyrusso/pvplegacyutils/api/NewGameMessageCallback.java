package io.github.andyrusso.pvplegacyutils.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.text.Text;

/**
 * A custom event for new game messages.
 * @see io.github.andyrusso.pvplegacyutils.mixin.MixinClientPlayNetworkHandler
 */
public interface NewGameMessageCallback {
    Event<NewGameMessageCallback> EVENT = EventFactory.createArrayBacked(NewGameMessageCallback.class,
            (listeners) -> (Text message) -> {
                PvPLegacyUtilsAPI.onGameMessage(message);

                for (NewGameMessageCallback listener : listeners) {
                    listener.interact(message);
                }
            }

    );

    void interact(Text message);
}
