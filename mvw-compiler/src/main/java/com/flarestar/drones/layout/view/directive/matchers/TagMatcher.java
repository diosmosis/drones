package com.flarestar.drones.layout.view.directive.matchers;

    import com.flarestar.drones.layout.view.Directive;
    import com.flarestar.drones.layout.view.ViewNode;
    import com.flarestar.drones.layout.view.directive.DirectiveMatcher;
    import org.jsoup.nodes.Element;

/**
 * TODO
 */
public class TagMatcher implements DirectiveMatcher {

    @Override
    public boolean matches(Element node, Class<?> directiveClass) {
        String directiveName = Directive.getDirectiveName(directiveClass);
        return node.tagName().equals(directiveName);
    }
}
