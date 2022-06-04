package com.awakenedredstone.cubecontroller;

import com.awakenedredstone.cubecontroller.events.CubeControllerEvents;
import com.awakenedredstone.cubecontroller.util.MessageUtils;
import com.awakenedredstone.cubecontroller.util.PacketUtils;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GameControl {
    private final Identifier identifier;
    private final Event<CubeControllerEvents> event;
    private final boolean valueBased;

    private double value = 0;
    private boolean enabled = false;
    private NbtCompound nbtData;

    public GameControl(@NotNull Identifier identifier) {
        this(identifier, CubeControllerEvents.NONE, true);
    }

    public GameControl(@NotNull Identifier identifier, @NotNull Event<CubeControllerEvents> event) {
        this(identifier, event, false);
    }

    public GameControl(@NotNull Identifier identifier, @NotNull Event<CubeControllerEvents> event, boolean valueBased) {
        this(identifier, event, valueBased, new NbtCompound());
    }

    public GameControl(@NotNull Identifier identifier, @NotNull Event<CubeControllerEvents> event, boolean valueBased, @NotNull NbtCompound nbt) {
        this.identifier = identifier;
        this.event = event;
        this.valueBased = valueBased;
        this.nbtData = nbt;
    }

    public boolean isWithinLimit(double value) {
        NbtElement nbtElement = getNbt().get("limit");
        if (nbtElement instanceof NbtCompound limit) {
            return value >= limit.getDouble("min") && value <= limit.getDouble("max");
        } else if (nbtElement instanceof AbstractNbtNumber limit) {
            return value <= limit.doubleValue();
        } else if (nbtElement == null) {
            return true;
        } else {
            return true;
        }
    }

    public double withinLimit(double value) {
        NbtElement nbtElement = getNbt().get("limit");
        if (nbtElement instanceof NbtCompound limit) {
            return MathHelper.clamp(value, limit.getDouble("min"), limit.getDouble("max"));
        } else if (nbtElement instanceof AbstractNbtNumber limit) {
            return Math.min(value, limit.doubleValue());
        } else if (nbtElement == null) {
            return value;
        } else {
            return value;
        }
    }

    public double value() {
        return value;
    }

    public void value(double value) {
        if (!valueBased) return;
        this.value = withinLimit(value);
        if (CubeController.getServer() != null) {
            if (isVisible()) {
                MessageUtils.broadcastPacket(CubeController.identifier("info_update"), PacketUtils.controlInfoUpdate(PacketByteBufs.create(), this));
            }
        }
    }

    public boolean enabled() {
        return enabled;
    }

    public void enabled(boolean enabled) {
        this.enabled = enabled;
        if (CubeController.getServer() != null) {
            if (isVisible()) {
                MessageUtils.broadcastPacket(CubeController.identifier("info_update"), PacketUtils.controlInfoUpdate(PacketByteBufs.create(), this));
            } else {
                MessageUtils.broadcastPacket(CubeController.identifier("remove_info"), PacketByteBufs.create().writeIdentifier(identifier()));
            }
        }
    }

    public Identifier identifier() {
        return identifier;
    }

    @NotNull
    public Event<CubeControllerEvents> event() {
        return event;
    }

    public boolean hasEvent() {
        return event != CubeControllerEvents.NONE;
    }

    public boolean valueBased() {
        return valueBased;
    }

    public NbtCompound getNbt() {
        return nbtData.copy();
    }

    public void setNbt(NbtCompound nbt) {
        nbtData = nbt;
        if (CubeController.getServer() != null) {
            if (isVisible()) {
                MessageUtils.broadcastPacket(CubeController.identifier("info_update"), PacketUtils.controlInfoUpdate(PacketByteBufs.create(), this));
            } else {
                MessageUtils.broadcastPacket(CubeController.identifier("remove_info"), PacketByteBufs.create().writeIdentifier(identifier()));
            }
        }
    }

    public void copyNbt(NbtCompound nbt) {
        nbtData.copyFrom(nbt);
        if (CubeController.getServer() != null) {
            if (isVisible()) {
                MessageUtils.broadcastPacket(CubeController.identifier("info_update"), PacketUtils.controlInfoUpdate(PacketByteBufs.create(), this));
            } else {
                MessageUtils.broadcastPacket(CubeController.identifier("remove_info"), PacketByteBufs.create().writeIdentifier(identifier()));
            }
        }
    }

    public boolean invoke() {
        if (enabled && hasEvent()) event.invoker().invoke(this);
        return enabled && hasEvent();
    }

    private boolean isVisible() {
        return (enabled || nbtData.getBoolean("alwaysVisible")) && !nbtData.getBoolean("hideInfo");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GameControl) obj;
        return Objects.equals(this.identifier, that.identifier) &&
                Objects.equals(this.enabled, that.enabled) &&
                Objects.equals(this.valueBased, that.valueBased) &&
                Objects.equals(this.value, that.value) &&
                Objects.equals(this.event, that.event) &&
                Objects.equals(this.nbtData, that.nbtData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, enabled, valueBased, value, event, nbtData);
    }

    @Override
    public String toString() {
        return "GameControl[" +
                "identifier=" + identifier + ", " +
                "enabled=" + enabled + ", " +
                "valueBased=" + valueBased + ", " +
                "value=" + value + ", " +
                "event=" + event + ", " +
                "nbt=" + nbtData + ']';
    }

}
