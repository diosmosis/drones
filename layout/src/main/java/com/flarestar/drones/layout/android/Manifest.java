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

    public static Manifest findManifestFile(String startPath) throws ManifestCannotBeFound, ManifestCannotBeParsed {
        return findManifestFile(new File(startPath));
    }

    public static Manifest findManifestFile(File path) throws ManifestCannotBeFound, ManifestCannotBeParsed {
        if (path.isFile()) {
            path = path.getParentFile();
        }

        File[] files = path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.equals("AndroidManifest.xml");
            }
        });

        if (files.length  == 0) {
            File parentFile = path.getParentFile();
            if (parentFile == null) {
                throw new ManifestCannotBeFound("Cannot find the android manifest XML.");
            }

            return findManifestFile(parentFile);
        } else {
            String manifestPath = files[0].getPath();
            try {
                String xmlContents = new String(Files.readAllBytes(Paths.get(manifestPath)), Charset.forName("UTF-8"));
                return new Manifest(xmlContents);
            } catch (IOException e) {
                throw new ManifestCannotBeParsed("Cannot open and read '" + manifestPath + "'.");
            }
        }
    }

    public String getApplicationPackage() throws InvalidManifestException {
        String applicationPackage = document.select("manifest").attr("package");
        if (applicationPackage == null || applicationPackage.isEmpty()) {
            throw new InvalidManifestException("Android manifest is missing package attribute.");
        }
        return applicationPackage;
    }
}
