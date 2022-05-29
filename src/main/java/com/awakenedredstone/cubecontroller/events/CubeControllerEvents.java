package com.awakenedredstone.cubecontroller.events;

import com.awakenedredstone.cubecontroller.GameControl;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface CubeControllerEvents {
    Event<CubeControllerEvents> NONE = EventFactory.createArrayBacked(CubeControllerEvents.class, (listeners) -> control -> {});
    Event<CubeControllerEvents> DUMMY = EventFactory.createArrayBacked(CubeControllerEvents.class, (listeners) -> control -> {});

    void invoke(GameControl control);
}
