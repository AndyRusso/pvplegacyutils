package io.github.andyrusso.pvplegacyutils.versions.mc1_19.mixin;

import io.github.andyrusso.pvplegacyutils.PvPLegacyUtilsConfig;
import io.github.andyrusso.pvplegacyutils.api.PvPLegacyUtilsAPI;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Comparator;
import java.util.List;

@Mixin(PlayerListHud.class)
public class MixinPlayerListHud {

    @ModifyVariable(method = "render", at = @At(value = "STORE"))
    private List<PlayerListEntry> sortByKills(
            List<PlayerListEntry> list,
            MatrixStack matrices,
            int scaledWindowWidth,
            Scoreboard scoreboard,
            @Nullable ScoreboardObjective objective
    ) {
        if (!PvPLegacyUtilsAPI.isInFFA() || !PvPLegacyUtilsConfig.getInstance().sortByKills) return list;

        Comparator<PlayerListEntry> sortByKills = (PlayerListEntry entry1, PlayerListEntry entry2) -> {
            int entry1Score = scoreboard.getPlayerScore(entry1.getProfile().getName(), objective).getScore();
            int entry2Score = scoreboard.getPlayerScore(entry2.getProfile().getName(), objective).getScore();

            return Integer.compare(entry2Score, entry1Score);
        };

        return list.stream().sorted(sortByKills).toList();
    }
}
