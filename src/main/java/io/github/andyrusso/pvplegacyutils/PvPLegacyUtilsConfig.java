package io.github.andyrusso.pvplegacyutils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The class to handle enabling/disabling of options and features.
 */
public class PvPLegacyUtilsConfig {
    private static final Path file = FabricLoader.getInstance().getConfigDir().resolve(
            PvPLegacyUtils.MOD_ID + ".json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static PvPLegacyUtilsConfig instance;

    //  General settings
    // Check someone's stats
    public boolean statsRightClick = true;
    // Check own stats
    public boolean statsMiddleClick = true;
    public boolean pingDuel = true;
    public boolean pingInvite = true;
    public boolean pingTenVSTen = true;
    public boolean pingGodGame = true;
    public boolean leaveExplicitly = true;
    public boolean leaveLeftClick = true;
    public boolean hideTips = false;
    //  Versus duels settings
    public boolean deathParticles = true;
    public boolean hideNoSF = false;
    public boolean autogg = true;
    public boolean autoggStartGame = false;
    public boolean autoggStartRound = false;
    public boolean autoggEndRound = true;
    public boolean autoggEndGame = false;
    public String autoggStartGameText = "glhf";
    public String autoggStartRoundText = "gl";
    public String autoggEndRoundText = "gg";
    public String autoggEndGameText = "wp";
    //  FFA settings
    public boolean sortByKills = true;

    public void save() {
        try {
            Files.writeString(file, GSON.toJson(this));
        } catch (IOException e) {
            PvPLegacyUtils.LOGGER.error(PvPLegacyUtils.MOD_ID + " could not save the config.");
            throw new RuntimeException(e);
        }
    }

    public static PvPLegacyUtilsConfig getInstance() {
        if (instance == null) {
            try {
                instance = GSON.fromJson(Files.readString(file), PvPLegacyUtilsConfig.class);
            } catch (IOException exception) {
                PvPLegacyUtils.LOGGER.warn(PvPLegacyUtils.MOD_ID + " couldn't load the config, using defaults.");
                instance = new PvPLegacyUtilsConfig();
            }
        }

        return instance;
    }
}
