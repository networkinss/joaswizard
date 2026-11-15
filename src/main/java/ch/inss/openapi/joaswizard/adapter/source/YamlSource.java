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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads resources from Yaml or JSON (valid Yaml) sample data. Types are guessed from the sample values.
 * A top level key with an object value is a resource. If the first value is not an object, the whole document
 * is one resource; its name is not known here, so its definition has name null (the caller's resource).
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
        /* Loop for objects. */
        int max = 0;
        for (String key : allObjectsMap.keySet()) {
            max++;
            if (max > options.maxObjects()) {
                c.warning("Maximum number of objects (" + options.maxObjects() + ") reached, further objects are skipped.");
                break;
            }
            boolean breakLoop = false;
            LinkedHashMap<String, Object> propMap;
            ResourceName name = null;
            Object ob = allObjectsMap.get(key);
            String cl = ob == null ? "" : ob.getClass().toString();
            if (cl.equals("class java.util.LinkedHashMap")) {
                @SuppressWarnings("unchecked")
                LinkedHashMap<String, Object> objectMap = (LinkedHashMap<String, Object>) ob;
                propMap = objectMap;
                name = new ResourceName(key);
            } else {
                propMap = allObjectsMap;
                breakLoop = true;
            }

            /* Loop for the properties of one object. */
            List<Property> list = new ArrayList<>();
            for (String propKey : propMap.keySet()) {
                list.add(inferrer.infer(propKey, propMap.get(propKey)));
            }
            result.add(new ResourceDefinition(name, options.resourceId(), options.methods(), list));
            if (breakLoop) break;
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
        if (filePath.isFile() == false) {
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
