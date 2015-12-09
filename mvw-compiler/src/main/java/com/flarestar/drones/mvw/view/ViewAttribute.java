package com.flarestar.drones.mvw.view;

import com.flarestar.drones.mvw.parser.exceptions.InvalidLayoutAttributeValue;

/**
 * TODO
 */
public interface ViewAttribute {
    void toCode(ViewNode node, StringBuilder result) throws InvalidLayoutAttributeValue;
    String[] getRequiredAttributes();
}
