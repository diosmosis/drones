package com.flarestar.drones.mvw.directives;

import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.mvw.annotations.directive.DirectiveName;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.processing.parser.directive.matchers.AttributeMatcher;
import com.flarestar.drones.mvw.model.scope.Property;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DirectiveName("ng-controller")
@DirectiveMatcher(AttributeMatcher.class)
public class Controller extends Directive {
    public static class InvalidControllerAttribute extends LayoutFileException {
        public InvalidControllerAttribute(String message) {
            super(message);
        }
    }

    // TODO: shouldn't be inner since it's used by Directive.
    public static class AttributeProcessor {
        public static class ParseResult {
            private boolean isInjected;
            private String controllerClass;
            private String controllerScopeProperty;

            public ParseResult(boolean isInjected, String controllerClass, String controllerScopeProperty) {
                this.isInjected = isInjected;
                this.controllerClass = controllerClass;
                this.controllerScopeProperty = controllerScopeProperty;
            }

            public boolean isInjected() {
                return isInjected;
            }

            public String getControllerClass() {
                return controllerClass;
            }

            public String getControllerScopeProperty() {
                return controllerScopeProperty;
            }

            public void setIsInjected(boolean isInjected) {
                this.isInjected = isInjected;
            }
        }

        public static class ControllerProperty extends Property {
            public ControllerProperty(ParseResult parsed, String initialValue, boolean isInjected, Directive source) {
                super(parsed.controllerScopeProperty, parsed.controllerClass, BindType.PARENT_CHILD, initialValue, isInjected, source);
            }
        }

        private final GenerationContext context;
        private final Directive source;

        public AttributeProcessor(GenerationContext context, Directive source) {
            this.context = context;
            this.source = source;
        }

        public void process(ParseResult parsed, ViewNode node) {
            String initialValue = null;
            if (parsed.isInjected) {
                initialValue = "new " + parsed.controllerClass + "(owner.getContext())";
            }

            node.scopeDefinition.addProperty(new ControllerProperty(parsed, initialValue, parsed.isInjected, source));
        }

        private final static Pattern controllerAttributeRegex = Pattern.compile("(#)?([\\w.$]+)\\s+as\\s+(\\w+)");

        public ParseResult parse(String attributeValue) throws InvalidControllerAttribute {
            Matcher match = controllerAttributeRegex.matcher(attributeValue);
            if (!match.matches()) {
                throw new InvalidControllerAttribute("Invalid ng-controller attribute: " + attributeValue +
                    ". Expected something like 'ControllerClass as scopeProperty'.");
            }

            boolean isInjected = match.group(1) != null;
            String controllerClass = match.group(2);
            String controllerScopeProperty = match.group(3);

            return new ParseResult(isInjected, controllerClass, controllerScopeProperty);
        }
    }

    public Controller(GenerationContext context) throws LayoutFileException {
        super(context);
    }

    @Override
    public void manipulateViewNode(ViewNode node) throws LayoutFileException {
        super.manipulateViewNode(node);

        String attributeValue = node.element.attr("ng-controller");

        AttributeProcessor processor = new AttributeProcessor(context, this);
        AttributeProcessor.ParseResult result = processor.parse(attributeValue);
        processor.process(result, node);
    }
}
