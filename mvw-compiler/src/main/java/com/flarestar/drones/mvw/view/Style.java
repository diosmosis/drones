package com.flarestar.drones.mvw.view;

import com.flarestar.drones.mvw.parser.exceptions.InvalidStyleValue;

/**
 * Created by runic on 10/10/15.
 */
public interface Style {
    void toCode(ViewNode node, StringBuilder result) throws InvalidStyleValue;
}
