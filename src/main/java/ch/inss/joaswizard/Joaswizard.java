package ch.inss.joaswizard;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;

/**
 * Author: Oliver Glas, https://inss.ch.
 */
public class Joaswizard implements Constants {

//    private java.util.logging.Logger logger =  java.util.logging.Logger.getLogger(this.getClass().getName());
    private static Logger logger = null;

    public Joaswizard() {

    }

    static {
        FileHandler fileHandler = null;
        try {
            InputStream stream = Joaswizard.class.getClassLoader().getResourceAsStream("logging.properties");
            try {
                LogManager.getLogManager().readConfiguration(stream);
                logger = Logger.getLogger(Joaswizard.class.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileHandler = new FileHandler("joaswizard.log");
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.addHandler(fileHandler);
    }

    public static void main(String[] args) {
        logger.info("Starting main.");
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

    /** Create all CRUD operations for one object. */
    public void createCrudFile(InputParameter inputParameter) {
        logger.info("Starting create crud file.");
        String resultSchema = this.fromCrudTemplate(inputParameter);
        boolean ok = Util.writeStringToData(Constants.DATA_FOLDER, resultSchema, inputParameter.getOutputFile());
        if (ok == false) {
            System.out.println("Could not write file " + Constants.DATA_FOLDER + inputParameter.getOutputFile());
        }
    }

    /** Create all methods as defined in input parameter object. */
    public void createMethodsFile(InputParameter inputParameter){
        logger.info("Starting create methods.");
        String pathGet = "";
        String pathPost = "";
        String pathDelete = "";
        String pathPut = "";
        String pathPatch = "";
        if (inputParameter.getMethodList().contains(InputParameter.Method.GET)){
            pathGet = this.fromGetTemplate(inputParameter);
        }if (inputParameter.getMethodList().contains(InputParameter.Method.POST)){
           System.out.println("POST not implemented (yet).");
        }if (inputParameter.getMethodList().contains(InputParameter.Method.PUT)){
            System.out.println("PUT not implemented (yet).");
        }if (inputParameter.getMethodList().contains(InputParameter.Method.DELETE)){
            System.out.println("DELETE not implemented (yet).");
        }if (inputParameter.getMethodList().contains(InputParameter.Method.PATCH)){
            System.out.println("PATCH not implemented (yet).");
        }
        
        
        boolean ok = Util.writeStringToData(Constants.DATA_FOLDER, pathGet, inputParameter.getOutputFile());
        if (ok == false) {
            System.out.println("Could not write file " + Constants.DATA_FOLDER + inputParameter.getOutputFile());
        }
    }

    /** Creates one components schema as defined in input paramter. */
    public String createSchema(InputParameter inputParameter) {
        logger.info("Starting create schema.");
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

    private String fromGetTemplate(InputParameter inputParameter) {
        if (inputParameter.getOutputFile() == null || inputParameter.getOutputFile().equals("")) {
            inputParameter.setOutputFile("get_" + Constants.DEFAULT_OUTPUT_FILE);
        }
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
        YamlWrapper yamlWrapper = getYamlAsMap(inputParameter.getSampleYaml());
        HashMap sampleMap = yamlWrapper.getMap();
        
        
        if (yamlWrapper.getName().equals("") == false ){
            inputParameter.setResource(yamlWrapper.getName());
        }
        sampleMap.put("objectName", new PropertyData("objectName", inputParameter.getCapResource()));
        try {
            mBasic.execute(writerPaths, inputParameter).flush();
            mInfo.execute(writerInfo, inputParameter).flush();
            mSchema.execute(writerSchema, sampleMap).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writerInfo + "\n" + writerPaths + "\n" + writerSchema;
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
    
    

