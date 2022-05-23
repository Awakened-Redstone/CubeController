package com.awakenedredstone.cubecontroller.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;

@FunctionalInterface
public interface MinecraftClientCallback {
    Event<MinecraftClientCallback> SPRITE_MANAGER = EventFactory.createArrayBacked(MinecraftClientCallback.class, (listeners) -> client -> {
        for (MinecraftClientCallback event : listeners) {
            event.invoke(client);
        }
    });

    void invoke(MinecraftClient client);
}
