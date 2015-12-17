package com.flarestar.drones.base.generation.jtwig;

import com.flarestar.drones.base.generation.Generator;
import org.jtwig.Environment;
import org.jtwig.addons.Addon;
import org.jtwig.addons.AddonModel;
import org.jtwig.compile.CompileContext;
import org.jtwig.content.api.Renderable;
import org.jtwig.exception.*;
import org.jtwig.expressions.api.CompilableExpression;
import org.jtwig.expressions.api.Expression;
import org.jtwig.loader.Loader;
import org.jtwig.parser.model.JtwigPosition;
import org.jtwig.render.RenderContext;
import org.parboiled.Rule;

import java.io.IOException;

/**
 * TODO
 */
public class RenderAddon extends Addon {

    public class Model extends AddonModel<Model> {
        private final JtwigPosition position;
        private CompilableExpression expression;

        public Model(JtwigPosition position, CompilableExpression expression) {
            this.position = position;
            this.expression = expression;
        }

        @Override
        public Renderable compile(CompileContext context) throws CompileException {
            return new CompiledModel(position, expression.compile(context));
        }
    }

    public class CompiledModel implements Renderable {
        private final JtwigPosition position;
        private Expression expression;

        private CompiledModel(JtwigPosition position, Expression expression) {
            this.position = position;
            this.expression = expression;
        }

        @Override
        public void render(RenderContext renderContext) throws RenderException {
            Object value;
            try {
                value = expression.calculate(renderContext);
            } catch (CalculateException e) {
                throw new RenderException(e);
            }

            if (!(value instanceof com.flarestar.drones.base.generation.Renderable)) {
                throw new RenderException(position + ": value supplied to {% render %} does not implement "
                    + "com.flarestar.drones.base.generation.Renderable.");
            }

            com.flarestar.drones.base.generation.Renderable renderable =
                (com.flarestar.drones.base.generation.Renderable)value;

            try {
                generator.render(renderContext, renderable);
            } catch (JtwigException | IOException e) {
                throw new RenderException("Failed to render " + renderable.getClass().getName() + ": " + e.getMessage());
            } catch (RuntimeException e) {
                throw new RuntimeException("Failed to render " + renderable.getClass().getName(), e);
            }
        }
    }

    private Generator generator;

    public RenderAddon(Loader.Resource resource, Environment env) {
        super(resource, env);

        // HACK: we need to get a Generator instance from Guice, but Addons are created by jtwig, so we can'
        // do any sort of injection. instead, we create a custom Symbols class that contains the Generator instance,
        // and assume jtwig has been configured w/ it. TODO: make sure we use this
        this.generator = ((SymbolsHack)env.getConfiguration().getSymbols()).getGenerator();
    }

    @Override
    public AddonModel instance() {
        return null;
    }

    @Override
    public String beginKeyword() {
        return "render";
    }

    @Override
    public String endKeyword() {
        return "endrender";
    }

    @Override
    public Rule startRule() {
        return mandatory(
            Sequence(
                expressionParser().expression(),
                push(new Model(currentPosition(), expressionParser().pop()))
            ),
            new ParseException("render tag must have renderable variable")
        );
    }


}
