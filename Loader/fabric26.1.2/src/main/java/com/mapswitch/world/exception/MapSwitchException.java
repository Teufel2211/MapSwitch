package com.mapswitch.world.exception;

public class MapSwitchException extends RuntimeException {
    public MapSwitchException(String message) {
        super(message);
    }

    public MapSwitchException(String message, Throwable cause) {
        super(message, cause);
    }
}


