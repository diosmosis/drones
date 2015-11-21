package com.flarestar.drones.layout.view.directive;

import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.view.directive.exceptions.InvalidDirectiveClassException;
import com.flarestar.drones.layout.view.directive.matchers.AttributeMatcher;
import com.flarestar.drones.layout.view.directive.matchers.TagMatcher;
import com.google.common.base.Predicate;
import com.helger.commons.io.stream.StringInputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.*;

public class DirectiveFactory {

    private final static Set<Class<?>> allDirectives = new HashSet<>();
    private final static Map<Class<?>, DirectiveMatcher> directiveMatchers = new HashMap<>();

    private final static TagMatcher tagDirectiveMatcher = new TagMatcher();
    private final static AttributeMatcher attributeDirectiveMatcher = new AttributeMatcher();

    static {
        Reflections reflections = new Reflections("", new ResourcesScanner());
        Set<String> resources = reflections.getResources(new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String s) {
                return s != null && s.endsWith("directives.xml"); // TODO: there will probably naming collisions eventually
            }
        });

        ClassLoader classLoader = DirectiveFactory.class.getClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream("com.flarestar.drones.layout.directives.xml")) {
            resources.add(DirectiveFactory.readWholeInputStream(stream));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read the com.flarestar.drones.layout.directives.xml resource.");
        }

        for (String resourceXml : resources) {
            List<String> directiveClassNames = getDirectivesDefinedInXml(resourceXml);
            for (String className : directiveClassNames) {
                Class<?> directiveClass;
                try {
                    directiveClass = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new InvalidDirectiveClassException(e);
                }

                if (!Directive.class.isAssignableFrom(directiveClass)) {
                    throw new InvalidDirectiveClassException("Directive class '" + className + "' does not extend 'com.flarestar.drones.layout.Directive'.");
                }

                registerDirective(directiveClass);
            }
        }
    }

    public void detectDirectives(ViewNode node) throws LayoutFileException {
        for (Class<?> directiveClass : allDirectives) {
            DirectiveMatcher matcher = directiveMatchers.get(directiveClass);
            if (!matcher.matches(node, directiveClass)) {
                continue;
            }

            Directive directive;
            try {
                directive = (Directive)directiveClass.getConstructor(ViewNode.class).newInstance(node);
                node.directives.add(directive);
            } catch (ReflectiveOperationException e) {
                throw new InvalidDirectiveClassException("Could not create new instance of directive.", e);
            }
        }
    }

    private static void registerDirective(Class<?> directiveClass) {
        allDirectives.add(directiveClass);
        directiveMatchers.put(directiveClass, getDirectiveMatcher(directiveClass));
    }

    private static DirectiveMatcher getDirectiveMatcher(Class<?> directiveClass) {
        com.flarestar.drones.layout.annotations.directive.DirectiveMatcher annotation =
            directiveClass.getAnnotation(com.flarestar.drones.layout.annotations.directive.DirectiveMatcher.class);

        if (annotation == null) {
            throw new IllegalStateException("Invalid directive, directive class '" + directiveClass.getName()
                + "' has no @DirectiveMatcher.");
        }

        if (annotation.value() == AttributeMatcher.class) {
            return attributeDirectiveMatcher;
        } else {
            return tagDirectiveMatcher;
        }
    }

    private static @Nonnull List<String> getDirectivesDefinedInXml(String resourceXml) {
        StringInputStream input = new StringInputStream(resourceXml, Charset.forName("UTF-8"));

        Document document;
        try {
            document = Jsoup.parse(input, "UTF-8", "", Parser.xmlParser());
        } catch (IOException e) {
            throw new RuntimeException("Unexpected error, failed to parse string input stream.");
        }

        List<String> result = new ArrayList<>();

        Elements elements = document.getElementsByTag("directive");
        for (Element element : elements) {
            String className = element.attr("class");
            if (className == null) {
                // TODO: should log warning
                continue;
            }

            result.add(className);
        }

        return result;
    }

    // TODO: redundanchy w/ LayoutParser
    private static String readWholeInputStream(InputStream inputStream) {
        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
