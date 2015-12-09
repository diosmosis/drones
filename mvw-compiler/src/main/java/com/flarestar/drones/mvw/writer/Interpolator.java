package com.flarestar.drones.mvw.writer;

import com.google.common.base.Joiner;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// TODO: services like these should mirror angular services (ie, should have a $interpolate service)
public class Interpolator {

    public String interpolate(String text) {
        List<String> parts = new ArrayList<>();

        int index = 0;
        while (index < text.length()) {
            int startIndex = text.indexOf("{{");
            int endIndex = text.indexOf("}}", startIndex);

            if (startIndex != -1 && endIndex != -1) {
                String normalPart = text.substring(index, startIndex);
                if (!normalPart.isEmpty()) {
                    parts.add(getStringLiteral(normalPart));
                }

                String codePart = text.substring(startIndex + 2, endIndex);
                parts.add(codePart);

                index = endIndex + 2;
            } else {
                String part = text.substring(index);
                if (!part.isEmpty()) {
                    parts.add(getStringLiteral(part));
                }

                break;
            }
        }

        return Joiner.on(" + ").join(parts);
    }

    private String getStringLiteral(String substring) {
        return JSONObject.quote(substring);
    }
}
