// This file is copied and modified from the Shared Resources mod, with permission by the creators: enjarai and jacg.
// Their repository: https://github.com/enjarai/shared-resources
// Copied file: https://github.com/enjarai/shared-resources/blob/master/src/main/java/nl/enjarai/shared_resources/versioned/SRVersionedEntryPoint.java

package io.github.andyrusso.pvplegacyutils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.sound.SoundEvent;

import java.util.Collection;

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
        ClientPlayerEntity player = client.player;

        if (player == null) {
            return;
        }

        try {
            client.player.clientWorld.addFireworkParticle(
                    x,
                    y,
                    z,
                    0,
                    0,
                    0,
                    StringNbtReader.parse("{Flight:0,Explosions:[{Colors:[I;44782,15724017]}]}")
            );
        } catch (CommandSyntaxException e) {
            // Unreachable
            throw new RuntimeException(e);
        }
    }
}
