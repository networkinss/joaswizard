package ch.inss.joaswizard;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Author: Oliver Glas, https://inss.ch.
 */
public class Joaswizard implements Constants {

    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Need four parameter.");
            System.out.println("Usage: <inputfile> <outpufile> <resource> <Idfield>");
            System.exit(1);
        }
        Parameter parameter = new Parameter();
        parameter.setInputFile(args[0]);
        parameter.setOutputFile(args[1]);
        parameter.setResource(args[2]);
        if (args.length >= 4) {
            parameter.setResourceId(args[3]);    
        }else{
            parameter.setResourceId("id");
        }
        
        if (args.length >= 5) {
            parameter.setSourceType(args[4]);
        }else{
            parameter.setSourceType("file");
        }
        Joaswizard joaswizard = new Joaswizard();
        joaswizard.createCrudFile(parameter);
    }

    /** Create all CRUD operations for one object. */
    public void createCrudFile(Parameter parameter) {
        String resultSchema = this.fromCrudTemplate(parameter);
        boolean ok = Util.writeStringToData(Constants.DATA_FOLDER, resultSchema, parameter.getOutputFile());
        if (ok == false) {
            System.out.println("Could not write file " + Constants.DATA_FOLDER + parameter.getOutputFile());
        }
    }

    public String fromCrudTemplate(Parameter parameter) {
        if (parameter.getOutputFile() == null || parameter.getOutputFile().equals("")) {
            parameter.setOutputFile(Constants.DEFAULT_OUTPUT_FILE);
        }
        if (parameter.getInputFile() == null || parameter.getInputFile().equals("")) {
            parameter.setInputFile("src/test/resources/Pet.yml");
        }
        String dStr = null;
        if (parameter.getSourceType() != null && parameter.getSourceType().equals("file")) {
            dStr = Util.readFromFile(parameter.getInputFile());
            parameter.setSampleYaml(dStr);
        }
        if (parameter.getSampleYaml() == null || parameter.getSampleYaml().length() < 3) {
            System.out.println("No sample yaml with data. Please define input file or set sample yaml.");
            System.exit(1);
        }
        System.out.println(parameter);

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mBasic = mf.compile(crudTemplate);
        Mustache mSchema = mf.compile(schemaTemplate);

        StringWriter writer = new StringWriter();
        StringWriter writerSchema = new StringWriter();
        /** Read input data sample. */
        YamlWrapper yamlWrapper = getYamlAsMap(parameter.getSampleYaml());
        HashMap sampleMap = yamlWrapper.getMap();
        
        sampleMap.put("objectName", new inputData("objectName", parameter.getCapResource()));
        try {
            mBasic.execute(writer, parameter).flush();
            mSchema.execute(writerSchema, sampleMap).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer + "\n" + writerSchema;
    }

    public String createSchema(Parameter parameter) {
        System.out.println(parameter);
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mSchema = mf.compile(schemaTemplate);
        StringWriter writerSchema = new StringWriter();
        YamlWrapper yamlWrapper = getYamlAsMap(parameter.getSampleYaml());
        HashMap sampleMap = yamlWrapper.getMap();
        
        sampleMap.put("objectName", new inputData("objectName", parameter.getCapResource()));
        try {
            mSchema.execute(writerSchema, sampleMap).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writerSchema.toString();
    }
    
    /** Providing HashMap with input data for the Mustache engine. */
    private YamlWrapper getYamlAsMap(String yamlFile) {
        /** Read input data sample. */
        HashMap<String, Object> map;
        HashMap resultMap = new HashMap<>();
        YamlWrapper yamlWrapper = new YamlWrapper("",new HashMap());
        map = Util.readYamlFromString(yamlFile);
        if (map == null || map.isEmpty()){
            return yamlWrapper;
        }
        Object ob = map.get(map.keySet().iterator().next());
        String cl = ob.getClass().toString(); 
        if ( cl.equals("class java.util.LinkedHashMap") == true){
            map = (LinkedHashMap<String, Object>) ob;
            
        }

        List<inputData> list = new ArrayList<>();
        
        for (String key : map.keySet()) {
//            Object o = map.get(key);
            String value = map.get(key).toString();
            inputData sampleData = new inputData(key, value);
            sampleData.setMinlength(!Util.isNumber(value));
            sampleData.setType(Util.isNumber(value) ? "number" : "string");
            list.add(sampleData);
        }
        resultMap.put("data", list);
//        sampleMap.put("objectName", new inputData("objectName", parameter.getCapResource()));
        yamlWrapper.setMap(resultMap);
        return yamlWrapper;
    }
}
    
    

