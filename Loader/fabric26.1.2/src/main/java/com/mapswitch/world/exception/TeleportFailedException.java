package com.mapswitch.world.exception;

public final class TeleportFailedException extends MapSwitchException {
    public TeleportFailedException(String message) {
        super(message);
    }

    public TeleportFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}


