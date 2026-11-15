package ch.inss.openapi.joaswizard;

import ch.inss.openapi.joaswizard.adapter.output.FileSpecWriter;
import ch.inss.openapi.joaswizard.adapter.render.HandlebarsRenderer;
import ch.inss.openapi.joaswizard.adapter.source.ExcelSource;
import ch.inss.openapi.joaswizard.adapter.source.YamlSource;
import ch.inss.openapi.joaswizard.application.SpecGenerator;
import ch.inss.openapi.joaswizard.port.SpecWriter;

import java.util.List;

/**
 * Builds the adapters and the use case. The parts are created on first use and shared (they hold no per-call state).
 */
final class Wiring {

    private HandlebarsRenderer renderer;
    private SpecGenerator generator;
    private final SpecWriter writer = new FileSpecWriter();

    synchronized HandlebarsRenderer renderer() {
        if (renderer == null) renderer = new HandlebarsRenderer();
        return renderer;
    }

    synchronized SpecGenerator generator() {
        if (generator == null) generator = new SpecGenerator(List.of(new YamlSource(), new ExcelSource()), renderer());
        return generator;
    }

    SpecWriter writer() {
        return writer;
    }
}
