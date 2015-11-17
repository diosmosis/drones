package com.flarestar.drones.layout.directives;

import android.widget.ImageView;
import com.flarestar.drones.layout.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.layout.annotations.directive.DirectiveView;
import com.flarestar.drones.layout.annotations.directive.IsolateScope;
import com.flarestar.drones.layout.parser.exceptions.InvalidLayoutAttributeValue;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.parser.exceptions.MissingLayoutAttributeValue;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.view.attributeprocessors.Helper;
import com.flarestar.drones.layout.view.directive.matchers.TagMatcher;

@DirectiveView(view = ImageView.class)
@DirectiveMatcher(TagMatcher.class)
public class Img extends Directive {
    @Override
    public String getDirectiveName() {
        return "img";
    }

    @Override
    public void onViewCreated(ViewNode node, StringBuilder result) throws LayoutFileException {
        String attributeValue = node.attributes.get("src");
        if (attributeValue == null) {
            throw new MissingLayoutAttributeValue("src");
        }

        String resourceCode = Helper.getResourceCode(attributeValue);
        if (resourceCode == null) {
            throw new InvalidLayoutAttributeValue("Invalid resource URI: " + attributeValue);
        }

        result.append("realScreen." + node.id + ".setImageResource(" + resourceCode + ");\n");
    }
}
