package io.github.andyrusso.pvplegacyutils;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    // See ModMenu documentation
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder menu = ConfigBuilder.create()
                    .setTitle(Text.literal("PvP Legacy Utils Config menu"))
                    .setParentScreen(parent);
            menu.getOrCreateCategory(Text.literal("General"))
                    .addEntry(toggleOption("options.pvplegacyutils.stats", ConfigOptions.STATS))
                    .addEntry(toggleOption("options.pvplegacyutils.stats_middleclick",
                            ConfigOptions.STATS_MIDDLECLICK,
                            Text.translatable("options.pvplegacyutils.stats_middleclick.tooltip.1"),
                            Text.translatable("options.pvplegacyutils.stats_middleclick.tooltip.2"),
                            Text.translatable("options.pvplegacyutils.stats_middleclick.tooltip.3")
                            )
                    )
                    .addEntry(toggleOption("options.pvplegacyutils.autogg", ConfigOptions.AUTOGG))
                    .addEntry(toggleOption("options.pvplegacyutils.duel", ConfigOptions.DUEL))
                    .addEntry(toggleOption("options.pvplegacyutils.invite", ConfigOptions.INVITE))
                    .addEntry(toggleOption("options.pvplegacyutils.ten_vs_ten", ConfigOptions.TEN_VS_TEN))
                    .addEntry(toggleOption("options.pvplegacyutils.leave_explicit",
                            ConfigOptions.LEAVE_EXPLICIT,
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
                    .addEntry(toggleOption("options.pvplegacyutils.leftclick_leave",
                            ConfigOptions.LEAVE_LEFTCLICK));

            return menu.build();
        };
    }

    private BooleanListEntry toggleOption(String name, ConfigOptions option) {
        return toggleOption(name, option, (Text[]) null);
    }

    private BooleanListEntry toggleOption(String key, ConfigOptions option, Text... tooltip) {
       BooleanToggleBuilder entry = ConfigEntryBuilder.create()
               .startBooleanToggle(Text.translatable(key), PvPLegacyUtilsConfig.get(option))
               .setSaveConsumer(newValue -> PvPLegacyUtilsConfig.set(option, newValue))
               .setDefaultValue(true);

       if (tooltip != null) {
           entry.setTooltip(tooltip);
       }

       return entry.build();
    }
}
