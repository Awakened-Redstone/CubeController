package com.awakenedredstone.cubecontroller;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

import java.io.File;

public class CubeData extends PersistentState {

    public static CubeData fromNbt(NbtCompound nbt) {
        try {
            CubeData cubeData = new CubeData();
            for (GameControl control : CubeController.GAME_CONTROL) {
                if (nbt.contains(control.identifier().toString())) {
                    control.value(nbt.getCompound(control.identifier().toString()).getDouble("value"));
                    control.enabled(nbt.getCompound(control.identifier().toString()).getBoolean("enabled"));
                    control.setNbt(nbt.getCompound(control.identifier().toString()).getCompound("data"));
                }
            }
            return cubeData;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        for (GameControl control : CubeController.GAME_CONTROL) {
            NbtCompound controlNbt = new NbtCompound();
            controlNbt.putDouble("value", control.value());
            controlNbt.putBoolean("enabled", control.enabled());
            controlNbt.put("data", control.getNbt());

            nbt.put(control.identifier().toString(), controlNbt);
        }
        return nbt;
    }

    @Override
    public void save(File file) {
        this.markDirty();
        super.save(file);
    }
}
