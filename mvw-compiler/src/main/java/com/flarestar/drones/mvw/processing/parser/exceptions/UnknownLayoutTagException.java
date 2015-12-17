package com.flarestar.drones.mvw.processing.parser.exceptions;

/**
 * Created by runic on 9/24/15.
 */
public class UnknownLayoutTagException extends LayoutFileException {
    public UnknownLayoutTagException(String tagName) {
        super("Unknown tag found in layout XML: <" + tagName + ">.");
    }
}
