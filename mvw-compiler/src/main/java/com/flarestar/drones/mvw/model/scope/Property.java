package com.flarestar.drones.mvw.model.scope;

import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.processing.parser.exceptions.InvalidPropertyDescriptor;
import com.flarestar.drones.mvw.model.Directive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Property {
    public enum BindType {

        NONE,
        RAW_ATTR_VALUE,
        EXPRESSION_VALUE,
        EXPRESSION_EVAL,
        PARENT_CHILD,
        LOCAL_VAR;

        public static BindType fromStr(String bindType) {
            if (bindType == null) {
                return NONE;
            }

            switch (bindType) {
                case "=":
                    return EXPRESSION_VALUE;
                case "@":
                    return RAW_ATTR_VALUE;
                case "&":
                    return EXPRESSION_EVAL;
                case "/":
                    return LOCAL_VAR;
                default:
                    return NONE;
            }
        }
    }

    // TODO: support required/optional attribute binding
    // TODO: handle collection watches when binding attributes?
    private static final Pattern PROPERTY_DESCRIPTOR_REGEX =
        Pattern.compile("(#)?([.\\w\\$]+)\\s+([\\w\\$]+)\\s*(?:=\\s*([=@&/]?)(.+))?");

    public final String name;
    public final String type;
    public final BindType bindType;
    public final String initialValue;
    public final Directive source;
    public final boolean isInjected;

    public Property(String name, String type, BindType bindType, String initialValue, boolean isInjected,
                    Directive source) {
        this.name = name;
        this.type = type;
        this.bindType = bindType == null ? BindType.NONE : bindType;
        this.initialValue = initialValue;
        this.source = source;
        this.isInjected = isInjected;
    }

    public boolean hasBinding() {
        return bindType != BindType.NONE;
    }

    public boolean canInitializeInScopeConstructor(boolean isDirectiveRoot) {
        if (isInjected) {
            return false;
        }

        return bindType == BindType.NONE || (bindType == BindType.PARENT_CHILD && !isDirectiveRoot);
    }

    public boolean hasBidirectionalBinding() {
        return bindType == BindType.EXPRESSION_VALUE;
    }

    public boolean initializeToLocalValue() {
        return bindType == BindType.LOCAL_VAR;
    }

    public static Property makeFromDescriptor(String propertyDescriptor, Directive directive)
        throws InvalidPropertyDescriptor {
        Matcher m = PROPERTY_DESCRIPTOR_REGEX.matcher(propertyDescriptor);
        if (!m.matches()) {
            throw new InvalidPropertyDescriptor(propertyDescriptor, directive.getDirectiveName());
        }

        BindType bindType = BindType.fromStr(m.group(4));
        if (bindType != BindType.NONE && m.group(5) == null) {
            throw new InvalidPropertyDescriptor(propertyDescriptor, directive.getDirectiveName(),
                "Property binding specified without initial value");
        }

        boolean isInjected = m.group(1) != null;

        return new Property(m.group(3), m.group(2), bindType, m.group(5), isInjected, directive);
    }
}
