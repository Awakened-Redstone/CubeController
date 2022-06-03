package com.awakenedredstone.cubecontroller.util;

import com.google.common.collect.Maps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EasyNbtCompound extends NbtCompound {

    public EasyNbtCompound(Map<String, NbtElement> entries) {
        super(Maps.newHashMap(entries));
    }
}
