package com.flarestar.drones.layout.view;

import com.flarestar.drones.layout.parser.exceptions.InvalidStyleValue;

/**
 * Created by runic on 10/10/15.
 */
public interface Style {
    void toCode(ViewNode node, StringBuilder result) throws InvalidStyleValue;
}
