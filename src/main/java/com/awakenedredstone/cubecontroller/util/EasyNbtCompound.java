package com.awakenedredstone.cubecontroller.util;

import com.google.common.collect.Maps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.Map;

public class EasyNbtCompound extends NbtCompound {

    public EasyNbtCompound(Map<String, NbtElement> entries) {
        super(Maps.newHashMap(entries));
    }
}
