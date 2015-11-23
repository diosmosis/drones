package com.flarestar.drones.layout.compilerutilities;

import com.flarestar.drones.layout.android.Manifest;
import com.flarestar.drones.layout.android.exceptions.ManifestCannotBeFound;
import com.flarestar.drones.layout.android.exceptions.ManifestCannotBeParsed;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@Singleton
public class ProjectSniffer {

    private Path projectRoot;
    private Path manifestPath;

    private ProcessingEnvironment processingEnvironment;

    @Inject
    public ProjectSniffer(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    public Path getProjectRoot() {
        if (projectRoot == null) {
            FileObject dummySource;
            try {
                dummySource = processingEnvironment.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", "dummy.out");
            } catch (IOException e) {
                throw new RuntimeException("Unexpected error, cannot create dummy source output resource.");
            }

            Path parent;
            Path path = Paths.get(dummySource.toUri());
            while ((parent = path.getParent()) != null) {
                path = parent;

                Path srcPath = Paths.get(path.toString(), "src");
                if (srcPath.toFile().isDirectory()) {
                    projectRoot = srcPath;
                    return projectRoot;
                }
            }

            throw new RuntimeException("Unexpected error, cannot find project root dir (by looking for a src dir) " +
                "using dummy source path '" + dummySource.toUri().getPath() + "'.");
        }

        return projectRoot;
    }

    public Manifest findManifestFile() throws ManifestCannotBeFound, ManifestCannotBeParsed {
        if (manifestPath ==  null) {
            FileVisitor<Path> visitor = new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    if (path.endsWith("AndroidManifest.xml")) {
                        manifestPath = path;
                        return FileVisitResult.TERMINATE;
                    } else {
                        return FileVisitResult.CONTINUE;
                    }
                }

                @Override
                public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            };

            try {
                Files.walkFileTree(getProjectRoot(), visitor);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (manifestPath == null) {
                throw new RuntimeException("Unexpected error, cannot find AndroidManifest.xml file. Using project root = "
                    + projectRoot.toString());
            }
        }

        try {
            String xmlContents = new String(Files.readAllBytes(manifestPath), Charset.forName("UTF-8"));
            return new Manifest(xmlContents);
        } catch (IOException e) {
            throw new ManifestCannotBeParsed("Cannot open and read '" + manifestPath + "'.");
        }
    }
}
