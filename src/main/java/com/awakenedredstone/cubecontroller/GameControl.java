package com.awakenedredstone.cubecontroller;

import com.awakenedredstone.cubecontroller.events.CubeControllerEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
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

    public double value() {
        return value;
    }

    public void value(double value) {
        this.value = value;
    }

    public boolean enabled() {
        return enabled;
    }

    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Identifier identifier() {
        return identifier;
    }

    @NotNull
    public Event<CubeControllerEvents> event() {
        return event;
    }

    public boolean valueBased() {
        return valueBased;
    }

    public NbtCompound nbtData() {
        return nbtData;
    }

    public void nbtData(NbtCompound nbt) {
        nbtData = nbt;
    }

    public void invoke() {
        event.invoker().invoke();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GameControl) obj;
        return Objects.equals(this.identifier, that.identifier) &&
                Objects.equals(this.event, that.event) &&
                Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, event, value);
    }

    @Override
    public String toString() {
        return "GameControl[" +
                "identifier=" + identifier + ", " +
                "event=" + event + ", " +
                "value=" + value + ']';
    }

}
