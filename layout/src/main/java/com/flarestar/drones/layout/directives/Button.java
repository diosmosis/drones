package com.flarestar.drones.layout.directives;

import com.flarestar.drones.layout.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.layout.annotations.directive.DirectiveView;
import com.flarestar.drones.layout.annotations.directive.IsolateScope;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.directive.matchers.TagMatcher;

@DirectiveView(view = android.widget.Button.class)
@DirectiveMatcher(TagMatcher.class)
public class Button extends Directive {
    @Override
    public String getDirectiveName() {
        return "button";
    }
}
