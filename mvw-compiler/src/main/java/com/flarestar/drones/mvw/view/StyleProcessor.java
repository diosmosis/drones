package com.flarestar.drones.mvw.view;

import com.flarestar.drones.mvw.processing.parser.exceptions.InvalidStyleValue;
import com.flarestar.drones.mvw.view.styleprocessors.BoxModelStyles;
import com.flarestar.drones.mvw.view.styleprocessors.OverflowStyles;
import com.google.inject.Singleton;

/**
 * TODO
 */
@Singleton
public class StyleProcessor {
    private final Style[] STYLES = {
        new BoxModelStyles(),
        new OverflowStyles(),
    };

    public String process(ViewNode node) throws InvalidStyleValue {
        StringBuilder result = new StringBuilder();
        for (Style style : STYLES) {
            style.toCode(node, result);
        }
        return result.toString();
    }
}
