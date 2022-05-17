package com.awakenedredstone.cubecontroller.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface CubeControllerEvents {
    Event<CubeControllerEvents> NONE = EventFactory.createArrayBacked(CubeControllerEvents.class, (listeners) -> () -> {});
    Event<CubeControllerEvents> DUMMY = EventFactory.createArrayBacked(CubeControllerEvents.class, (listeners) -> () -> {});

    Event<CubeControllerEvents> POTION_CHAOS = EventFactory.createArrayBacked(CubeControllerEvents.class, (listeners) -> () -> {
        for (CubeControllerEvents event : listeners) {
            event.invoke();
        }
    });

    void invoke();
}
