package io.github.andyrusso.pvplegacyutils.versions.mc1_20_4;

import io.github.andyrusso.pvplegacyutils.VersionedInterface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.Collection;

public class PvPLegacyUtilsVersioned implements VersionedInterface {
    @Override
    public String getVersion() {
        return "1.20.4";
    }

    @Override
    public void sendChatMessage(String message) {
        if (MinecraftClient.getInstance().getNetworkHandler() != null) {
            MinecraftClient.getInstance().getNetworkHandler().sendChatMessage(message);
        }
    }

    @Override
    public void sendCommand(String command) {
        if (MinecraftClient.getInstance().getNetworkHandler() != null) {
            MinecraftClient.getInstance().getNetworkHandler().sendChatCommand(command);
        }
    }

    @Override
    public SoundEvent getNoteBlockBell() {
        return SoundEvents.BLOCK_NOTE_BLOCK_BELL.value;
    }

    @Override
    public ScoreboardObjective getObjectiveForSlot(Scoreboard scoreboard, int slot) {
        return scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(slot));
    }

    @Override
    public Collection<?> getAllScoreHolders(Scoreboard scoreboard, ScoreboardObjective scoreboardObjective) {
        return scoreboard.getKnownScoreHolders();
    }

    @Override
    public String getScoreHolderName(Object scoreHolder) {
        return ((ScoreHolder) scoreHolder).getNameForScoreboard();
    }
}
