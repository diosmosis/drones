package com.flarestar.drones.layout.android.exceptions;

public class InvalidManifestException extends Exception {
    public InvalidManifestException() {
    }

    public InvalidManifestException(String message) {
        super(message);
    }

    public InvalidManifestException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidManifestException(Throwable cause) {
        super(cause);
    }

    public InvalidManifestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
