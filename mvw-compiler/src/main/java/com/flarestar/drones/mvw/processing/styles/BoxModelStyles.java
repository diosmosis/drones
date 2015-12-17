package com.flarestar.drones.mvw.processing.styles;

import com.flarestar.drones.mvw.processing.parser.exceptions.InvalidStyleValue;
import com.flarestar.drones.mvw.processing.Style;
import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.model.ViewProperty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO
 */
public class BoxModelStyles implements Style {

    public static class Size {
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

    public class Property extends ViewProperty {
        private Size marginTop;
        private Size marginBottom;
        private Size marginLeft;
        private Size marginRight;
        private Size paddingTop;
        private Size paddingBottom;
        private Size paddingLeft;
        private Size paddingRight;
        private Size width;
        private Size height;

        public Property(String marginTop, String marginBottom, String marginLeft, String marginRight, String paddingTop,
                        String paddingBottom, String paddingLeft, String paddingRight, String width, String height)
                throws InvalidStyleValue {
            super("setLayoutParams");

            this.marginTop = marginTop == null ? null : new Size(marginTop);
            this.marginBottom = marginBottom == null ? null : new Size(marginBottom);
            this.marginLeft = marginLeft == null ? null : new Size(marginLeft);
            this.marginRight = marginRight == null ? null : new Size(marginRight);
            this.paddingTop = paddingTop == null ? null : new Size(paddingTop);
            this.paddingBottom = paddingBottom == null ? null : new Size(paddingBottom);
            this.paddingLeft = paddingLeft == null ? null : new Size(paddingLeft);
            this.paddingRight = paddingRight == null ? null : new Size(paddingRight);
            this.width = width == null ? null : new Size(width);
            this.height = height == null ? null : new Size(height);
        }

        @Override
        public String getTemplate() {
            return "templates/boxModelStylesProperty.twig";
        }

        public BoxModelStyles getBoxModelStyles() {
            return BoxModelStyles.this;
        }

        public Size getMarginTop() {
            return marginTop;
        }

        public Size getMarginBottom() {
            return marginBottom;
        }

        public Size getMarginLeft() {
            return marginLeft;
        }

        public Size getMarginRight() {
            return marginRight;
        }

        public Size getPaddingTop() {
            return paddingTop;
        }

        public Size getPaddingBottom() {
            return paddingBottom;
        }

        public Size getPaddingLeft() {
            return paddingLeft;
        }

        public Size getPaddingRight() {
            return paddingRight;
        }

        public Size getBoxWidth() {
            return width;
        }

        public Size getBoxHeight() {
            return height;
        }
    }

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

    @Override
    public void process(ViewNode node) throws InvalidStyleValue {
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

        node.viewProperties.add(new Property(marginTop, marginBottom, marginLeft, marginRight, paddingTop, paddingBottom,
            paddingLeft, paddingRight, width, height));
    }
}
