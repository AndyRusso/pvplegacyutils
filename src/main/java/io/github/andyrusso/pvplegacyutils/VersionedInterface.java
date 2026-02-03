// This file is copied and modified from the Shared Resources mod, with permission by the creators: enjarai and jacg.
// Their repository: https://github.com/enjarai/shared-resources
// Copied file: https://github.com/enjarai/shared-resources/blob/master/src/main/java/nl/enjarai/shared_resources/versioned/SRVersionedEntryPoint.java

package io.github.andyrusso.pvplegacyutils;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.sound.SoundEvent;

import java.util.Collection;
import java.util.List;

public interface VersionedInterface {
    String getVersion();
    void sendChatMessage(String message);
    void sendCommand(String command);
    SoundEvent getNoteBlockBell();
    ScoreboardObjective getObjectiveForSlot(Scoreboard scoreboard, int slot);
    Collection<?> getAllScoreHolders(Scoreboard scoreboard, ScoreboardObjective scoreboardObjective);
    String getScoreHolderName(Object scoreHolder);
    default void addFireworkParticle(double x, double y, double z) {
        MinecraftClient client = MinecraftClient.getInstance();
        var world = client.world;

        if (world == null) {
            return;
        }

        world.addFireworkParticle(
                x,
                y,
                z,
                0,
                0,
                0,
                List.of(
                        new FireworkExplosionComponent(
                                FireworkExplosionComponent.Type.SMALL_BALL,
                                IntList.of(44782, 157249),
                                IntList.of(),
                                false,
                                false
                        )
                )
        );
    }
}
