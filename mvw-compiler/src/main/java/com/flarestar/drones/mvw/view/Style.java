package com.flarestar.drones.mvw.view;

import com.flarestar.drones.mvw.processing.parser.exceptions.InvalidStyleValue;

/**
 * TODO
 */
public interface Style {
    void process(ViewNode node) throws InvalidStyleValue;
}
