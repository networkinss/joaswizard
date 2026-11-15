package ch.inss.openapi.joaswizard.application;

import ch.inss.openapi.joaswizard.domain.ApiInfo;
import ch.inss.openapi.joaswizard.domain.GenerationOptions;
import ch.inss.openapi.joaswizard.domain.ResourceDefinition;
import ch.inss.openapi.joaswizard.domain.ResourceName;
import ch.inss.openapi.joaswizard.port.ResourceSource;
import ch.inss.openapi.joaswizard.port.SourceInput;
import ch.inss.openapi.joaswizard.port.SpecRenderer;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Use case: reads the resources from a source and renders the full OpenAPI document.
 * Stateless, one instance can be used by several threads if its sources and renderer can.
 * Errors and warnings go to the collector, the order of the diagnostics is part of the behaviour.
 */
public final class SpecGenerator {

    /** Marker string of a failed part, as returned by the former implementation. */
    public static final String ERROR = "Error";
    private static final String LOGGER_NAME = "ch.inss.openapi.joaswizard.Joaswizard";
    private static final String LOG_CLASS = "ch.inss.openapi.joaswizard.Joaswizard";
    private static final Logger logger = Logger.getLogger(LOGGER_NAME);
    private static final String NEXT_LINE = "\n";

    /**
     * Result of a run.
     *
     * @param spec  the document, null if it could not be generated.
     * @param title the info title that was computed because none was given, null if the given one was used or no
     *              info section was rendered. The caller may write it back to its info object.
     */
    public record Generated(String spec, String title) {
    }

    private final List<ResourceSource> sources;
    private final SpecRenderer renderer;

    /**
     * @param sources  the sources, the first one that supports the input is used.
     * @param renderer the renderer.
     */
    public SpecGenerator(List<ResourceSource> sources, SpecRenderer renderer) {
        this.sources = List.copyOf(sources);
        this.renderer = renderer;
    }

    /**
     * @param input   where the sample data come from.
     * @param options options, its resource is the name of an unnamed (flat) source document.
     * @param c       collector.
     * @return the result, its spec is null if an error occurred or the source had no data.
     */
    public Generated generate(SourceInput input, GenerationOptions options, DiagnosticCollector c) {
        ResourceSource source = sources.stream().filter(s -> s.supports(input)).findFirst().orElse(null);
        if (source == null) {
            c.error("No source available for the input type.");
            return new Generated(null, null);
        }
        List<ResourceDefinition> definitions = source.read(input, options, c);
        if (definitions == null) return new Generated(null, null);
        return this.full(normalize(definitions, options), options, c);
    }

    /** Gives unnamed resources the caller's name and unreadable ones the requested methods (paths are still rendered). */
    private static List<ResourceDefinition> normalize(List<ResourceDefinition> definitions, GenerationOptions o) {
        List<ResourceDefinition> result = new ArrayList<>();
        for (ResourceDefinition d : definitions) {
            ResourceName name = d.name() == null && o.resource() != null ? new ResourceName(o.resource()) : d.name();
            if (d.isUnreadable()) {
                result.add(new ResourceDefinition(name, o.resourceId(), o.methods(), null));
            } else {
                result.add(new ResourceDefinition(name, d.idField(), d.methods(), d.properties()));
            }
        }
        return result;
    }

    private Generated full(List<ResourceDefinition> definitions, GenerationOptions o, DiagnosticCollector c) {
        if (definitions.isEmpty()) return new Generated(null, null);
        String paths = this.paths(definitions, o.doPaths(), c);
        if (paths.startsWith(ERROR)) {
            c.error("Could not process data for OAS paths. " + paths);
        }
        StringBuilder resources = new StringBuilder();
        StringBuilder objects = new StringBuilder();
        int max = 0;
        for (ResourceDefinition definition : definitions) {
            if (max >= o.maxObjects()) {
                c.warning("Maximum number of objects (" + o.maxObjects() + ") reached, further objects are skipped.");
                break;
            }
            resources.append(definition.name() == null ? null : definition.name().value()).append(", ");
            String result = this.schema(definition, o.doSchemas(), c);
            if (ERROR.equals(result)) return new Generated(null, null);
            objects.append(result).append(NEXT_LINE);
            max++;
        }
        if (resources.length() >= 2) resources.delete(resources.length() - 2, resources.length());
        String[] title = new String[1];
        String info = this.info(o.info(), resources.toString(), o.doInfo(), title, c);
        String components = o.doSchemas() ? renderer.componentsHeader() + NEXT_LINE : "";
        return new Generated(info + paths + NEXT_LINE + components + objects, title[0]);
    }

    /**
     * Path sections of all resources.
     *
     * @param definitions resources.
     * @param doPaths     false renders nothing.
     * @param c           collector.
     * @return the paths, empty if skipped.
     */
    public String paths(List<ResourceDefinition> definitions, boolean doPaths, DiagnosticCollector c) {
        logger.fine("Start creating methods.");
        StringBuilder result = new StringBuilder();
        for (ResourceDefinition definition : definitions) {
            if (!doPaths) {
                logger.fine("Skipping creating methods (DoPaths = false) for " + definition.idField());
                continue;
            }
            String rendered = null;
            try {
                rendered = renderer.paths(definition);
            } catch (UncheckedIOException e) {
                logger.logp(Level.SEVERE, LOG_CLASS, "fromPathsTemplate", "Creating CRUD path component failed.", e.getCause());
                c.error("Creating CRUD path component failed: " + e.getCause().getLocalizedMessage());
            }
            result.append(NEXT_LINE).append(rendered);
        }
        if (result.toString().startsWith("\n\n")) {
            result.deleteCharAt(0);
        }
        logger.fine("End creating methods.");
        return result.toString();
    }

    private String schema(ResourceDefinition definition, boolean doSchemas, DiagnosticCollector c) {
        if (!doSchemas) {
            logger.fine("Skipping create schema (doSchemas = false).");
            return "";
        }
        logger.fine("Start creating schema objects for " + definition.name());
        String result = null;
        try {
            if (definition.isUnreadable()) {
                c.error("No data. Please define input file or set sample yaml.");
                return ERROR;
            }
            result = renderer.schema(definition);
        } catch (UncheckedIOException e) {
            logger.logp(Level.SEVERE, LOG_CLASS, "schemaObjects", "Creating OpenAPI section failed.", e.getCause());
            c.error(e.getCause().getLocalizedMessage());
        }
        logger.fine("End creating schema objects.");
        return result;
    }

    /**
     * Renders the info section for the joined resource names. If the info has no title, "Names API" is used and
     * reported in titleOut[0].
     */
    private String info(ApiInfo info, String resourceNames, boolean doInfo, String[] titleOut, DiagnosticCollector c) {
        if (!doInfo) {
            logger.fine("Skipping info section (doInfo = false).");
            return "";
        }
        logger.fine("Start creating info.");
        String result = null;
        try {
            if (resourceNames.length() == 0) {
                c.error("No resource defined.");
                return ERROR;
            }
            ApiInfo used = info;
            if (info.title() == null) {
                titleOut[0] = new ResourceName(resourceNames).cap() + " API";
                used = info.withTitle(titleOut[0]);
            }
            result = renderer.info(used);
        } catch (UncheckedIOException e) {
            logger.logp(Level.SEVERE, LOG_CLASS, "info", "Creating OpenAPI section failed.", e.getCause());
            c.error(e.getCause().getLocalizedMessage());
        }
        logger.fine("End creating info.");
        return result;
    }
}
