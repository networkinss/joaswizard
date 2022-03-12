package ch.inss.joaswizard;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
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

    public void createCrudFile(Parameter parameter) {
        String resultSchema = this.createOpenApi(parameter);
        boolean ok = Util.writeStringToData(Constants.DATA_FOLDER, resultSchema, parameter.getOutputFile());
        if (ok == false) {
            System.out.println("Could not write file " + Constants.DATA_FOLDER + parameter.getOutputFile());
        }
    }

    public String createOpenApi(Parameter parameter) {
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
        HashMap sampleMap = getSampleMap(parameter);
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
        HashMap sampleMap = getSampleMap(parameter);
        try {
            mSchema.execute(writerSchema, sampleMap).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writerSchema.toString();
    }

    private HashMap getSampleMap(Parameter parameter) {
        /** Read input data sample. */
        HashMap<String, Object> map;
        map = Util.readYamlFromString(parameter.getSampleYaml());

        List<SampleData> list = new ArrayList<>();
        HashMap sampleMap = new HashMap<>();
        for (String key : map.keySet()) {
            Object o = (Object) map.get(key);
            String value = (String) map.get(key).toString();
            SampleData sampleData = new SampleData(key, value);
            sampleData.setMinlength(!Util.isNumber(value));
            sampleData.setType(Util.isNumber(value) ? "number" : "string");
            list.add(sampleData);
        }
        sampleMap.put("data", list);
        sampleMap.put("objectName", new SampleData("objectName", parameter.getCapResource()));
        return sampleMap;
    }
}
    
    

