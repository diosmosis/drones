package com.flarestar.drones.layout.view.styleprocessors;

import com.flarestar.drones.layout.parser.exceptions.InvalidStyleValue;
import com.flarestar.drones.layout.view.Style;
import com.flarestar.drones.layout.view.ViewNode;

public class OverflowStyles implements Style {
    private static final String OVERFLOW_X_STYLE = "overflow-x";
    private static final String OVERFLOW_Y_STYLE = "overflow-y";
    private static final String SCROLL = "scroll";
    private static final String HIDDEN = "hidden";

    @Override
    public void toCode(ViewNode node, StringBuilder result) throws InvalidStyleValue {
        String overflowX = getOverflowConstant(node, OVERFLOW_X_STYLE);
        String overflowY = getOverflowConstant(node, OVERFLOW_Y_STYLE);

        if (overflowX != null && overflowX.equals(SCROLL)) {
            result.append("result.setHorizontalScrollBarEnabled(true);\n");
        }

        if (overflowY != null && overflowY.equals(SCROLL)) {
            result.append("result.setVerticalScrollBarEnabled(true);\n");
        }
    }

    private String getOverflowConstant(ViewNode node, String styleVarName) throws InvalidStyleValue {
        String value = node.styles.get(styleVarName);
        if (value != null && !value.equals(HIDDEN) && !value.equals(SCROLL)) {
            throw new InvalidStyleValue(styleVarName + " style can be either '" + HIDDEN + "' or '" + SCROLL + "'.");
        }
        return value;
    }
}
