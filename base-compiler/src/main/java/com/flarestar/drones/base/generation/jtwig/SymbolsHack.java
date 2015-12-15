package com.flarestar.drones.base.generation.jtwig;

import com.flarestar.drones.base.generation.Generator;
import com.google.inject.Inject;
import org.jtwig.parser.config.Symbols;

/**
 * TODO
 */
public class SymbolsHack implements Symbols {

    private Generator generator;

    @Inject
    public SymbolsHack(Generator generator) {
        this.generator = generator;
    }

    @Override
    public String beginOutput() {
        return "{{";
    }

    @Override
    public String endOutput() {
        return "}}";
    }

    @Override
    public String beginTag() {
        return "{%";
    }

    @Override
    public String endTag() {
        return "%}";
    }

    @Override
    public String beginComment() {
        return "{#";
    }

    @Override
    public String endComment() {
        return "#}";
    }

    public Generator getGenerator() {
        return generator;
    }
}
