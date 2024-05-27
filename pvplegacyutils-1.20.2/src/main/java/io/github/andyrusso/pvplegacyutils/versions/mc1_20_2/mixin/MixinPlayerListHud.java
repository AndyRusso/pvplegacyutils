package io.github.andyrusso.pvplegacyutils.versions.mc1_20_2.mixin;

import io.github.andyrusso.pvplegacyutils.PvPLegacyUtilsConfig;
import io.github.andyrusso.pvplegacyutils.api.PvPLegacyUtilsAPI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
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
            int entry1Score = scoreboard.getPlayerScore(entry1.getProfile().getName(), objective).getScore();
            int entry2Score = scoreboard.getPlayerScore(entry2.getProfile().getName(), objective).getScore();

            return Integer.compare(entry2Score, entry1Score);
        };

        args.set(0, sortByKills);
    }
}
