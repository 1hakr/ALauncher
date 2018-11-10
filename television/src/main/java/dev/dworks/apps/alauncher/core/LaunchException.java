package dev.dworks.apps.alauncher.core;

public final class LaunchException extends RuntimeException {
    public LaunchException(String message) {
        super(message);
    }

    public LaunchException(String message, Throwable cause) {
        super(message, cause);
    }
}
