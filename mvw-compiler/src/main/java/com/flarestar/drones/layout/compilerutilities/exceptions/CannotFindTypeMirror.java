package com.flarestar.drones.layout.compilerutilities.exceptions;

public class CannotFindTypeMirror extends RuntimeException {
    public CannotFindTypeMirror(String type) {
        super("Type '" + type + "' cannot be found. (This is not expected.)");
    }
}
