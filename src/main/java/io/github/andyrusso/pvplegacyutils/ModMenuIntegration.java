package io.github.andyrusso.pvplegacyutils;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.StringListEntry;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import me.shedaniel.clothconfig2.impl.builders.StringFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Optional;
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

            ConfigEntryBuilder entryBuilder = menu.entryBuilder();

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
                                    true,
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
                                    true,
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

            SubCategoryBuilder autogg = entryBuilder.startSubCategory(Text.translatable("pvplegacyutils.autogg"));

            autogg.addAll(Arrays.asList(
                    toggleOption(
                            "pvplegacyutils.autogg.toggle",
                            config.autogg,
                            value -> config.autogg = value
                    ),
                    toggleOptionFalse(
                            "pvplegacyutils.autogg.startGame",
                            config.autoggStartGame,
                            value -> config.autoggStartGame = value
                    ),
                    strField(
                            "pvplegacyutils.autogg.startGame.text",
                            config.autoggStartGameText,
                            (string) -> config.autoggStartGameText = string,
                            "glhf"
                    ),
                    toggleOptionFalse(
                            "pvplegacyutils.autogg.startRound",
                            config.autoggStartRound,
                            value -> config.autoggStartRound = value
                    ),
                    strField(
                            "pvplegacyutils.autogg.startRound.text",
                            config.autoggStartRoundText,
                            (string) -> config.autoggStartRoundText = string,
                            "gl"
                    ),
                    toggleOption(
                            "pvplegacyutils.autogg.endRound",
                            config.autoggEndRound,
                            value -> config.autoggEndRound = value,
                            true,
                            Text.translatable("pvplegacyutils.autogg.endRound.tooltip.1"),
                            Text.translatable("pvplegacyutils.autogg.endRound.tooltip.2"),
                            Text.translatable("pvplegacyutils.autogg.endRound.tooltip.3")
                    ),
                    strField(
                            "pvplegacyutils.autogg.endRound.text",
                            config.autoggEndRoundText,
                            (string) -> config.autoggEndRoundText = string,
                            "gg"
                    ),
                    toggleOptionFalse(
                            "pvplegacyutils.autogg.endGame",
                            config.autoggEndGame,
                            value -> config.autoggEndGame = value
                    ),
                    strField(
                            "pvplegacyutils.autogg.endGame.text",
                            config.autoggEndGameText,
                            (string) -> config.autoggEndGameText = string,
                            "wp"
                    )
            ));
            autogg.setExpanded(true);

            menu.getOrCreateCategory(Text.translatable("pvplegacyutils.versusDuels"))
                    .addEntry(
                            toggleOption(
                                    "pvplegacyutils.deathParticles",
                                    config.deathParticles,
                                    value -> config.deathParticles = value
                            )
                    )
                    .addEntry(
                            toggleOptionFalse(
                                    "pvplegacyutils.hideNoSF",
                                    config.hideNoSF,
                                    value -> config.hideNoSF = value
                            )
                    )
                    .addEntry(autogg.build());

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

    private BooleanListEntry toggleOptionFalse(String name, boolean value, Consumer<Boolean> save) {
        return toggleOption(name, value, save, false, (Text[]) null);
    }

    private BooleanListEntry toggleOption(String name, boolean value, Consumer<Boolean> save) {
        return toggleOption(name, value, save, true, (Text[]) null);
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
    private BooleanListEntry toggleOption(String key, boolean value, Consumer<Boolean> save, boolean default_, Text... tooltip) {
       BooleanToggleBuilder entry = ConfigEntryBuilder.create()
               .startBooleanToggle(Text.translatable(key), value)
               .setSaveConsumer(save)
               .setDefaultValue(default_);

       if (tooltip != null) {
           entry.setTooltip(tooltip);
       }

       return entry.build();
    }

    private StringListEntry strField(String key, String value, Consumer<String> save, String default_) {
        StringFieldBuilder entry = ConfigEntryBuilder.create()
                .startStrField(Text.translatable(key), value)
                .setSaveConsumer(save)
                .setDefaultValue(default_)
                .setErrorSupplier(
                        string -> {
                            if (!string.isBlank()) return Optional.empty();
                            return Optional.of(Text.translatable("pvplegacyutils.fieldEmpty"));
                        }
                );

        return entry.build();
    }
}
