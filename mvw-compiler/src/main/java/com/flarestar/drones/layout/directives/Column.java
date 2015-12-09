package com.flarestar.drones.layout.directives;

import com.flarestar.drones.layout.GenerationContext;
import com.flarestar.drones.layout.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.layout.annotations.directive.DirectiveName;
import com.flarestar.drones.layout.annotations.directive.DirectiveView;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.directive.matchers.TagMatcher;

@DirectiveName("column")
@DirectiveView(view = com.flarestar.drones.views.viewgroups.Column.class)
@DirectiveMatcher(TagMatcher.class)
public class Column extends Directive {
    public Column(GenerationContext context) throws LayoutFileException {
        super(context);
    }
}
