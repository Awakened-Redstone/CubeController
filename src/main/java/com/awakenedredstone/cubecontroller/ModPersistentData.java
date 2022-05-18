package com.awakenedredstone.cubecontroller;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

public class ModPersistentData extends PersistentState {
    private final NbtCompound modPersistentData;

    public ModPersistentData() {
        this.modPersistentData = new NbtCompound();
    }

    public ModPersistentData(NbtCompound modPersistentData) {
        this.modPersistentData = modPersistentData;
    }

    public NbtCompound getData() {
        return modPersistentData;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return modPersistentData;
    }
}
