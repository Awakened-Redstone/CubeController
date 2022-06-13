package com.awakenedredstone.cubecontroller.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ControlEvents extends CubeControllerEvents {
    Event<CubeControllerEvents> POTION_CHAOS = EventFactory.createArrayBacked(CubeControllerEvents.class, (listeners) -> control -> {
        for (CubeControllerEvents event : listeners) {
            event.invoke(control);
        }
    });

    Event<CubeControllerEvents> SHUFFLE_INVENTORY = EventFactory.createArrayBacked(CubeControllerEvents.class, (listeners) -> control -> {
        for (CubeControllerEvents event : listeners) {
            event.invoke(control);
        }
    });

    Event<CubeControllerEvents> SCULK_CHAOS = EventFactory.createArrayBacked(CubeControllerEvents.class, (listeners) -> control -> {
        for (CubeControllerEvents event : listeners) {
            event.invoke(control);
        }
    });
}
