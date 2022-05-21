package com.awakenedredstone.cubecontroller.util;

import com.awakenedredstone.cubecontroller.CubeController;
import com.awakenedredstone.cubecontroller.GameControl;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

import java.util.stream.Stream;

public class PacketUtils {

    public static PacketByteBuf controlInfoBulkUpdate(PacketByteBuf buf) {
        NbtCompound nbt = new NbtCompound();
        Stream<GameControl> controlStream = CubeController.GAME_CONTROL.stream()
                .filter(control -> !control.getNbt().getBoolean("hideInfo"))
                .filter(control -> control.getNbt().getBoolean("alwaysVisible") || control.enabled());
        for (GameControl control : controlStream.toList()) {
            NbtCompound info = new NbtCompound();
            info.putBoolean("enabled", control.enabled());
            info.putBoolean("valueBased", control.valueBased());
            if (control.valueBased()) info.putDouble("value", control.value());
            nbt.put(control.identifier().toString(), info);
        }
        return buf.writeNbt(nbt);
    }

    public static PacketByteBuf controlInfoUpdate(PacketByteBuf buf, GameControl control) {
        buf.writeIdentifier(control.identifier())
                .writeBoolean(control.enabled())
                .writeBoolean(control.valueBased())
                .writeDouble(control.value());
        return buf;
    }
}
