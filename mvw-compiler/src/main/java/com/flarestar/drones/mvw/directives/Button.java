package com.flarestar.drones.mvw.directives;

import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.mvw.annotations.directive.DirectiveName;
import com.flarestar.drones.mvw.annotations.directive.DirectiveView;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.processing.parser.directive.matchers.TagMatcher;

@DirectiveName("button")
@DirectiveView(view = android.widget.Button.class)
@DirectiveMatcher(TagMatcher.class)
public class Button extends Directive {
    public Button(GenerationContext context) throws LayoutFileException {
        super(context);
    }
}
