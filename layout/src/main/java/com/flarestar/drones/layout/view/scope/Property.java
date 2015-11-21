package com.flarestar.drones.layout.view.scope;

import com.flarestar.drones.layout.parser.exceptions.InvalidPropertyDescriptor;
import com.flarestar.drones.layout.view.Directive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by runic on 11/21/15.
 */
public class Property {
    private static final Pattern PROPERTY_DESCRIPTOR_REGEX = Pattern.compile("(\\w+)\\s+(\\w+)\\s*(?:=\\s*(.+))?");

    public final String name;
    public final String type;
    public final String initialValueExpression;
    public final Directive source;
    public final boolean isInherited;

    public Property(String name, String type, String initialValueExpression, Directive source, boolean isInherited) {
        this.name = name;
        this.type = type;
        this.initialValueExpression = initialValueExpression;
        this.source = source;
        this.isInherited = isInherited;
    }

    public Property(String name, String type, String initialValueExpression, Directive source) {
        this(name, type, initialValueExpression, source, false);
    }

    public static Property makeFromDescriptor(String propertyDescriptor, Directive directive)
        throws InvalidPropertyDescriptor {
        Matcher m = PROPERTY_DESCRIPTOR_REGEX.matcher(propertyDescriptor);
        if (!m.matches()) {
            throw new InvalidPropertyDescriptor(propertyDescriptor, directive.getDirectiveName());
        }

        return new Property(m.group(2), m.group(1), m.group(3), directive);
    }

    public Property makeInherited() {
        return new Property(name, type, null, source, true);
    }
}
