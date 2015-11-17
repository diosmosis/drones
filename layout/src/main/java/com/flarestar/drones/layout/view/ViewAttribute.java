package com.flarestar.drones.layout.view;

import com.flarestar.drones.layout.parser.exceptions.InvalidLayoutAttributeValue;

/**
 * TODO
 */
public interface ViewAttribute {
    void toCode(ViewNode node, StringBuilder result) throws InvalidLayoutAttributeValue;
    String[] getRequiredAttributes();
}
