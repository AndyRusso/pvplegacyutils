package io.github.andyrusso.pvplegacyutils.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.text.Text;

/**
 * Custom event to cancel game messages.
 * @see io.github.andyrusso.pvplegacyutils.mixin.MixinClientPlayNetworkHandler
 */
public interface HideGameMessageCallback {
    Event<HideGameMessageCallback> EVENT = EventFactory.createArrayBacked(HideGameMessageCallback.class,
            listeners -> message -> {
                for (HideGameMessageCallback listener : listeners) {
                    if (listener.hide(message)) return true;
                }

                return false;
            }
    );

    boolean hide(Text message);
}
