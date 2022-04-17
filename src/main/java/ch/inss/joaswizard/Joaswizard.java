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
        InputParameter inputParameter = new InputParameter();
        inputParameter.setInputFile(args[0]);
        inputParameter.setOutputFile(args[1]);
        inputParameter.setResource(args[2]);
        if (args.length >= 4) {
            inputParameter.setResourceId(args[3]);    
        }else{
            inputParameter.setResourceId("id");
        }
        
        if (args.length >= 5) {
            inputParameter.setSourceType(args[4]);
        }else{
            inputParameter.setSourceType("file");
        }
        Joaswizard joaswizard = new Joaswizard();
        joaswizard.createCrudFile(inputParameter);
    }
    
    public void createMethodsFile(InputParameter inputParameter){
        String resultSchema = this.fromGetTemplate(inputParameter);
        boolean ok = Util.writeStringToData(Constants.DATA_FOLDER, resultSchema, inputParameter.getOutputFile());
        if (ok == false) {
            System.out.println("Could not write file " + Constants.DATA_FOLDER + inputParameter.getOutputFile());
        }
    }

    private String fromGetTemplate(InputParameter inputParameter) {
        if (inputParameter.getOutputFile() == null || inputParameter.getOutputFile().equals("")) {
            inputParameter.setOutputFile("get_" + Constants.DEFAULT_OUTPUT_FILE);
        }
//        if (parameter.getInputFile() == null || parameter.getInputFile().equals("")) {
//            parameter.setInputFile("src/test/resources/Pet.yml");
//        }
        String dStr = null;
        if (inputParameter.getSourceType() != null && inputParameter.getSourceType().equals("file")) {
            dStr = Util.readFromFile(inputParameter.getInputFile());
            inputParameter.setSampleYaml(dStr);
        }
//        if (parameter.getSampleYaml() == null || parameter.getSampleYaml().length() < 3) {
//            System.out.println("No sample yaml with data. Please define input file or set sample yaml.");
//            System.exit(1);
//        }
        System.out.println(inputParameter);

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mBasic = mf.compile(getTemplate);
        Mustache mSchema = mf.compile(schemaTemplate);
        Mustache mInfo = mf.compile(infoTemplate);

        StringWriter writerPaths = new StringWriter();
        StringWriter writerSchema = new StringWriter();
        StringWriter writerInfo = new StringWriter();
        /** Read input data sample. */
        //TODO YamlWrapper yamlWrapper = getYamlAsMap(parameter.getSampleYaml());
        //TODO HashMap sampleMap = yamlWrapper.getMap();
        
        
//        if (yamlWrapper.getName().equals("") == false ){
//            parameter.setResource(yamlWrapper.getName());
//        }
//        sampleMap.put("objectName", new PropertyData("objectName", parameter.getCapResource()));
        try {
            mBasic.execute(writerPaths, inputParameter).flush();
            mInfo.execute(writerInfo, inputParameter).flush();
//            mSchema.execute(writerSchema, sampleMap).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writerInfo + "\n" + writerPaths + "\n" + writerSchema;
    }

    /** Create all CRUD operations for one object. */
    public void createCrudFile(InputParameter inputParameter) {
        String resultSchema = this.fromCrudTemplate(inputParameter);
        boolean ok = Util.writeStringToData(Constants.DATA_FOLDER, resultSchema, inputParameter.getOutputFile());
        if (ok == false) {
            System.out.println("Could not write file " + Constants.DATA_FOLDER + inputParameter.getOutputFile());
        }
    }

    private String fromCrudTemplate(InputParameter inputParameter) {
        if (inputParameter.getOutputFile() == null || inputParameter.getOutputFile().equals("")) {
            inputParameter.setOutputFile(Constants.DEFAULT_OUTPUT_FILE);
        }
        if (inputParameter.getInputFile() == null || inputParameter.getInputFile().equals("")) {
            inputParameter.setInputFile("src/test/resources/Pet.yml");
        }
        String dStr = null;
        if (inputParameter.getSourceType() != null && inputParameter.getSourceType().equals("file")) {
            dStr = Util.readFromFile(inputParameter.getInputFile());
            inputParameter.setSampleYaml(dStr);
        }
        if (inputParameter.getSampleYaml() == null || inputParameter.getSampleYaml().length() < 3) {
            System.out.println("No sample yaml with data. Please define input file or set sample yaml.");
            System.exit(1);
        }
        System.out.println(inputParameter);

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mBasic = mf.compile(fullCrudTemplate);
        Mustache mSchema = mf.compile(schemaTemplate);

        StringWriter writer = new StringWriter();
        StringWriter writerSchema = new StringWriter();
        /** Read input data sample. */
        YamlWrapper yamlWrapper = getYamlAsMap(inputParameter.getSampleYaml());
        HashMap sampleMap = yamlWrapper.getMap();
        if (yamlWrapper.getName().equals("") == false ){
            inputParameter.setResource(yamlWrapper.getName());
        }
        sampleMap.put("objectName", new PropertyData("objectName", inputParameter.getCapResource()));
        try {
            mBasic.execute(writer, inputParameter).flush();
            mSchema.execute(writerSchema, sampleMap).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer + "\n" + writerSchema;
    }

    public String createSchema(InputParameter inputParameter) {
        System.out.println(inputParameter);
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mSchema = mf.compile(schemaTemplate);
        StringWriter writerSchema = new StringWriter();
        YamlWrapper yamlWrapper = getYamlAsMap(inputParameter.getSampleYaml());
        HashMap sampleMap = yamlWrapper.getMap();
        if (yamlWrapper.getName().equals("") == false ){
            inputParameter.setResource(yamlWrapper.getName());
        }
        
        sampleMap.put("objectName", new PropertyData("objectName", inputParameter.getCapResource()));
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
        String firstKey = map.keySet().iterator().next();
        Object ob = map.get(firstKey);
        String cl = ob.getClass().toString(); 
        if ( cl.equals("class java.util.LinkedHashMap") == true){
            map = (LinkedHashMap<String, Object>) ob;
            yamlWrapper.setName(firstKey);   
        }

        List<PropertyData> list = new ArrayList<>();
        
        for (String key : map.keySet()) {
//            Object o = map.get(key);
            String value = map.get(key).toString();
            PropertyData sampleData = new PropertyData(key, value);
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
    
    

