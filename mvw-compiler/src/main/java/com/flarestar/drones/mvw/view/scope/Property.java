package com.flarestar.drones.mvw.view.scope;

import com.flarestar.drones.mvw.parser.exceptions.InvalidPropertyDescriptor;
import com.flarestar.drones.mvw.view.Directive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Property {
    private static final Pattern PROPERTY_DESCRIPTOR_REGEX = Pattern.compile("(\\w+)\\s+(\\w+)\\s*(?:=\\s*(.+))?");

    public final String name;
    public final String type;
    public final String initialValueExpression;
    public final Directive source;

    public Property(String name, String type, String initialValueExpression, Directive source) {
        this.name = name;
        this.type = type;
        this.initialValueExpression = initialValueExpression;
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

        return new Property(m.group(2), m.group(1), m.group(3), directive);
    }
}
