package ch.inss.joaswizard;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JoaswizardTest implements Constants {

    private final String output = "output" + sep;
    private final String outputPet = "testOutputPet.yml";
    private final String outputContact = "testOutputContact.yml";
    private final String outputString = "testOutputString.yml";
    private final String referenceContact = "src/test/resources/testReferenceContact.yml";
    private final String referencePet = "src/test/resources/testReferencePet.yml";
    private final String referenceString = "src/test/resources/testReferenceString.yml";

    private static Joaswizard jo = new Joaswizard();
    private static boolean cleanUp = true;


    @Test
    @Order(1)
    void testContact() throws Exception {
        Main.createOpenApiFromYamlfile(new String[]{"src/test/resources/Contact.yml", outputContact, "contact"});
        final List<String> list = new ArrayList<>();
        list.add("title: Contact API");
        list.add("$ref: '#/components/schemas/Contact'");
        list.add("Contact:");
        try (Stream<String> lines = Files.lines(Paths.get(output + outputContact))) {
            Stream<String> f = lines.filter(l -> list.contains(l.trim()));
            long x = f.count();
            assertEquals(6.0, x);
        }

        File file1 = new File(output + outputContact);
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }

    }

    @Test
    @Order(2)
    void testFullContact() throws Exception {
        Main.createOpenApiFromYamlfile(new String[]{"src/test/resources/Contact.yml", "testOutputContact.yml", "contact"});
        File file1 = new File(output + "testOutputContact.yml");
        File file2 = new File("src/test/resources/testReferenceContact.yml");
        assertTrue(file1.isFile());
        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(3)
    void testPet() throws Exception {
        Main.createOpenApiFromYamlfile(new String[]{"src/test/resources/Pet.yml", outputPet, "pet", "name", "yamlfile"});
        final List<String> list = new ArrayList<>();
        list.add("title: Pet API");
        list.add("$ref: '#/components/schemas/Pet'");
        try (Stream<String> lines = Files.lines(Paths.get(output + outputPet))) {
            Stream<String> f = lines.filter(l -> list.contains(l.trim()));
            long x = f.count();
            assertEquals(5.0, x);
        }
        File file1 = new File(output + outputPet);
        assertTrue(file1.isFile());
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }

    }

    @Test
    @Order(4)
    void testSchemaPet() throws Exception {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setResourceId("name");
        inputParameter.setResource("PET");

        InputParameter p1 = new InputParameter();
        p1.setResourceId("name");
        p1.setInputFile("src/test/resources/PetObject.yml");
        String schema1 = jo.createSchemaObjects(p1);
        Util.writeStringToData("output", schema1, "testOutputSchema_Object.yml");

        inputParameter.setInputFile("src/test/resources/Pet.yml");
        String schema = jo.createSchemaObjects(inputParameter);
        Util.writeStringToData("output", schema, "testOutputSchema.yml");

        File file1 = new File(output + "testOutputSchema.yml");
        File file2 = new File("src/test/resources/testReferenceSchemaPet.yml");
        File file3 = new File(output + "testOutputSchema_Object.yml");
        assertTrue(file1.isFile());
        assertTrue(file3.isFile());

        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file3, "utf-8"), "There is a breaking change, outputfile is not equal to " + file3.getCanonicalPath());
        if (cleanUp) {
            file1.delete();
            file3.delete();
            assertTrue(file1.isFile() == false);
        }
    }


    @Test
    @Order(5)
    void testCreateFromString() throws Exception {
        String debug = "Zmlyc3RuYW1lOiBNYXgKbmFtZTogTXVzdGVybWFubgpwaG9uZTogMTIzNDU2Nzg5CmVtYWlsOiAibWF4QGV4YW1wbGUuY29tIg==";
        InputParameter inputParameter = new InputParameter();
        inputParameter.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
        inputParameter.setSampleYamlBase64(debug);
        inputParameter.setOutputFile(outputString);
        inputParameter.setResourceId("name");
        inputParameter.setResource("contact");

        jo.createCrudFile(inputParameter);

        File file1 = new File(output + outputString);
        File file2 = new File(referenceString);
        assertTrue(file1.isFile());

        final List<String> list = new ArrayList<>();
        list.add("title: Contact API");
        list.add("$ref: '#/components/schemas/Contact'");
        try (Stream<String> lines = Files.lines(Paths.get(output + outputString))) {
            assertTrue(lines.anyMatch(l -> list.contains(l.trim())));
        }

        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    /**
     * Tests only the path section with get methods.
     */
    @Test
    @Order(6)
    void testGetPet() throws Exception {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setResourceId("name");
        inputParameter.setResource("pet");
        inputParameter.addMethod("GET");
        inputParameter.setInputFile("src/test/resources/Pet.yml");
        inputParameter.setSourceType(InputParameter.Sourcetype.YAMLFILE.toString());
        List<InputParameter> inputList = new ArrayList<>();
        inputList.add(inputParameter);
        String result = jo.createMethodsFromList(inputList);
        boolean ok = Util.writeStringToData(Constants.OUTPUT_FOLDER, result, inputParameter.getOutputFile());
        assertTrue(ok);

        File file1 = new File(output + "get_openapi.yaml");
        assertTrue(file1.isFile());

        final List<String> list = new ArrayList<>();
        list.add("/pet/{name}:");
        list.add("description: Returns all pets");
        list.add("$ref: '#/components/schemas/Pet'");
        try (Stream<String> lines = Files.lines(Paths.get(output + "get_openapi.yaml"))) {
            Stream<String> f = lines.filter(l -> list.contains(l.trim()));
            long x = f.count();
            assertEquals(4.0, x);
        }
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(7)
    void testExcelWrapper() throws Exception {
        ExcelWrapper excelWrapper = new ExcelWrapper();
        HashMap<String, List<Map<String, String>>> integerListHashMap = excelWrapper.readExcelfile("src/test/resources/objectimport.xlsx");
        Assertions.assertEquals(3, integerListHashMap.keySet().size());
    }

    @Test
    @Order(8)
    void testExcel() throws Exception {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setSourceType(InputParameter.Sourcetype.EXCEL);
        inputParameter.setInputFile("src/test/resources/objectimport.xlsx");
        inputParameter.setOutputFile("testOutputExcelsheet0.yml");
        inputParameter.addMethod("get");

        jo.createFromExcel(inputParameter);
        File file1 = new File(output + "testOutputExcelsheet0.yml");
        File file2 = new File("src/test/resources/testReferenceGetMethodList.yml");
        assertTrue(file1.isFile());

        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(9)
    void testCreateMethodsFromSingleYamlObject() throws Exception {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setResourceId("name");
        inputParameter.setResource("pet");
        inputParameter.setInputFile("src/test/resources/Pet.yml");
        inputParameter.setOutputFile("testOutputSingleYamlObject.yml");
        inputParameter.setSourceType(InputParameter.Sourcetype.YAMLFILE.toString());
        inputParameter.addMethod("put");
        inputParameter.addMethod("get");

        jo.createMethodsFromSingleYamlObject(inputParameter);
        File file1 = new File(output + "testOutputSingleYamlObject.yml");
        File file2 = new File("src/test/resources/testReferenceSingleYamlObject.yml");
        assertTrue(file1.isFile());

        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(10)
    void testCreateCrudFromSingleYamlObject() throws Exception {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setResourceId("name");
        inputParameter.setResource("pet");
        inputParameter.setInputFile("src/test/resources/Pet.yml");
        inputParameter.setOutputFile("testOutputCrudSingleYamlObject.yml");
        inputParameter.setSourceType(InputParameter.Sourcetype.YAMLFILE.toString());
        inputParameter.setCrud();

        jo.createMethodsFromSingleYamlObject(inputParameter);
        File file1 = new File(output + "testOutputCrudSingleYamlObject.yml");
        File file2 = new File("src/test/resources/testReferenceCrudSingleYamlObject.yml");
        assertTrue(file1.isFile());

        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(11)
    void testIsValidType() {
        InputParameter inputParameter = new InputParameter();
        assertTrue(inputParameter.isValidSourcetype("excel"));
        assertFalse(inputParameter.isValidSourcetype("nix"));
        assertTrue(InputParameter.isValidMethod("CRUD"));
        assertFalse(InputParameter.isValidMethod("nix"));
    }

    @Test
    @Order(12)
    void testMainYaml() {
        Main.main(new String[]{"src/test/resources/sample.yaml", "testMainYamloutput.yaml", "pet", "name", "yamlfile", "delete,post,patch"});
        File file1 = new File("testMainYamloutput.yaml");
        assertTrue(file1.isFile());

        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(13)
    void testMainExcel() throws Exception {
        Main.main(new String[]{"src/test/resources/objectimport.xlsx", "testMainExceloutput.yaml", "pet", "name", "excel", "delete,post,patch"});
        File file1 = new File("testMainExceloutput.yaml");
        assertTrue(file1.isFile());

        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(14)
    void testReadMapping() throws Exception {
        String file = Util.readFromFile("src/test/resources/mapping.json");
        JSONParser parser = new JSONParser();
        JSONArray obj = (JSONArray) parser.parse(file);
        JSONObject jsonObject = (JSONObject) obj.toArray()[0];
        String dbtype = (String) jsonObject.get("dbtype");
        String oastype = (String) jsonObject.get("oastype");
        String oasformat = (String) jsonObject.get("oasformat");
        String oaspattern = (String) jsonObject.get("oaspattern");
        assertTrue("TIMESTMP".equals(dbtype));
        assertTrue("string".equals(oastype));
        assertTrue("date-time".equals(oasformat));
        assertTrue("".equals(oaspattern));
    }

//    @Test
//    @Order(11)
//    void testSamplesMain() throws Exception {
//        try {
//            // Execute command
//            String command = "java ";
//            Process child = Runtime.getRuntime().exec(command);
//
//            // Get output stream to write from it
//            OutputStream out = child.getOutputStream();
//
//            out.write("cd C:/ /r/n".getBytes());
//            out.flush();
//            out.write("dir /r/n".getBytes());
//            out.close();
//        } catch (IOException e) {
//        }
//    }

}