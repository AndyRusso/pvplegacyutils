package io.github.andyrusso.pvplegacyutils.mixin;

import io.github.andyrusso.pvplegacyutils.api.PvPLegacyUtilsAPI;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {
    /**
     * This mixin is required to determine if the player is in the lobby, duel, or FFA.
     * This method is being run every rendered frame, so it could probably be improved, performance-wise.
     * @param scoreboardObjective2 The scoreboard for the sidebar.
     * @return Un-altered value for scoreboardObjective2.
     */
    @ModifyVariable(method = "render", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    private ScoreboardObjective render(ScoreboardObjective scoreboardObjective2) {
        if (!PvPLegacyUtilsAPI.isVl()) return scoreboardObjective2;

        // Reset all the states
        PvPLegacyUtilsAPI.setIsInDuel(false);
        PvPLegacyUtilsAPI.setIsInFFA(false);
        PvPLegacyUtilsAPI.setIsInLobby(false);

        // If the scoreboard is empty, this means the player is in a Versus Duel
        if (scoreboardObjective2 == null) {
            PvPLegacyUtilsAPI.setIsInDuel(true);
            return null;
        }

        // For every "player" in the sidebar scoreboard, check its decorated name and if it contains a word,
        // because only the Versus Lobby sidebar scoreboard has the "Server" field,
        // and only FFA has a "Total Deaths" field.
        for (ScoreboardPlayerScore player :
                scoreboardObjective2.getScoreboard().getAllPlayerScores(scoreboardObjective2)) {
            Team team = scoreboardObjective2.getScoreboard().getPlayerTeam(player.getPlayerName());
            String name = Team.decorateName(team, Text.literal(player.getPlayerName())).getString();
            if (name.contains("Server")) {
                PvPLegacyUtilsAPI.setIsInLobby(true);
                return scoreboardObjective2;
            } else if (name.contains("Total Deaths")) {
                PvPLegacyUtilsAPI.setIsInFFA(true);
                return scoreboardObjective2;
            }
        }

        // In any other case, the player is in a Versus Duel, like a Solo pop UHC (unreleased game-mode)
        PvPLegacyUtilsAPI.setIsInDuel(true);
        return scoreboardObjective2;
    }
}
