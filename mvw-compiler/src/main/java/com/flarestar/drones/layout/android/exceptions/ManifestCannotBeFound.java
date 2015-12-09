package com.flarestar.drones.layout.android.exceptions;

public class ManifestCannotBeFound extends InvalidManifestException {
    public ManifestCannotBeFound(String message) {
        super(message);
    }

    public ManifestCannotBeFound(String message, Throwable cause) {
        super(message, cause);
    }

    public ManifestCannotBeFound(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
