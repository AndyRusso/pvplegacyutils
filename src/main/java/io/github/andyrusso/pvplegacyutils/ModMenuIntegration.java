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
                    .setTitle(Text.literal("PvP Legacy Utils Config menu"))
                    .setParentScreen(parent)
                    .setSavingRunnable(config::save);

            menu.getOrCreateCategory(Text.literal("General"))
                    .addEntry(
                            toggleOption(
                                    "options.pvplegacyutils.stats",
                                    config.statsRightClick,
                                    value -> config.statsRightClick = value
                            )
                    )
                    .addEntry(
                            toggleOption(
                                    "options.pvplegacyutils.stats_middleclick",
                                    config.statsMiddleClick,
                                    value -> config.statsMiddleClick = value,
                                    Text.translatable("options.pvplegacyutils.stats_middleclick.tooltip.1"),
                                    Text.translatable("options.pvplegacyutils.stats_middleclick.tooltip.2"),
                                    Text.translatable("options.pvplegacyutils.stats_middleclick.tooltip.3")
                            )
                    )
                    .addEntry(
                            toggleOption(
                                    "options.pvplegacyutils.autogg",
                                    config.autogg,
                                    value -> config.autogg = value
                            )
                    )
                    .addEntry(
                            toggleOption(
                                    "options.pvplegacyutils.duel",
                                    config.pingDuel,
                                    value -> config.pingDuel = value
                            )
                    )
                    .addEntry(
                            toggleOption(
                                    "options.pvplegacyutils.invite",
                                    config.pingInvite,
                                    value -> config.pingInvite = value
                            )
                    )
                    .addEntry(
                            toggleOption(
                                    "options.pvplegacyutils.ten_vs_ten",
                                    config.pingTenVSTen,
                                    value -> config.pingTenVSTen = value
                            )
                    )
                    .addEntry(
                            toggleOption(
                                    "options.pvplegacyutils.leave_explicit",
                                    config.leaveExplicitly,
                                    value -> config.leaveExplicitly = value,
                                    Text.translatable("options.pvplegacyutils.leave_explicit.tooltip.1"),
                                    Text.translatable("options.pvplegacyutils.leave_explicit.tooltip.2"),
                                    Text.translatable("options.pvplegacyutils.leave_explicit.tooltip.3"),
                                    Text.literal(" "),
                                    Text.translatable("options.pvplegacyutils.leave_explicit.tooltip.4"),
                                    Text.translatable("options.pvplegacyutils.leave_explicit.tooltip.5"),
                                    Text.translatable("options.pvplegacyutils.leave_explicit.tooltip.6"),
                                    Text.translatable("options.pvplegacyutils.leave_explicit.tooltip.7")
                            )
                    )
                    .addEntry(
                            toggleOption(
                                    "options.pvplegacyutils.leftclick_leave",
                                    config.leaveLeftClick,
                                    value -> config.leaveLeftClick = value
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
