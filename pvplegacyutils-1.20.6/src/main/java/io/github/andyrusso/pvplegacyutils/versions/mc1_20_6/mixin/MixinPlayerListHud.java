package io.github.andyrusso.pvplegacyutils.versions.mc1_20_6.mixin;

import io.github.andyrusso.pvplegacyutils.PvPLegacyUtilsConfig;
import io.github.andyrusso.pvplegacyutils.api.PvPLegacyUtilsAPI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Comparator;

@Mixin(PlayerListHud.class)
public class MixinPlayerListHud {
    @ModifyArgs(
            method = "collectPlayerEntries",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;sorted(Ljava/util/Comparator;)Ljava/util/stream/Stream;"
            )
    )
    private void sortByKills(Args args) {
        if (!PvPLegacyUtilsAPI.isInFFA() || !PvPLegacyUtilsConfig.getInstance().sortByKills) return;

        if (MinecraftClient.getInstance().world == null) return;
        Scoreboard scoreboard = MinecraftClient.getInstance().world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.LIST);

        Comparator<PlayerListEntry> sortByKills = (PlayerListEntry entry1, PlayerListEntry entry2) -> {
            ReadableScoreboardScore entry1ReadableScore =
                    scoreboard.getScore(ScoreHolder.fromName(entry1.getProfile().getName()), objective);
            ReadableScoreboardScore entry2ReadableScore =
                    scoreboard.getScore(ScoreHolder.fromName(entry2.getProfile().getName()), objective);

            int entry1Score = entry1ReadableScore == null ? 0 : entry1ReadableScore.getScore();
            int entry2Score = entry2ReadableScore == null ? 0 : entry2ReadableScore.getScore();

            return Integer.compare(entry2Score, entry1Score);
        };

        args.set(0, sortByKills);
    }
}
