package com.awakenedredstone.cubecontroller.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public record GameControlInfo(Identifier identifier, boolean enabled, boolean valueBased, double value) {}
