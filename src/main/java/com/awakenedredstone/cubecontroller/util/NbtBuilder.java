package com.awakenedredstone.cubecontroller.util;

import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NbtBuilder {
    private final Map<String, NbtElement> entries = new HashMap<>();

    public static NbtBuilder create() {
        return new NbtBuilder();
    }

    public NbtCompound build() {
        return new EasyNbtCompound(entries);
    }

    public NbtBuilder addElement(String key, NbtElement value) {
        entries.put(key, value);
        return this;
    }

    public NbtBuilder addCompound(String key, NbtElement value) {
        entries.put(key, value);
        return this;
    }

    public NbtBuilder addDouble(String key, double value) {
        entries.put(key, NbtDouble.of(value));
        return this;
    }

    public NbtBuilder addFloat(String key, float value) {
        entries.put(key, NbtFloat.of(value));
        return this;
    }

    public NbtBuilder addLong(String key, long value) {
        entries.put(key, NbtLong.of(value));
        return this;
    }

    public NbtBuilder addInt(String key, int value) {
        entries.put(key, NbtInt.of(value));
        return this;
    }

    public NbtBuilder addShort(String key, short value) {
        entries.put(key, NbtShort.of(value));
        return this;
    }

    public NbtBuilder addByte(String key, byte value) {
        entries.put(key, NbtByte.of(value));
        return this;
    }

    public NbtBuilder addBoolean(String key, boolean value) {
        entries.put(key, NbtByte.of(value));
        return this;
    }

    public NbtBuilder addString(String key, String value) {
        entries.put(key, NbtString.of(value));
        return this;
    }

    public NbtBuilder addLongArray(String key, long... values) {
        entries.put(key, new NbtLongArray(values));
        return this;
    }

    public NbtBuilder addIntArray(String key, int... values) {
        entries.put(key, new NbtIntArray(values));
        return this;
    }

    public NbtBuilder addByteArray(String key, byte... values) {
        entries.put(key, new NbtByteArray(values));
        return this;
    }
}
