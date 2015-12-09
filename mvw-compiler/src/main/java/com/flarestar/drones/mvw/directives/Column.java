package com.flarestar.drones.mvw.directives;

import com.flarestar.drones.mvw.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.mvw.annotations.directive.DirectiveName;
import com.flarestar.drones.mvw.annotations.directive.DirectiveView;
import com.flarestar.drones.mvw.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.directive.matchers.TagMatcher;

@DirectiveName("column")
@DirectiveView(view = com.flarestar.drones.views.viewgroups.Column.class)
@DirectiveMatcher(TagMatcher.class)
public class Column extends Directive {
    public Column(GenerationContext context) throws LayoutFileException {
        super(context);
    }
}
