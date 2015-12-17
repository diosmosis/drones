package com.flarestar.drones.mvw.view.styleprocessors;

import com.flarestar.drones.mvw.processing.parser.exceptions.InvalidStyleValue;
import com.flarestar.drones.mvw.view.Style;
import com.flarestar.drones.mvw.view.ViewNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO
 */
public class BoxModelStyles implements Style {
    public final String MARGIN_TOP = "margin-top";
    public final String MARGIN_BOTTOM = "margin-bottom";
    public final String MARGIN_LEFT = "margin-left";
    public final String MARGIN_RIGHT = "margin-right";
    public final String PADDING_TOP = "padding-top";
    public final String PADDING_BOTTOM = "padding-bottom";
    public final String PADDING_LEFT = "padding-left";
    public final String PADDING_RIGHT = "padding-right";
    public final String WIDTH = "width";
    public final String HEIGHT = "height";

    private static class Size {
        private final Pattern SIZE_PATTERN = Pattern.compile("([0-9]+)%\\s*(width|height)?");

        public String amount;
        public String context;

        public Size(String descriptor) throws InvalidStyleValue {
            Matcher matcher = SIZE_PATTERN.matcher(descriptor.trim());
            if (!matcher.matches()) {
                throw new InvalidStyleValue("Invalid size descriptor: " + descriptor);
            }

            this.amount = matcher.group(1);

            String strContext = matcher.group(2);
            if (strContext == null) {
                strContext = "";
            }

            switch (strContext) {
                case "width":
                    this.context = "BoxModelNode.Size.CONTEXT_MEASURED_WIDTH";
                    break;
                case "height":
                    this.context = "BoxModelNode.Size.CONTEXT_MEASURED_HEIGHT";
                    break;
                default:
                    this.context = "BoxModelNode.Size.CONTEXT_AVAILABLE_SPACE";
                    break;
            }
        }
    }

    @Override
    public void toCode(ViewNode node, StringBuilder result) throws InvalidStyleValue {
        String marginTop = node.styles.get(MARGIN_TOP);
        String marginBottom = node.styles.get(MARGIN_BOTTOM);
        String marginLeft = node.styles.get(MARGIN_LEFT);
        String marginRight = node.styles.get(MARGIN_RIGHT);

        String paddingTop = node.styles.get(PADDING_TOP);
        String paddingBottom = node.styles.get(PADDING_BOTTOM);
        String paddingLeft = node.styles.get(PADDING_LEFT);
        String paddingRight = node.styles.get(PADDING_RIGHT);

        String width = node.styles.get(WIDTH);
        String height = node.styles.get(HEIGHT);

        if (marginTop == null
            && marginBottom == null
            && marginLeft == null
            && marginRight == null
            && paddingTop == null
            && paddingBottom == null
            && paddingLeft == null
            && paddingRight == null
            && width == null
            && height == null
        ) {
            return;
        }

        {
            result.append("BoxModelNode.LayoutParams layoutParams = new BoxModelNode.LayoutParams();\n");
        }

        addBoxModelProperty(node, result, "boxWidth", width);
        addBoxModelProperty(node, result, "boxHeight", height);

        addBoxModelProperty(node, result, "marginTop", marginTop);
        addBoxModelProperty(node, result, "marginBottom", marginBottom);
        addBoxModelProperty(node, result, "marginLeft", marginLeft);
        addBoxModelProperty(node, result, "marginRight", marginRight);

        addBoxModelProperty(node, result, "paddingTop", paddingTop);
        addBoxModelProperty(node, result, "paddingBottom", paddingBottom);
        addBoxModelProperty(node, result, "paddingLeft", paddingLeft);
        addBoxModelProperty(node, result, "paddingRight", paddingRight);

        result.append("result.setLayoutParams(layoutParams);\n");
    }

    private void addBoxModelProperty(ViewNode node, StringBuilder result, String name, String value) throws InvalidStyleValue {
        if (value == null) {
            return;
        }

        Size size = new Size(value);

        result.append("layoutParams.");
        result.append(name);
        result.append(" = new BoxModelNode.Size(");
        result.append(size.amount);
        result.append(", ");
        result.append(size.context);
        result.append(");\n");
    }
}
