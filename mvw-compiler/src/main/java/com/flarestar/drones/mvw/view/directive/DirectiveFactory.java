package com.flarestar.drones.mvw.view.directive;

import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.directive.exceptions.InvalidDirectiveClassException;
import com.flarestar.drones.mvw.view.directive.matchers.AttributeMatcher;
import com.flarestar.drones.mvw.view.directive.matchers.TagMatcher;
import com.flarestar.drones.mvw.view.scope.Property;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.helger.commons.io.stream.StringInputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

public class DirectiveFactory {

    private final static Set<Class<?>> allDirectives = new HashSet<>();
    private final static Map<Class<?>, DirectiveMatcher> directiveMatchers = new HashMap<>();

    private final static TagMatcher tagDirectiveMatcher = new TagMatcher();
    private final static AttributeMatcher attributeDirectiveMatcher = new AttributeMatcher();

    static {
        Reflections reflections = new Reflections("com.flarestar", new ResourcesScanner(), new SubTypesScanner());

        Set<Class<? extends Directive>> directiveClasses = reflections.getSubTypesOf(Directive.class);
        for (Class<? extends Directive> directiveClass : directiveClasses) {
            try {
                directiveClass = (Class<? extends Directive>) Class.forName(directiveClass.getName());
            } catch (ClassNotFoundException e) {
                throw new InvalidDirectiveClassException(e);
            }

            registerDirective(directiveClass);
        }
    }

    private Injector injector;

    @Inject
    public DirectiveFactory(Injector injector) {
        this.injector = injector;
    }

    public List<Directive> detectDirectives(Element node, GenerationContext context) throws LayoutFileException {
        List<Directive> directives = new ArrayList<>();
        for (Class<?> directiveClass : allDirectives) {
            DirectiveMatcher matcher = directiveMatchers.get(directiveClass);
            if (!matcher.matches(node, directiveClass)) {
                continue;
            }

            Directive directive = make(context, directiveClass);
            directives.add(directive);
        }
        return directives;
    }

    private static void registerDirective(Class<?> directiveClass) {
        allDirectives.add(directiveClass);
        directiveMatchers.put(directiveClass, getDirectiveMatcher(directiveClass));
    }

    private static DirectiveMatcher getDirectiveMatcher(Class<?> directiveClass) {
        com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher annotation =
            directiveClass.getAnnotation(com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher.class);

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

    public Directive make(GenerationContext context, Class<?> directiveClass) throws LayoutFileException {
        Directive directive;
        try {
            directive = (Directive)directiveClass.getConstructor(GenerationContext.class).newInstance(context);
        } catch (ReflectiveOperationException e) {
            throw new InvalidDirectiveClassException("Could not create new instance of directive '"
                + directiveClass.getName() + "'.", e);
        }

        injector.injectMembers(directive);
        directive.postConstruct();
        return directive;
    }

    public Directive makeRootDirective(GenerationContext context, Class<?> directiveClass) throws LayoutFileException {
        Directive directive = make(context, directiveClass);

        // remove properties without bindings
        directive.removePropertiesIf(new Predicate<Property>() {
            @Override
            public boolean apply(@Nonnull Property property) {
                return !property.hasBinding();
            }
        });

        return directive;
    }
}
