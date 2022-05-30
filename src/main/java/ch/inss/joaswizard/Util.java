package ch.inss.joaswizard;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ch.inss.joaswizard.Main.consoleHandler;

public class Util implements Constants {
    
//    private static Logger logger = null;
//    
//    static {
//        consoleHandler.setLevel(Level.ALL);
//        Formatter formatter = new LogFormatter();
//        consoleHandler.setFormatter(formatter);
//        logger = Logger.getLogger(Util.class.getName());
//        logger.addHandler(consoleHandler);
//        logger.setLevel(Level.INFO);
//        logger.setUseParentHandlers(false);
//    }

    static boolean writeStringToData(String userFolder, String data, String file) {
        if (userFolder == null) userFolder = ".";
        if (file == null) file = DEFAULT_OUTPUT_FILE;
        File folder = new File(userFolder);
        if (folder.mkdirs() == false && folder.isDirectory() == false) {
            return false;
        }
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            fos = new FileOutputStream(folder + sep + file);
            bos = new BufferedOutputStream(fos);
            byte[] bytes = data.getBytes();
            bos.write(bytes);
            bos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
                bos.close();
            } catch (IOException ioe) {
            }
        }
        return true;
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
            e.printStackTrace();
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
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(file);
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(in, writer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    /*** #1
     *  Generate a LinkedHashMap from a String in Yaml format.
     * @param strYaml
     * @return
     */
    static LinkedHashMap<String, Object> readYamlFromString(String strYaml) {
        Yaml yaml = new Yaml();
        LinkedHashMap<String, Object> result = yaml.load(strYaml);
        return result;
    }

    static HashMap<String, Object> readYamlFile(String inputFile) {
        if (inputFile == null) {
            inputFile = "src/test/resources/Pet.yml";
        }
        File file = new File(inputFile);
        Yaml yaml = new Yaml();
        InputStream fis = null;
        try {
            fis = new FileInputStream(inputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        return yaml.load(isr);
    }

    /** regex =  "[0-9.]+", null = false, */
    static boolean isNumber(String str) {
        return str != null && str.trim().matches("[0-9.]+");
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
                HashMap<String,String> map = new HashMap<>();
                String dbtype = (String) jsonObject.get(Header.DBTYPE.toString().toLowerCase());
                String oastype = (String) jsonObject.get(Header.OASTYPE.toString().toLowerCase());
                String oasformat = (String) jsonObject.get(Header.OASFORMAT.toString().toLowerCase());
                String oaspattern = (String) jsonObject.get(Header.OASPATTERN.toString().toLowerCase());
                map.put(Header.OASTYPE.toString(),oastype);
                map.put(Header.OASFORMAT.toString(), oasformat);
                map.put(Header.OASPATTERN.toString(), oaspattern);
                resultMap.put(dbtype, map);
            };
            obj.stream().forEach( lambdaExpression);
            
        }catch(ParseException pe){
            pe.printStackTrace();
            return null;
        }
        return resultMap;
    }

    /**
     * Splits a String like a filename into the part before the file extension (like .yml) and the extension itself without dot.
     */
    static String[] splitExtension(String name) {
        int pos = 0;
        String[] result = new String[2];
        pos = name.lastIndexOf(".");
        if (pos <= 1) {
            result[0] = name;
            return result;
        }
        result[0] = name.substring(0, pos);
        result[1] = name.substring(pos + 1, name.length());
        return result;
    }

}