package ch.inss.openapi.joaswizard.adapter.source;

import ch.inss.openapi.joaswizard.application.DiagnosticCollector;
import ch.inss.openapi.joaswizard.application.TypeInferrer;
import ch.inss.openapi.joaswizard.domain.GenerationOptions;
import ch.inss.openapi.joaswizard.domain.Property;
import ch.inss.openapi.joaswizard.domain.ResourceDefinition;
import ch.inss.openapi.joaswizard.domain.ResourceName;
import ch.inss.openapi.joaswizard.port.ResourceSource;
import ch.inss.openapi.joaswizard.port.SourceInput;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads resources from Yaml or JSON (valid Yaml) sample data. Types are guessed from the sample values.
 * If every top level value is an object, each key is a resource. Otherwise the whole document is one resource; its
 * name is not known here, so its definition has name null (the caller's resource). Objects inside a resource become
 * nested schemas (see {@link NestedSchemas}), returned as nested definitions right after their top level resource.
 */
public final class YamlSource implements ResourceSource {

    private static final Logger logger = Logger.getLogger(YamlSource.class.getName());

    private final TypeInferrer inferrer = new TypeInferrer();

    @Override
    public boolean supports(SourceInput input) {
        return input instanceof SourceInput.YamlText;
    }

    @Override
    public List<ResourceDefinition> read(SourceInput input, GenerationOptions options, DiagnosticCollector c) {
        SourceInput.YamlText yamlText = (SourceInput.YamlText) input;
        LinkedHashMap<String, Object> allObjectsMap;
        try {
            allObjectsMap = readYaml(yamlText.text());
            if (allObjectsMap == null || allObjectsMap.isEmpty()) {
                return null;
            }
        } catch (Exception e) {
            String source = yamlText.file() != null ? "file " + yamlText.file() : "string";
            c.error("Could not parse input (" + source + "): not valid Yaml/JSON.");
            return null;
        }

        List<ResourceDefinition> result = new ArrayList<>();
        NestedSchemas nested = new NestedSchemas(inferrer);
        boolean multi = allObjectsMap.values().stream().allMatch(v -> v instanceof Map);
        if (!multi) {
            /* One object: its name is the caller's resource, maps and lists of maps in it are nested objects. */
            if (options.maxObjects() < 1) {
                c.warning("Maximum number of objects (" + options.maxObjects() + ") reached, further objects are skipped.");
                return result;
            }
            String parent = options.resource() == null ? "" : new ResourceName(options.resource()).cap();
            nested.reserve(parent);
            List<ResourceDefinition> children = new ArrayList<>();
            List<Property> list = nested.properties(allObjectsMap, parent, children);
            result.add(new ResourceDefinition(null, options.resourceId(), options.methods(), list));
            result.addAll(children);
            return result;
        }
        /* Every top level value is an object: one resource per key. */
        for (String key : allObjectsMap.keySet()) nested.reserve(new ResourceName(key).cap());
        int max = 0;
        for (Map.Entry<String, Object> entry : allObjectsMap.entrySet()) {
            max++;
            if (max > options.maxObjects()) {
                c.warning("Maximum number of objects (" + options.maxObjects() + ") reached, further objects are skipped.");
                break;
            }
            ResourceName name = new ResourceName(entry.getKey());
            List<ResourceDefinition> children = new ArrayList<>();
            List<Property> list = nested.properties((Map<?, ?>) entry.getValue(), name.cap(), children);
            nested.complete(name.cap(), list);
            result.add(new ResourceDefinition(name, options.resourceId(), options.methods(), list));
            result.addAll(children);
        }
        return result;
    }

    /**
     * Generates a LinkedHashMap from a String in Yaml format. Safe constructor: input is untrusted, no arbitrary
     * Java types (global tags are rejected).
     *
     * @param strYaml Yaml text.
     * @return the map.
     */
    public static LinkedHashMap<String, Object> readYaml(String strYaml) {
        Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
        LinkedHashMap<String, Object> result = yaml.load(strYaml);
        return result;
    }

    /**
     * @param file file path.
     * @return the content, null if the path is not a file or reading failed.
     */
    public static String readFile(String file) {
        File filePath = new File(file);
        if (!filePath.isFile()) {
            return null;
        }
        String result = null;
        try {
            result = new String(Files.readAllBytes(Paths.get(file)));

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Reading file failed: " + file, e);
        }
        return result;
    }
}
