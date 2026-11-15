package ch.inss.openapi.joaswizard.adapter.source;

import ch.inss.openapi.joaswizard.Header;
import ch.inss.openapi.joaswizard.application.TypeMapper.MappedType;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads a DB type mapping file (JSON array) from a file path or the classpath.
 */
public final class MappingFileLoader {

    private static final Logger logger = Logger.getLogger(MappingFileLoader.class.getName());

    private MappingFileLoader() {
    }

    /**
     * @param jsonFile file path or classpath resource.
     * @return mapping by DB type (case-insensitive), null if missing or not parsable.
     */
    public static CaseInsensitiveMap<String, MappedType> load(String jsonFile) {
        CaseInsensitiveMap<String, HashMap<String, String>> raw = loadRaw(jsonFile);
        if (raw == null) return null;
        CaseInsensitiveMap<String, MappedType> result = new CaseInsensitiveMap<>();
        for (Map.Entry<String, HashMap<String, String>> e : raw.entrySet()) {
            HashMap<String, String> m = e.getValue();
            result.put(e.getKey(), new MappedType(m.get(Header.OASTYPE.toString()),
                    m.get(Header.OASFORMAT.toString()), m.get(Header.OASPATTERN.toString())));
        }
        return result;
    }

    /**
     * Reads a json file and returns a map with maps.
     *
     * @param jsonFile file path or classpath resource.
     * @return mapping by DB type, null if missing or not parsable.
     */
    public static CaseInsensitiveMap<String, HashMap<String, String>> loadRaw(String jsonFile) {
        final CaseInsensitiveMap<String, HashMap<String, String>> resultMap = new CaseInsensitiveMap<>();
        try {
            String file = YamlSource.readFile(jsonFile);
            if (file == null) {
                file = readClasspath(jsonFile);
            }
            if (file == null) return null;

            // JSON is valid Yaml, so the (safe) Yaml parser reads the mapping file.
            Object parsed = new Yaml(new SafeConstructor(new LoaderOptions())).load(file);
            if (!(parsed instanceof List)) {
                logger.log(Level.SEVERE, "Parsing mapping JSON failed: not a JSON array.");
                return null;
            }
            for (Object entry : (List<?>) parsed) {
                Map<?, ?> jsonObject = (Map<?, ?>) entry;
                HashMap<String, String> map = new HashMap<>();
                String dbtype = (String) jsonObject.get(Header.DBTYPE.toString().toLowerCase());
                map.put(Header.OASTYPE.toString(), (String) jsonObject.get(Header.OASTYPE.toString().toLowerCase()));
                map.put(Header.OASFORMAT.toString(), (String) jsonObject.get(Header.OASFORMAT.toString().toLowerCase()));
                map.put(Header.OASPATTERN.toString(), (String) jsonObject.get(Header.OASPATTERN.toString().toLowerCase()));
                resultMap.put(dbtype, map);
            }
        } catch (YAMLException | ClassCastException e) {
            logger.log(Level.SEVERE, "Parsing mapping JSON failed.", e);
            return null;
        }
        return resultMap;
    }

    /**
     * @param file classpath resource name.
     * @return the UTF-8 content, null if the resource does not exist.
     */
    public static String readClasspath(String file) {
        InputStream inputStream = MappingFileLoader.class.getClassLoader().getResourceAsStream(file);
        if (inputStream == null) return null;
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Reading classpath resource failed: " + file, e);
        }
        return textBuilder.toString();
    }
}
