package com.flarestar.drones.mvw.directives;

import android.widget.ImageView;
import com.flarestar.drones.mvw.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.mvw.annotations.directive.DirectiveName;
import com.flarestar.drones.mvw.annotations.directive.DirectiveView;
import com.flarestar.drones.mvw.parser.exceptions.InvalidLayoutAttributeValue;
import com.flarestar.drones.mvw.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.parser.exceptions.MissingLayoutAttributeValue;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.ViewNode;
import com.flarestar.drones.mvw.view.attributeprocessors.Helper;
import com.flarestar.drones.mvw.view.directive.matchers.TagMatcher;

@DirectiveName("img")
@DirectiveView(view = ImageView.class)
@DirectiveMatcher(TagMatcher.class)
public class Img extends Directive {
    public Img(GenerationContext context) throws LayoutFileException {
        super(context);
    }

    @Override
    public String afterViewCreated(ViewNode node) throws LayoutFileException {
        String attributeValue = node.attributes.get("src");
        if (attributeValue == null) {
            throw new MissingLayoutAttributeValue("src");
        }

        String resourceCode = Helper.getResourceCode(attributeValue);
        if (resourceCode == null) {
            throw new InvalidLayoutAttributeValue("Invalid resource URI: " + attributeValue);
        }

        return "result.setImageResource(" + resourceCode + ");\n";
    }
}
