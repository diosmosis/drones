package com.flarestar.drones.base;

import com.flarestar.drones.base.annotations.DroneMarker;
import com.google.inject.Singleton;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class ScreenDroneSniffer {

    public static class DroneInformation {

        private String className;
        private String simpleClassName;

        public DroneInformation(String generatedClassTemplate, TypeElement activityClassElement) {
            String fullName = activityClassElement.getQualifiedName().toString();
            className = generatedClassTemplate.replace("{fullName}", fullName);

            // TODO: this is repeated in many places. should deal w/ that.
            int lastDot = Math.max(className.lastIndexOf('.'), className.lastIndexOf('$'));
            simpleClassName = className.substring(lastDot + 1);
        }

        public String getClassName() {
            return className;
        }

        public String getSimpleClassName() {
            return simpleClassName;
        }
    }

    public List<DroneInformation> getDroneInformationList(TypeElement activityClassElement) {
        List<DroneInformation> result = new ArrayList<>();

        for (AnnotationMirror mirror : activityClassElement.getAnnotationMirrors()) {
            TypeElement annotationType = (TypeElement)mirror.getAnnotationType().asElement();
            DroneMarker marker = annotationType.getAnnotation(DroneMarker.class);
            if (marker == null) {
                continue;
            }

            result.add(new DroneInformation(marker.generatedClass(), activityClassElement));
        }

        return result;
    }

    public Set<String> getExtraComponentModules(TypeElement activityClassElement) {
        final String fullName = activityClassElement.getQualifiedName().toString();

        Set<String> result = new HashSet<>();

        for (AnnotationMirror mirror : activityClassElement.getAnnotationMirrors()) {
            TypeElement annotationType = (TypeElement)mirror.getAnnotationType().asElement();
            DroneMarker marker = annotationType.getAnnotation(DroneMarker.class);
            if (marker == null) {
                continue;
            }

            for (String extraMethod : marker.extraComponentMethods()) {
                extraMethod = extraMethod.replace("{fullName}", fullName);
                result.add(extraMethod);
            }
        }

        return result;
    }
}
