package com.flarestar.drones.base;

import com.flarestar.drones.base.generation.Generator;
import flarestar.bdd.annotations.Describe;
import flarestar.bdd.annotations.It;
import flarestar.bdd.runner.Runner;
import flarestar.mirror.mock.MockProcessingEnvironment;
import flarestar.mirror.mock.element.ClassElement;
import flarestar.mirror.mock.element.ElementFactory;
import org.junit.runner.RunWith;

import javax.lang.model.element.Element;

import static flarestar.bdd.Assert.expect;
import static org.mockito.Mockito.mock;

@RunWith(Runner.class)
@Describe(DaggerFilesGenerator.class)
public class DaggerFilesGeneratorTest {

    public static class TestClassIsScreen extends BaseScreen {
        public void someMethod() {
            // empty
        }
    }

    public static class TestClassIsNotScreen {
        // empty
    }

    interface TestInterface {
        // empty
    }

    private MockProcessingEnvironment processingEnvironment;
    private DaggerFilesGenerator instance;

    public void beforeEach() {
        this.processingEnvironment = new MockProcessingEnvironment("");
        this.instance = new DaggerFilesGenerator(processingEnvironment, mock(Generator.class), mock(ScreenDroneSniffer.class));
    }

    @Describe(desc = "#canGenerateFor()")
    public class canGenerateForTest {
        @It("should return false if element is not a TypeElement")
        public void testSuccess1() throws Throwable {
            ClassElement classElement = ElementFactory.make(TestClassIsScreen.class);
            Element methodElement = ElementFactory.make(TestClassIsScreen.class.getMethod("someMethod"), classElement);

            boolean canGenerate = instance.canGenerateFor(methodElement);
            expect(canGenerate).to().be().false_();
        }

        @It("should return false if element is not for a class")
        public void testSuccess2() {
            Element element = ElementFactory.make(TestInterface.class);

            boolean canGenerate = instance.canGenerateFor(element);
            expect(canGenerate).to().be().false_();
        }

        @It("should return false if element does not derive from BaseScreen")
        public void testSuccess3() {
            Element element = ElementFactory.make(TestClassIsNotScreen.class);

            boolean canGenerate = instance.canGenerateFor(element);
            expect(canGenerate).to().be().false_();
        }

        @It("should return true if element is for a class that derives from BaseScreen")
        public void testSuccess4() {
            Element element = ElementFactory.make(TestClassIsScreen.class);

            boolean canGenerate = instance.canGenerateFor(element);
            expect(canGenerate).to().be().true_();
        }
    }
}
