package com.flarestar.drones.mvw.directives;

import com.flarestar.drones.mvw.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.mvw.annotations.directive.DirectiveName;
import com.flarestar.drones.mvw.annotations.directive.DirectiveView;
import com.flarestar.drones.mvw.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.directive.matchers.TagMatcher;

@DirectiveName("button")
@DirectiveView(view = android.widget.Button.class)
@DirectiveMatcher(TagMatcher.class)
public class Button extends Directive {
    public Button(GenerationContext context) throws LayoutFileException {
        super(context);
    }
}
