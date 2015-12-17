package com.flarestar.drones.mvw.processing;

import com.flarestar.drones.mvw.processing.parser.exceptions.InvalidStyleValue;
import com.flarestar.drones.mvw.model.ViewNode;

/**
 * TODO
 */
public interface Style {
    void process(ViewNode node) throws InvalidStyleValue;
}
