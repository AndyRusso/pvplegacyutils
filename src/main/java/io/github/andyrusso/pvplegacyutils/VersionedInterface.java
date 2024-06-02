// This file is copied and modified from the Shared Resources mod, with permission by the creators: enjarai and jacg.
// Their repository: https://github.com/enjarai/shared-resources
// Copied file: https://github.com/enjarai/shared-resources/blob/master/src/main/java/nl/enjarai/shared_resources/versioned/SRVersionedEntryPoint.java

package io.github.andyrusso.pvplegacyutils;

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
}
