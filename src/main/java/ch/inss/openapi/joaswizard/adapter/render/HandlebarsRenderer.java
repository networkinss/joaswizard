package ch.inss.openapi.joaswizard.adapter.render;

import ch.inss.openapi.joaswizard.domain.ApiInfo;
import ch.inss.openapi.joaswizard.domain.Property;
import ch.inss.openapi.joaswizard.domain.ResourceDefinition;
import ch.inss.openapi.joaswizard.port.SpecRenderer;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders the specification parts with the Handlebars templates from the classpath.
 * Templates are compiled once per instance.
 */
public final class HandlebarsRenderer implements SpecRenderer {
    private static final String INFO = "info.yaml";
    private static final String PATHS = "pathsCrud.yaml";
    private static final String SCHEMA = "schema.yaml";
    private static final String COMPONENTS = "componentsErrorModel.yaml";

    private final Template info;
    private final Template paths;
    private final Template schema;

    /**
     * @throws UncheckedIOException if a template cannot be compiled.
     */
    public HandlebarsRenderer() {
        Handlebars handlebars = YamlScalar.newHandlebars();
        try {
            this.info = handlebars.compile(INFO);
            this.paths = handlebars.compile(PATHS);
            this.schema = handlebars.compile(SCHEMA);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String info(ApiInfo info) {
        return apply(this.info, new InfoView(info));
    }

    @Override
    public String paths(ResourceDefinition resource) {
        return apply(paths, new PathsView(resource));
    }

    @Override
    public String componentsHeader() {
        return readClasspath(COMPONENTS + ".hbs");
    }

    @Override
    public String schema(ResourceDefinition resource) {
        if (resource.properties() == null) {
            throw new IllegalArgumentException("Resource has no properties: " + resource.name().value());
        }
        List<PropertyView> views = new ArrayList<>();
        for (Property p : resource.properties()) views.add(new PropertyView(p));
        Map<String, Object> context = new HashMap<>();
        context.put("objectName", resource.nested() ? resource.name().value() : resource.name().cap());
        context.put("data", views);
        return renderSchemaContext(context);
    }

    /**
     * Renders the schema template with a prepared context (keys objectName and data).
     * Not part of the port, used by the deprecated createSchemaObjects path.
     *
     * @param context template context.
     * @return rendered schema object.
     */
    public String renderSchemaContext(Map<String, Object> context) {
        return apply(schema, context);
    }

    private static String apply(Template template, Object context) {
        try {
            return template.apply(context);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String readClasspath(String file) {
        InputStream in = HandlebarsRenderer.class.getClassLoader().getResourceAsStream(file);
        if (in == null) return null;
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            int c;
            while ((c = reader.read()) != -1) text.append((char) c);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return text.toString();
    }
}
