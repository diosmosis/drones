package com.flarestar.drones.mvw.view;

import com.flarestar.drones.mvw.processing.parser.exceptions.InvalidStyleValue;
import com.flarestar.drones.mvw.view.styles.BoxModelStyles;
import com.flarestar.drones.mvw.view.styles.OverflowStyles;
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

    public void process(ViewNode node) throws InvalidStyleValue {
        for (Style style : STYLES) {
            style.process(node);
        }
    }
}
