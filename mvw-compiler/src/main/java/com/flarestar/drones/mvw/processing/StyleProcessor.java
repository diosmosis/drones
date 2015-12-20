package com.flarestar.drones.mvw.processing;

import com.flarestar.drones.mvw.processing.parser.exceptions.InvalidStyleValue;
import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.processing.styles.BoxModelStyles;
import com.flarestar.drones.mvw.processing.styles.OverflowStyles;
import com.google.inject.Inject;
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

    @Inject
    public StyleProcessor() {
        // empty
    }

    public void process(ViewNode node) throws InvalidStyleValue {
        for (Style style : STYLES) {
            style.process(node);
        }
    }
}
