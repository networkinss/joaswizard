package ch.inss.openapi.joaswizard;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.function.Consumer;

public class Util implements Constants {

    private static final Logger logger = Logger.getLogger(Util.class.getName());

    static boolean writeStringToData(String userFolder, String data, String file) {
        if (userFolder == null) userFolder = ".";
        if (file == null) file = DEFAULT_OUTPUT_FILE;
        if (data == null) data = "";
        try {
            Path path = Paths.get(userFolder + sep + file);
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(path, data.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException | RuntimeException e) {
            logger.severe("Writing file failed: " + userFolder + sep + file + ": " + e);
            return false;
        }
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

    static boolean fileExists(String file) {
        File filePath = new File(file);
        if (filePath.isFile()) {
            return true;
        }
        return false;
    }

    String readFromClasspath(String file) {
//        File filePath = new File(file);
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(file);
//        StringWriter writer = new StringWriter();
        StringBuilder textBuilder = new StringBuilder();
//        try {
//            IOUtils.copy(in, writer, "UTF-8");
        try (
                Reader reader = new BufferedReader(new InputStreamReader
                        (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
//            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Reading classpath resource failed: " + file, e);
        }
        return textBuilder.toString();
    }

    /*** #1
     *  Generate a LinkedHashMap from a String in Yaml format.
     * @param strYaml
     * @return
     */
    static LinkedHashMap<String, Object> readYamlFromString(String strYaml) {
        // SafeConstructor: input is untrusted, no arbitrary Java types (global tags are rejected).
        Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
        LinkedHashMap<String, Object> result = yaml.load(strYaml);
        return result;
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
        return str != null && str.trim().matches("-?[0-9]+(\\.[0-9]+)?");
    }

    /**
     * Read a json file and returns a map with maps.
     */
    static CaseInsensitiveMap<String, HashMap<String, String>> getJsonAsMap(String jsonFile) {
        final CaseInsensitiveMap<String, HashMap<String, String>> resultMap = new CaseInsensitiveMap<>();
        try {
            String file = readFromFile(jsonFile);
            if (file == null) {
                file = new Util().readFromClasspath(jsonFile);
            }
            if (file == null) return null;

            JSONParser parser = new JSONParser();
            JSONArray obj = (JSONArray) parser.parse(file);
            Consumer<Object> lambdaExpression = x -> {
                JSONObject jsonObject = (JSONObject) x;
                HashMap<String, String> map = new HashMap<>();
                String dbtype = (String) jsonObject.get(Header.DBTYPE.toString().toLowerCase());
                String oastype = (String) jsonObject.get(Header.OASTYPE.toString().toLowerCase());
                String oasformat = (String) jsonObject.get(Header.OASFORMAT.toString().toLowerCase());
                String oaspattern = (String) jsonObject.get(Header.OASPATTERN.toString().toLowerCase());
                map.put(Header.OASTYPE.toString(), oastype);
                map.put(Header.OASFORMAT.toString(), oasformat);
                map.put(Header.OASPATTERN.toString(), oaspattern);
                resultMap.put(dbtype, map);
            };
            obj.stream().forEach(lambdaExpression);

        } catch (ParseException pe) {
            logger.log(Level.SEVERE, "Parsing mapping JSON failed.", pe);
            return null;
        }
        return resultMap;
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