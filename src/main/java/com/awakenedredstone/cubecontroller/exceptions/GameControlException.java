package com.awakenedredstone.cubecontroller.exceptions;

import net.minecraft.util.Identifier;

public class GameControlException extends RuntimeException {
    public GameControlException(Identifier identifier) {
        super(String.format("Error getting game control: %s", identifier));
    }

    public GameControlException(Identifier identifier, Throwable cause) {
        super(String.format("Error getting game control: %s", identifier), cause);
    }
}
