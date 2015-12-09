package com.flarestar.drones.mvw.android;

import com.flarestar.drones.mvw.android.exceptions.InvalidManifestException;
import com.flarestar.drones.mvw.android.exceptions.ManifestCannotBeParsed;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

public class Manifest {

    private Document document;

    public Manifest(String xmlContents) throws ManifestCannotBeParsed {
        document = Jsoup.parse(xmlContents, "UTF-8", Parser.xmlParser());
    }

    public String getApplicationPackage() throws InvalidManifestException {
        String applicationPackage = document.select("manifest").attr("package");
        if (applicationPackage == null || applicationPackage.isEmpty()) {
            throw new InvalidManifestException("Android manifest is missing package attribute.");
        }
        return applicationPackage;
    }
}
