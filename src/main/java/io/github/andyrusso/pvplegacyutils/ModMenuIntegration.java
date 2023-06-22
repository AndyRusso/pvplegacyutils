package io.github.andyrusso.pvplegacyutils;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class ModMenuIntegration implements ModMenuApi {
    // See ModMenu documentation
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            PvPLegacyUtilsConfig config = PvPLegacyUtilsConfig.getInstance();
            ConfigBuilder menu = ConfigBuilder.create()
                    .setTitle(Text.translatable("pvplegacyutils.title"))
                    .setParentScreen(parent)
                    .setSavingRunnable(config::save);

            menu.getOrCreateCategory(Text.translatable("pvplegacyutils.general"))
                    .addEntry(
                            toggleOption(
                                    "pvplegacyutils.stats.rightClick",
                                    config.statsRightClick,
                                    value -> config.statsRightClick = value
                            )
                    )
                    .addEntry(
                            toggleOption(
                                    "pvplegacyutils.stats.middleClick",
                                    config.statsMiddleClick,
                                    value -> config.statsMiddleClick = value,
                                    Text.translatable("pvplegacyutils.stats.middleClick.tooltip.1"),
                                    Text.translatable("pvplegacyutils.stats.middleClick.tooltip.2"),
                                    Text.translatable("pvplegacyutils.stats.middleClick.tooltip.3")
                            )
                    )
                    .addEntry(
                            toggleOption(
                                    "pvplegacyutils.ping.duel",
                                    config.pingDuel,
                                    value -> config.pingDuel = value
                            )
                    )
                    .addEntry(
                            toggleOption(
                                    "pvplegacyutils.ping.invite",
                                    config.pingInvite,
                                    value -> config.pingInvite = value
                            )
                    )
                    .addEntry(
                            toggleOption(
                                    "pvplegacyutils.ping.tenVSTen",
                                    config.pingTenVSTen,
                                    value -> config.pingTenVSTen = value
                            )
                    )
                    .addEntry(
                            toggleOption(
                                    "pvplegacyutils.ping.godGame",
                                    config.pingGodGame,
                                    value -> config.pingGodGame = value
                            )
                    )
                    .addEntry(
                            toggleOption(
                                    "pvplegacyutils.leave.explicit",
                                    config.leaveExplicitly,
                                    value -> config.leaveExplicitly = value,
                                    Text.translatable("pvplegacyutils.leave.explicit.tooltip.1"),
                                    Text.translatable("pvplegacyutils.leave.explicit.tooltip.2"),
                                    Text.translatable("pvplegacyutils.leave.explicit.tooltip.3"),
                                    Text.literal(" "),
                                    Text.translatable("pvplegacyutils.leave.explicit.tooltip.4"),
                                    Text.translatable("pvplegacyutils.leave.explicit.tooltip.5"),
                                    Text.translatable("pvplegacyutils.leave.explicit.tooltip.6"),
                                    Text.translatable("pvplegacyutils.leave.explicit.tooltip.7")
                            )
                    )
                    .addEntry(
                            toggleOption(
                                    "pvplegacyutils.leave.leftClick",
                                    config.leaveLeftClick,
                                    value -> config.leaveLeftClick = value
                            )
                    );

            menu.getOrCreateCategory(Text.translatable("pvplegacyutils.versusDuels"))
                    .addEntry(
                            toggleOption(
                                    "pvplegacyutils.deathParticles",
                                    config.deathParticles,
                                    value -> config.deathParticles = value
                            )
                    )
                    .addEntry(
                            toggleOption(
                                    "pvplegacyutils.autogg",
                                    config.autogg,
                                    value -> config.autogg = value
                            )
                    );

            menu.getOrCreateCategory(Text.translatable("pvplegacyutils.ffa"))
                    .addEntry(
                            toggleOption(
                                    "pvplegacyutils.sortByKills",
                                    config.sortByKills,
                                    value -> config.sortByKills = value
                            )
                    );

            return menu.build();
        };
    }

    private BooleanListEntry toggleOption(String name, boolean value, Consumer<Boolean> save) {
        return toggleOption(name, value, save, (Text[]) null);
    }

    /**
     * Helper function to generate Cloth Config boolean entries.
     * @param key Translation key for description of the entry.
     * @param value Whether the entry should be "Yes" or "No".
     * @param save Lambda to update the actual config value.
     * @param tooltip List of {@link Text} objects for the tooltip.
     * @return {@link BooleanListEntry} object to be passed in
     * {@link
     * me.shedaniel.clothconfig2.api.ConfigCategory#addEntry(me.shedaniel.clothconfig2.api.AbstractConfigListEntry)
     * addEntry()}
     */
    private BooleanListEntry toggleOption(String key, boolean value, Consumer<Boolean> save, Text... tooltip) {
       BooleanToggleBuilder entry = ConfigEntryBuilder.create()
               .startBooleanToggle(Text.translatable(key), value)
               .setSaveConsumer(save)
               .setDefaultValue(true);

       if (tooltip != null) {
           entry.setTooltip(tooltip);
       }

       return entry.build();
    }
}
