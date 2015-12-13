package com.flarestar.drones.mvw.view.scope;

import com.flarestar.drones.mvw.parser.exceptions.InvalidPropertyDescriptor;
import com.flarestar.drones.mvw.view.Directive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Property {
    public enum BindType {

        NONE,
        RAW_ATTR_VALUE,
        EXPRESSION_VALUE,
        EXPRESSION_EVAL,
        ;

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
                default:
                    return NONE;
            }
        }
    }

    // TODO: support required/optional attribute binding
    // TODO: handle collection watches when binding attributes?
    private static final Pattern PROPERTY_DESCRIPTOR_REGEX =
        Pattern.compile("([.\\w\\$]+)\\s+([\\w\\$]+)\\s*(?:=\\s*([=@&]?)(.+))?");

    public final String name;
    public final String type;
    public final BindType bindType;
    public final String initialValue;
    public final Directive source;

    public Property(String name, String type, BindType bindType, String initialValue, Directive source) {
        this.name = name;
        this.type = type;
        this.bindType = bindType == null ? BindType.NONE : bindType;
        this.initialValue = initialValue;
        this.source = source;
    }

    public String accessCode() {
        return "scope." + name;
    }

    public static Property makeFromDescriptor(String propertyDescriptor, Directive directive)
        throws InvalidPropertyDescriptor {
        Matcher m = PROPERTY_DESCRIPTOR_REGEX.matcher(propertyDescriptor);
        if (!m.matches()) {
            throw new InvalidPropertyDescriptor(propertyDescriptor, directive.getDirectiveName());
        }

        BindType bindType = BindType.fromStr(m.group(3));
        if (bindType != BindType.NONE && m.group(4) == null) {
            throw new InvalidPropertyDescriptor(propertyDescriptor, directive.getDirectiveName(),
                "Property binding specified without initial value");
        }

        return new Property(m.group(2), m.group(1), bindType, m.group(4), directive);
    }

    public boolean hasBinding() {
        return bindType != BindType.NONE;
    }

    public boolean hasBidirectionalBinding() {
        return bindType == BindType.EXPRESSION_VALUE;
    }
}
