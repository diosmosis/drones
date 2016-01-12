package com.flarestar.drones.base;

import com.flarestar.drones.base.annotations.DroneMarker;
import flarestar.bdd.annotations.Describe;
import flarestar.bdd.annotations.It;
import flarestar.bdd.runner.Runner;
import flarestar.mirror.mock.element.ElementFactory;
import org.junit.runner.RunWith;

import javax.lang.model.element.TypeElement;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static flarestar.bdd.Assert.expect;

@RunWith(Runner.class)
@Describe(ScreenDroneSniffer.class)
public class ScreenDroneSnifferTest {

    @DroneMarker(generatedClass = "com.TestDroneMarker1", extraComponentMethods = {"c1", "c2"})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestDroneMarker1 {
        // empty
    }

    @DroneMarker(generatedClass = "com.TestDroneMarker2", extraComponentMethods = {"c3", "c2"})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestDroneMarker2 {
        // empty
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface NonDroneMarkerInterface {
        // empty
    }

    @Retention(RetentionPolicy.RUNTIME)
    @DroneMarker(generatedClass = "{fullName}TestDroneMarker3", extraComponentMethods = {"{fullName}c4"})
    public @interface TestDroneMarker3 {
        // empty
    }

    public static class TestClassWithNoAnnotations {}

    @NonDroneMarkerInterface
    @TestDroneMarker1
    @TestDroneMarker2
    public static class TestClassWithAnnotations {}

    @TestDroneMarker3
    public static class TestClassWithFullNameAnnotation {}

    private ScreenDroneSniffer instance;

    public void beforeEach() {
        this.instance = new ScreenDroneSniffer();
    }

    @Describe(desc = "#getDroneInformationList()")
    public class getDroneInformationListTest {
        @It("should return empty array called w/ type w/ no annotations")
        public void test_getDroneInformationList_success1() {
            TypeElement element = ElementFactory.make(TestClassWithNoAnnotations.class);

            List<ScreenDroneSniffer.DroneInformation> result = instance.getDroneInformationList(element);

            expect(result).to().be().empty();
        }

        @It("should return drone information for annotations on class that are marked w/ @DroneMarker")
        public void test_getDroneInformationList_success2() {
            TypeElement element = ElementFactory.make(TestClassWithAnnotations.class);

            List<ScreenDroneSniffer.DroneInformation> result = instance.getDroneInformationList(element);
            sort(result);

            expect(result).to().have().length(2);
            expect(result.get(0).getClassName()).to().equal("com.TestDroneMarker1");
            expect(result.get(0).getSimpleClassName()).to().equal("TestDroneMarker1");

            expect(result.get(1).getClassName()).to().equal("com.TestDroneMarker2");
            expect(result.get(1).getSimpleClassName()).to().equal("TestDroneMarker2");
        }

        @It("should replace instances of {fullName} in @DroneMarker generatedClass property with activity class name")
        public void test_getDroneInformationList_success3() {
            TypeElement element = ElementFactory.make(TestClassWithFullNameAnnotation.class);

            List<ScreenDroneSniffer.DroneInformation> result = instance.getDroneInformationList(element);

            expect(result).to().have().length(1);
            expect(result.get(0).getClassName()).to().equal("com.flarestar.drones.base.ScreenDroneSnifferTest$TestClassWithFullNameAnnotationTestDroneMarker3");
            expect(result.get(0).getSimpleClassName()).to().equal("TestClassWithFullNameAnnotationTestDroneMarker3");
        }
    }

    @Describe(desc = "#getExtraComponentModules()")
    public class getExtraComponentModulesTest {
        @It("should return an empty list if called w/ type w/ no annotations")
        public void test_getExtraComponentModules_success1() {
            TypeElement element = ElementFactory.make(TestClassWithNoAnnotations.class);

            Set<String> result = instance.getExtraComponentModules(element);

            expect(result).to().be().empty();
        }

        @It("should return the set of extra components specified by @DroneMarker annotations")
        public void test_getExtraComponentModules_success2() {
            TypeElement element = ElementFactory.make(TestClassWithAnnotations.class);

            Set<String> result = instance.getExtraComponentModules(element);

            expect(result).to().have().values(new String[] { "c1", "c2", "c3" });
        }

        @It("should replace instances of {fullName} in @DroneMarker generatedClass property with activity class name")
        public void test_getExtraComponentModules_success3() {
            TypeElement element = ElementFactory.make(TestClassWithFullNameAnnotation.class);

            Set<String> result = instance.getExtraComponentModules(element);

            expect(result).to().have().values(new String[] {"com.flarestar.drones.base.ScreenDroneSnifferTest$TestClassWithFullNameAnnotationc4"});
        }
    }

    private void sort(List<ScreenDroneSniffer.DroneInformation> result) {
        Collections.sort(result, new Comparator<ScreenDroneSniffer.DroneInformation>() {
            @Override
            public int compare(ScreenDroneSniffer.DroneInformation lhs, ScreenDroneSniffer.DroneInformation rhs) {
                return lhs.getClassName().compareTo(rhs.getClassName());
            }
        });
    }
}
