package ch.inss.joaswizard;

import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Util implements Constants {

    static boolean writeStringToData(String userFolder, String data, String file) {
        if (userFolder == null) userFolder = OUTPUT_FOLDER;
        if (file == null) file = DEFAULT_OUTPUT_FILE;
        File folder = new File(userFolder);
        if (folder.mkdirs() == false && folder.isDirectory() == false) {
            return false;
        }
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            fos = new FileOutputStream(folder + S + file);
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
            System.out.println("File not found: " + filePath);
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

    static boolean isNumber(String str) {
        return str != null && str.matches("[0-9.]+");
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