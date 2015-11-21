package com.flarestar.drones.layout.android.exceptions;

/**
 * Created by runic on 11/21/15.
 */
public class ManifestCannotBeParsed extends InvalidManifestException {
    public ManifestCannotBeParsed(String message) {
        super(message);
    }

    public ManifestCannotBeParsed(String message, Throwable cause) {
        super(message, cause);
    }

    public ManifestCannotBeParsed(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
