package ch.inss.openapi.joaswizard;

import ch.inss.openapi.joaswizard.adapter.output.FileSpecWriter;
import ch.inss.openapi.joaswizard.adapter.source.MappingFileLoader;
import ch.inss.openapi.joaswizard.adapter.source.YamlSource;
import ch.inss.openapi.joaswizard.application.Numbers;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * Helpers; the Yaml, mapping and number parts are delegating shims to the new packages.
 */
public class Util {

    static boolean writeStringToData(String userFolder, String data, String file) {
        return FileSpecWriter.writeRelative(userFolder, data, file);
    }

    /**
     * Reads the version filtered by Maven into version.properties. Returns "unknown" on failure.
     */
    static String readVersion() {
        try (InputStream in = Util.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (in == null) return "unknown";
            Properties props = new Properties();
            props.load(in);
            String v = props.getProperty("version");
            return (v == null || v.isBlank()) ? "unknown" : v.trim();
        } catch (IOException | RuntimeException e) {
            return "unknown";
        }
    }

    static String readFromFile(String file) {
        return YamlSource.readFile(file);
    }

    static boolean fileExists(String file) {
        return new File(file).isFile();
    }

    /**
     * Generate a LinkedHashMap from a String in Yaml format.
     */
    static LinkedHashMap<String, Object> readYamlFromString(String strYaml) {
        return YamlSource.readYaml(strYaml);
    }

    /**
     * Optional leading minus, digits, optional single decimal part. null or blank = false.
     */
    static boolean isNumber(String str) {
        return Numbers.isNumber(str);
    }

    /**
     * Read a json file and returns a map with maps.
     */
    static CaseInsensitiveMap<String, HashMap<String, String>> getJsonAsMap(String jsonFile) {
        return MappingFileLoader.loadRaw(jsonFile);
    }
}
