package io.github.andyrusso.pvplegacyutils.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.network.PlayerListEntry;

/**
 * A custom Fabric API event for Versus duel deaths.
 * <p>Detected by players going from some game mode to spectator.
 */
public interface DeathCallback {
    Event<DeathCallback> EVENT = EventFactory.createArrayBacked(DeathCallback.class,
            (listeners) -> (PlayerListEntry playerListEntry) -> {
                for (DeathCallback listener : listeners) {
                    listener.interact(playerListEntry);
                }
            }
    );

    void interact(PlayerListEntry playerListEntry);
}
