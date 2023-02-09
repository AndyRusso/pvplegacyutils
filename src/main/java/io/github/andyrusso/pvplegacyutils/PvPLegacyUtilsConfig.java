package io.github.andyrusso.pvplegacyutils;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;

/**
 * {@code enum} for different config options.
 */
enum ConfigOptions {
    STATS,
    STATS_MIDDLECLICK,
    AUTOGG,
    DUEL,
    INVITE,
    TEN_VS_TEN,
    LEAVE_EXPLICIT,
    LEAVE_LEFTCLICK
}

/**
 * The class to handle enabling/disabling of options and features.
 */
public class PvPLegacyUtilsConfig {

    // EnumMap for storing different options as key-value pairs in ram to prevent I/O on checks
    private static final EnumMap<ConfigOptions, Boolean> options = new EnumMap<>(ConfigOptions.class);

    // Used in the open method
    private static boolean isMissing = false;

    /**
     * @return Returns the config file as a {@link File} object.
     */
    private static File getFile() {
        return new File(
                FabricLoader.getInstance().getConfigDir().toString(),
                PvPLegacyUtils.MOD_ID + ".toml"
        );
    }

    /**
     * Loads the values of the {@code pvplegacyutils.toml} into the {@link EnumMap}.
     * <p>If the file is absent, a new one with all values set to true created.
     * <p>Should be called before using other methods.
     */
    public static void open() {
        File file = getFile();

        // Checks if the file exists,
        // handy way to avoid an IntelliJ "value not used" warning and create the file in one line
        boolean isPresent;
        try {
            isPresent = !file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (isPresent) {
            Toml toml = new Toml().read(file);

            // For every config option in the enum, load the option from the file to the EnumMap
            for (ConfigOptions option : ConfigOptions.values()) {

                // If the file contains the key then process it normally
                if (toml.contains(option.name())) {
                    options.put(option, toml.getBoolean(option.name()));
                } else {
                    // If the key is new, set it to true and mark it, so it'd get added in the actual file later
                    isMissing = true;
                    options.put(option, true);
                }
            }

            // If the file requires updates, it is updated
            if (isMissing) {
                TomlWriter writer = new TomlWriter();
                try {
                    writer.write(options, file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return;
        }

        TomlWriter toml = new TomlWriter();

        // For every config option in the enum, set it to true in the EnumMap
        for (ConfigOptions option : ConfigOptions.values()) {
            options.put(option, true);
        }

        // Convert the EnumMap to the pvplegacy.toml file
        try {
            toml.write(options, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the config option value from the {@link EnumMap}.
     * @param option The option to lookup.
     * @return Boolean value, true if enabled, false if disabled.
     */
    public static boolean get(ConfigOptions option) {
        return options.get(option);
    }

    /**
     * Sets the specified config option to a specified value.
     * @param option Config option to change.
     * @param value Value to set to the config option.
     */
    public static void set(ConfigOptions option, boolean value) {
        options.put(option, value);
        TomlWriter toml = new TomlWriter();
        try {
            toml.write(options, getFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
