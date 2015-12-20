package com.flarestar.drones.mvw.directives;

import android.widget.ImageView;
import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.mvw.annotations.directive.DirectiveName;
import com.flarestar.drones.mvw.annotations.directive.DirectiveView;
import com.flarestar.drones.mvw.processing.parser.exceptions.InvalidLayoutAttributeValue;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.processing.parser.exceptions.MissingLayoutAttributeValue;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.model.ViewProperty;
import com.flarestar.drones.mvw.processing.parser.directive.matchers.TagMatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DirectiveName("img")
@DirectiveView(view = ImageView.class)
@DirectiveMatcher(TagMatcher.class)
public class Img extends Directive {
    private static Pattern resourceUriPattern = Pattern.compile("resource://([a-zA-Z0-9_]+)/([a-zA-Z0-9_]+)");

    public Img(GenerationContext context) throws LayoutFileException {
        super(context);
    }

    @Override
    public void manipulateViewNode(ViewNode node) throws LayoutFileException {
        String attributeValue = node.element.attr("src");
        if (attributeValue == null) {
            throw new MissingLayoutAttributeValue("src");
        }

        String resourceCode = getResourceCode(attributeValue);
        if (resourceCode == null) {
            throw new InvalidLayoutAttributeValue("Invalid resource URI: " + attributeValue);
        }

        node.viewProperties.add(new ViewProperty("setImageResource", resourceCode));
    }

    public static String getResourceCode(String resourceUrl) {
        Matcher m = resourceUriPattern.matcher(resourceUrl);
        if (!m.matches()) {
            return null;
        }

        String type = m.group(1), name = m.group(2);
        return "R." + type + "." + name;
    }
}
