package com.flarestar.drones.layout.view;

import com.flarestar.drones.layout.parser.exceptions.InvalidStyleValue;
import com.flarestar.drones.layout.view.styleprocessors.BoxModelStyles;
import com.google.inject.Singleton;

/**
 * TODO
 */
@Singleton
public class StyleProcessor {
    private final Style[] STYLES = {
        new BoxModelStyles(),
    };

    public String process(ViewNode node) throws InvalidStyleValue {
        StringBuilder result = new StringBuilder();
        for (Style style : STYLES) {
            style.toCode(node, result);
        }
        return result.toString();
    }
}
