package ch.inss.joaswizard;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util implements Constants {

    public static boolean writeStringToData(String userFolder, String data, String file) {
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

    public static String readFromFile(String file) {
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

    public static HashMap<String, Object> readYamlFromString(String strYaml) {
        Yaml yaml = new Yaml();
        HashMap<String, Object> result = yaml.load(strYaml);

        return result;
    }

    public static HashMap<String, Object> readYamlFile(String inputFile) {
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

    public static boolean isNumber(String str) {
        return str != null && str.matches("[0-9.]+");
    }

    public static List<String> getYamlAsString(HashMap<Integer, List<Map<String, String>>> map){
        List<String> result = new ArrayList<>();
        for ( Integer index : map.keySet() ){
            List<Map<String, String>> mapList = map.get(index);
            String yaml = new String();
            for ( Map<String, String> sheetMap : mapList){
                
            }
            
        }
        return result;
    }
}