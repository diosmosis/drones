package com.flarestar.drones.layout.android;

import com.flarestar.drones.layout.android.exceptions.InvalidManifestException;
import com.flarestar.drones.layout.android.exceptions.ManifestCannotBeFound;
import com.flarestar.drones.layout.android.exceptions.ManifestCannotBeParsed;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

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
