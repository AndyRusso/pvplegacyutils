package io.github.andyrusso.pvplegacyutils.versions.mc1_20_2;

import io.github.andyrusso.pvplegacyutils.VersionedInterface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class PvPLegacyUtilsVersioned implements VersionedInterface {
    @Override
    public String getVersion() {
        return "1.20.2";
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
}
