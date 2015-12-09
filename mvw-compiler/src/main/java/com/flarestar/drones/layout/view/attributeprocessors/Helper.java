package com.flarestar.drones.layout.view.attributeprocessors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO
 */
public class Helper {
    private static Pattern resourceUriPattern = Pattern.compile("resource://([a-zA-Z0-9_]+)/([a-zA-Z0-9_]+)");

    public static String getResourceCode(String resourceUrl) {
        Matcher m = resourceUriPattern.matcher(resourceUrl);
        if (!m.matches()) {
            return null;
        }

        String type = m.group(1), name = m.group(2);
        return "R." + type + "." + name;
    }
}
