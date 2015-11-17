package com.flarestar.drones.layout.directives;

import com.flarestar.drones.layout.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.layout.annotations.directive.DirectiveView;
import com.flarestar.drones.layout.annotations.directive.IsolateScope;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.directive.matchers.TagMatcher;

@DirectiveView(view = com.flarestar.drones.views.Column.class)
@DirectiveMatcher(TagMatcher.class)
public class Column extends Directive {
    @Override
    public String getDirectiveName() {
        return "column";
    }
}
