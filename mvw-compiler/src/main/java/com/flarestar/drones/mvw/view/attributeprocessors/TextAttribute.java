package com.flarestar.drones.mvw.view.attributeprocessors;

import com.flarestar.drones.mvw.view.ViewAttribute;
import com.flarestar.drones.mvw.view.ViewNode;

/**
 * TODO
 */
public class TextAttribute implements ViewAttribute {
    private final String[] REQUIRED_ATTRIBUTES = {"text"};

    @Override
    public void toCode(ViewNode node, StringBuilder result) {
        String text = node.attributes.get("text").replace("\"", "\\\"");
        result.append("realScreen." + node.id + ".setText(\"" + text + "\");\n");
    }

    @Override
    public String[] getRequiredAttributes() {
        return REQUIRED_ATTRIBUTES;
    }
}
