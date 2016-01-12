package com.flarestar.drones.base.generation;

import com.flarestar.drones.base.generation.jtwig.RenderAddon;
import com.flarestar.drones.base.generation.jtwig.SymbolsHack;
import com.google.inject.Provider;
import flarestar.bdd.annotations.Describe;
import flarestar.bdd.annotations.It;
import flarestar.bdd.runner.Runner;
import flarestar.mirror.mock.MockProcessingEnvironment;
import flarestar.mirror.mock.utils.MemoryFileObject;
import org.jtwig.configuration.JtwigConfiguration;
import org.jtwig.configuration.JtwigConfigurationBuilder;
import org.jtwig.exception.JtwigException;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;

import static flarestar.bdd.Assert.expect;

@RunWith(Runner.class)
@Describe(Generator.class)
public class GeneratorTest {

    // TODO: we need a simple renderable, complex renderable, failing renderable, class renderable
    public static class SimpleRenderable implements Renderable {
        public int value;

        public SimpleRenderable(int value) {
            this.value = value;
        }

        @Override
        public String getTemplate() {
            return "templates/testSimpleRenderable.twig";
        }

        @Override
        public String getModelAttribute() {
            return "simple";
        }
    }

    public static class ComplexRenderable implements Renderable {

        private SimpleRenderable simple1;
        private SimpleRenderable simple2;
        private int otherValue;

        public ComplexRenderable(int simpleValue1, int simpleValue2, int otherValue) {
            this.simple1 = new SimpleRenderable(simpleValue1);
            this.simple2 = new SimpleRenderable(simpleValue2);
            this.otherValue = otherValue;
        }

        @Override
        public String getTemplate() {
            return "templates/testComplexRenderable.twig";
        }

        @Override
        public String getModelAttribute() {
            return "complex";
        }

        public SimpleRenderable getSimple1() {
            return simple1;
        }

        public SimpleRenderable getSimple2() {
            return simple2;
        }

        public int getOtherValue() {
            return otherValue;
        }
    }

    public static class FailingRenderable implements Renderable {
        @Override
        public String getTemplate() {
            return "templates/testFailingRenderable.twig";
        }

        @Override
        public String getModelAttribute() {
            return "failing";
        }
    }

    public static class TestClassRenderable implements ClassRenderable {

        public SimpleRenderable simpleRenderable;
        public ComplexRenderable complexRenderable;
        public String value;

        public TestClassRenderable(int simpleValue1, ComplexRenderable complex, String stringValue) {
            this.simpleRenderable = new SimpleRenderable(simpleValue1);
            this.complexRenderable = complex;
            this.value = stringValue;
        }

        @Override
        public String getFullGeneratedClassName() {
            return "com.flarestar.drones.base.generation.TestClassRenderableOutput";
        }

        @Override
        public String getTemplate() {
            return "templates/testClassRenderable.twig";
        }

        @Override
        public String getModelAttribute() {
            return "test";
        }
    }

    private MockProcessingEnvironment mockProcessingEnvironment;
    private Generator instance;

    public void beforeEach() {
        this.mockProcessingEnvironment = new MockProcessingEnvironment("");

        Provider<JtwigConfiguration> jtwigConfigProvider = new Provider<JtwigConfiguration>() {
            @Override
            public JtwigConfiguration get() {
                SymbolsHack symbolsHack = new SymbolsHack(instance);
                return JtwigConfigurationBuilder.newConfiguration()
                    .withSymbols(symbolsHack)
                    .withAddon(RenderAddon.class)
                    .build();
            }
        };

        this.instance = new Generator(jtwigConfigProvider, mockProcessingEnvironment);
    }

    @Describe(desc = "#render()")
    public class RenderTest {

        private static final String EXPECTED_SIMPLE_RENDERABLE_OUTPUT = "SIMPLE: 50\n";
        private static final String EXPECTED_COMPLEX_RENDERABLE_OUTPUT = "\n\n<complex value=\"30\">\n\n" +
            "SIMPLE: 10\n\n" +
            "SIMPLE: 20\n\n" +
            "</complex>\n\n" +
            "#";

        @It("should correctly use Jtwig to output renderables to the given output stream")
        public void testSuccess() throws Throwable {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            instance.render(new SimpleRenderable(50), stream);

            expect(stream.toString("UTF-8")).to().be().equal(EXPECTED_SIMPLE_RENDERABLE_OUTPUT);
        }

        @It("should correctly handle nested renderable structures that are used via {% render %} tags")
        public void testSuccess2() throws Throwable {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            instance.render(new ComplexRenderable(10, 20, 30), stream);

            expect(stream.toString("UTF-8")).to().be().equal(EXPECTED_COMPLEX_RENDERABLE_OUTPUT);
        }

        @It("should forward Jtwig exceptions caused by template errors to the caller")
        public void testFailure() {
            expect(new Callable() {
                @Override
                public Object call() throws Exception {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    instance.render(new FailingRenderable(), stream);
                    return null;
                }
            }).to().throw_(JtwigException.class);
        }
    }

    @Describe(desc = "#renderClass()")
    public class RenderClassTest {
        private static final String EXPECTED_CLASS_RENDERABLE_OUTPUT =
            "package com.flarestar.drones.base.generation;\n\n" +
            "class TestClassRenderableOutput {\n" +
            "    private SimpleRenderable = new SimpleRenderable(15);\n\n" +
            "    <complex value=\"3\">\n\n" +
            "    SIMPLE: 1\n\n" +
            "    SIMPLE: 2\n\n" +
            "    </complex>\n\n" +
            "    #\n\n" +
            "    test value\n" +
            "}";

        @It("should render ClassRenderables to new source files created via the ProcessingEnvironment")
        public void testSuccess() throws Throwable {
            ClassRenderable renderable = new TestClassRenderable(15, new ComplexRenderable(1, 2, 3), "test value");
            instance.renderClass(renderable);

            MemoryFileObject createdFile = mockProcessingEnvironment.getMockFiler().getCreatedFile(
                "com/flarestar/drones/base/generation/TestClassRenderableOutput.java");
            CharSequence content = createdFile.getCharContent(false);

            expect(content).to().be().equal(EXPECTED_CLASS_RENDERABLE_OUTPUT);
        }
    }
}
