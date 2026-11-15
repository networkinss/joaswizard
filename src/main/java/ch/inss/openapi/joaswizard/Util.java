package ch.inss.openapi.joaswizard;

import ch.inss.openapi.joaswizard.adapter.output.FileSpecWriter;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import ch.inss.openapi.joaswizard.adapter.source.MappingFileLoader;
import ch.inss.openapi.joaswizard.adapter.source.YamlSource;
import ch.inss.openapi.joaswizard.application.Numbers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helpers; the Yaml, mapping and number parts are delegating shims to the new packages.
 */
public class Util implements Constants {

    private static final Logger logger = Logger.getLogger(Util.class.getName());

    static boolean writeStringToData(String userFolder, String data, String file) {
        return FileSpecWriter.writeRelative(userFolder, data, file);
    }

    /** Writes UTF-8 data to the path and creates missing parent folders. */
    static boolean writeToPath(Path path, String data) {
        return FileSpecWriter.writeToPath(path, data);
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
        File filePath = new File(file);
        if (filePath.isFile()) {
            return true;
        }
        return false;
    }

    String readFromClasspath(String file) {
        return MappingFileLoader.readClasspath(file);
    }

    /*** #1
     *  Generate a LinkedHashMap from a String in Yaml format.
     * @param strYaml
     * @return
     */
    static LinkedHashMap<String, Object> readYamlFromString(String strYaml) {
        return YamlSource.readYaml(strYaml);
    }

//    static HashMap<String, Object> readYamlFile(String inputFile) {
//        if (inputFile == null) {
//            inputFile = "src/test/resources/Pet.yml";
//        }
//        File file = new File(inputFile);
//        Yaml yaml = new Yaml();
//        InputStream fis = null;
//        try {
//            fis = new FileInputStream(inputFile);
//        } catch (FileNotFoundException e) {
//        }
//        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
//        return yaml.load(isr);
//    }

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

    /**
     * Splits a String like a filename into the part before the file extension (like .yml) and the extension itself without dot.
     */
//    static String[] splitExtension(String name) {
//        int pos = 0;
//        String[] result = new String[2];
//        pos = name.lastIndexOf(".");
//        if (pos <= 1) {
//            result[0] = name;
//            return result;
//        }
//        result[0] = name.substring(0, pos);
//        result[1] = name.substring(pos + 1, name.length());
//        return result;
//    }

}