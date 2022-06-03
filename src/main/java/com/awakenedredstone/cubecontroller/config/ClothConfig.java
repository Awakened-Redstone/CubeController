package com.awakenedredstone.cubecontroller.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ClothConfig {
    public Screen build(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create().setTitle(Text.translatable("title.cubecontroller.config"));

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.cubecontroller.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startTextDescription(Text.literal("In development").formatted(Formatting.RED)).build());

    return builder.build();
    }
}
