package com.awakenedredstone.cubecontroller;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;

import java.util.Map;

public class CubeData extends PersistentState {

    public static CubeData fromNbt(NbtCompound nbt) {
        CubeData cubeData = new CubeData();
        for (Map.Entry<RegistryKey<GameControl>, GameControl> controlEntry : CubeController.GAME_CONTROL.getEntrySet()) {
            if (nbt.contains(controlEntry.getKey().toString())) {
                controlEntry.getValue().value = nbt.getCompound(controlEntry.getKey().getValue().toString()).getDouble("value");
                controlEntry.getValue().enabled = nbt.getCompound(controlEntry.getKey().getValue().toString()).getBoolean("enabled");
                controlEntry.getValue().nbtData().copyFrom(nbt.getCompound(controlEntry.getKey().getValue().toString()).getCompound("data"));
            }
        }
        return cubeData;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        for (Map.Entry<RegistryKey<GameControl>, GameControl> controlEntry : CubeController.GAME_CONTROL.getEntrySet()) {
            NbtCompound controlNbt = new NbtCompound();
            controlNbt.putDouble("value", controlEntry.getValue().value);
            controlNbt.putBoolean("enabled", controlEntry.getValue().enabled);
            controlNbt.put("data", controlEntry.getValue().nbtData());

            nbt.put(controlEntry.getKey().getValue().toString(), controlNbt);
        }
        return nbt;
    }
}
