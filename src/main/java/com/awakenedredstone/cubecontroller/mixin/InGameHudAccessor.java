package com.awakenedredstone.cubecontroller.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InGameHud.class)
@Environment(value= EnvType.CLIENT)
public interface InGameHudAccessor {
    @Accessor float getAutosaveIndicatorAlpha();
    @Accessor float getLastAutosaveIndicatorAlpha();
}
