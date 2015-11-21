package com.flarestar.drones.layout.directives;

import android.widget.TextView;
import com.flarestar.drones.layout.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.layout.annotations.directive.DirectiveName;
import com.flarestar.drones.layout.annotations.directive.DirectiveView;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.view.directive.matchers.TagMatcher;

@DirectiveName("label")
@DirectiveMatcher(TagMatcher.class)
@DirectiveView(view = TextView.class)
public class Label extends Directive {
    public Label(ViewNode node) {
        super(node);
    }
}
