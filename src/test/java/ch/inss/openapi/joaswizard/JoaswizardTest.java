package ch.inss.openapi.joaswizard;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JoaswizardTest implements Constants {

    private final static String output = "output" + sep;
    private final static String outputPet = output + "test03_OutputPet.yml";
    private final static String outputContact1 = output + "test01_OutputContact.yml";
    private final static String outputContact2 = output + "test02_OutputContact.yml";
    private final static String outputContactJSON = output + "test24_OutputContactJSON.yml";
    private final static String outputSchema1 = "test04_OutputSchema1.yml";
    private final static String outputSchema2 = "test04_OutputSchema2.yml";
    private final static String outputMinimalString = output + "test05_OutputMinimalString.yml";
    private final static String outputString = output + "test06_OutputString.yml";
    private final static String outputGetPet = "test07_OutputGetPet.yml";
    private final static String outputExcel = output + "test09_OutputExcelsheet.yml";
    private final static String outputExcelInputStream = "test23_OutputExcelsheetInputStream.yml";
    private final static String outputDBFieldsExcel = output + "test10_OutputDBFieldsExcelsheet.yml";
    private final static String outputMySQLDBFieldsExcel = output + "test11_OutputMySQLDBFieldsExcelsheet_postpatchdelete.yml";
    private final static String outputMainExcel = output + "test18_MainExceloutput.yaml";
    private final static String outputMainYaml = output + "test14_MainYamloutput.yaml";
    private final static String outputMainJSON = output + "test25_MainJSONoutput.yaml";
    private final static String outputSingleYamlObject = output + "test11_OutputSingleYamlObject.yml";
    private final static String outputCrudSingleYamlObject = output + "test12_OutputCrudSingleYamlObject.yml";
    private final static String outputCrudMultipleYamlObject = output + "test14_OutputCrudMultipleYamlObject.yml";
    private final static String outputCrudMaxOneObject = output + "test14_OutputCrudMaxOneObject.yml";
    private final static String outputCustomInfo = "test22_CustomInfo.yml";

    private static Joaswizard jo = new Joaswizard();
    private static boolean cleanUp = true;

    @BeforeAll
    static void initialize() {
        File outputFolder = new File(output);
        if (outputFolder.isDirectory() == false) {
            outputFolder.mkdir();
        }
    }

    @Test
    @Order(1)
    void testContact() throws Exception {
        Main.createOpenApiFromYamlfile(new String[]{"src/test/resources/Contact.yml", outputContact1, "contact"});
        final List<String> list = new ArrayList<>();
        list.add("title: Contact API");
        list.add("$ref: '#/components/schemas/Contact'");
        list.add("name: John Doe");
        try (Stream<String> lines = Files.lines(Paths.get(outputContact1))) {
            Stream<String> f = lines.filter(l -> list.contains(l.trim()));
            long x = f.count();
            assertEquals(6.0, x);
        }

        File file1 = new File(outputContact1);
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }
    @Test
    @Order(2)
    void testFullContact() throws Exception {
        Main.createOpenApiFromYamlfile(new String[]{"src/test/resources/Contact.yml", outputContact2, "contact"});
        File file1 = new File(outputContact2);
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
        try (Stream<String> lines = Files.lines(Paths.get(outputPet))) {
            Stream<String> f = lines.filter(l -> list.contains(l.trim()));
            long x = f.count();
            assertEquals(5.0, x);
        }
        File file1 = new File(outputPet);
        assertTrue(file1.isFile());
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }

    }
    @Test
    @Order(4)
    void testSchemaPet() throws Exception {
        InputParameter p1 = new InputParameter();
        p1.setResourceId("name");
        p1.setInputFile("src/test/resources/YamlObjects.yml");
        p1.setSampleYamlData(Util.readFromFile(p1.getInputFile()));
        List<InputParameter> inputParameterList1 = jo.createMustacheDataFromYaml(p1);
        String schema1 = "";
        for (InputParameter p : inputParameterList1) {
            schema1 = schema1 + jo.createSchemaObjects(p) + nextLine;
        }
        Util.writeStringToData(output, schema1, outputSchema1);

        InputParameter p2 = new InputParameter();
        p2.setResourceId("name");
        p2.setResource("PET");
        p2.setInputFile("src/test/resources/Pet.yml");
        p2.setSampleYamlData(Util.readFromFile(p2.getInputFile()));
        List<InputParameter> inputParameterList2 = jo.createMustacheDataFromYaml(p2);
        String schema2 = "";
        for (InputParameter p : inputParameterList2) {
            schema2 = schema2 + jo.createSchemaObjects(p) + nextLine;
        }
//        String schema2 = jo.createSchemaObjects(p2);
        Util.writeStringToData(output, schema2, outputSchema2);

        File file1 = new File(output + outputSchema1);
        File file2 = new File("src/test/resources/testReferenceSchemaPetObject.yml");
        File file3 = new File(output + outputSchema2);
        File file4 = new File("src/test/resources/testReferenceSchemaPet.yml");
        assertTrue(file1.isFile());
        assertTrue(file3.isFile());

        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        Assertions.assertEquals(FileUtils.readFileToString(file3, "utf-8"), FileUtils.readFileToString(file4, "utf-8"), "There is a breaking change, outputfile is not equal to " + file4.getCanonicalPath());
        if (cleanUp) {
            file1.delete();
            file3.delete();
            assertTrue(file1.isFile() == false);
        }
    }
    @Test
    @Order(5)
    void testCreateFromMinimalString() throws Exception {
        String string64 = "price: 12.05";
        InputParameter inputParameter = new InputParameter();
        inputParameter.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
        inputParameter.setSampleYamlData(string64);
        inputParameter.setOutputFile(outputMinimalString);
//        inputParameter.setResourceId("ID");
        inputParameter.setResource("object");

        jo.createCrudFromYamlToFile(inputParameter);

        File file1 = new File(outputMinimalString);
        File file2 = new File("src/test/resources/testReferenceMinimalString.yml");
        assertTrue(file1.isFile());

        final List<String> checkList = new ArrayList<>();
        checkList.add("title: Object API");
        checkList.add("$ref: '#/components/schemas/Object'");
        checkList.add("example: 12.05");

        List<String> allLines = Files.readAllLines(Paths.get(outputMinimalString)).stream().map(String::trim).collect(Collectors.toList());
        try (Stream<String> lines = checkList.stream()) {//Files.lines(Paths.get(outputMinimalString))) {
            assertTrue(lines.allMatch(l -> allLines.contains(l)));
        }

        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(6)
    void testCreateFromString() throws Exception {
        String string64 = "Zmlyc3RuYW1lOiBNYXgKbmFtZTogTXVzdGVybWFubgpwaG9uZTogMTIzNDU2Nzg5CmVtYWlsOiAibWF4QGV4YW1wbGUuY29tIg==";
        InputParameter inputParameter = new InputParameter();
        inputParameter.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
        inputParameter.setSampleYamlBase64(string64);
        inputParameter.setOutputFile(outputString);
        inputParameter.setResourceId("name");
        inputParameter.setResource("contact");

        jo.createCrudFromYamlToFile(inputParameter);

        File file1 = new File(outputString);
        File file2 = new File("src/test/resources/testReferenceString.yml");
        assertTrue(file1.isFile());

        final List<String> checkList = new ArrayList<>();
        checkList.add("title: Contact API");
        checkList.add("$ref: '#/components/schemas/Contact'");
        List<String> allLines = Files.readAllLines(Paths.get(outputString)).stream().map(String::trim).collect(Collectors.toList());
        try (Stream<String> lines = checkList.stream()) {
            assertTrue(lines.allMatch(l -> allLines.contains(l)));
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
    @Order(7)
    void testGetPet() throws Exception {
//        String outputGetPet = "get_openapi.yaml";
        InputParameter inputParameter = new InputParameter();
        inputParameter.setResourceId("name");
        inputParameter.setResource("pet");
        inputParameter.addMethods("GET");
        inputParameter.setOutputFile(outputGetPet);
        inputParameter.setInputFile("src/test/resources/Pet.yml");
        inputParameter.setSourceType(InputParameter.Sourcetype.YAMLFILE);
        List<InputParameter> inputList = new ArrayList<>();
        inputList.add(inputParameter);
        String result = jo.createMethodsFromList(inputList);
        boolean ok = Util.writeStringToData(output, result, inputParameter.getOutputFile());
        assertTrue(ok);

        File file1 = new File(output + outputGetPet);
        assertTrue(file1.isFile());

        final List<String> list = new ArrayList<>();
        list.add("/pet/{name}:");
        list.add("description: List of pets.");
        list.add("$ref: '#/components/schemas/Pet'");
        try (Stream<String> lines = Files.lines(Paths.get(output + outputGetPet))) {
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
    @Order(8)
    void testExcelWrapper() throws Exception {
        ExcelWrapper excelWrapper = new ExcelWrapper();
        HashMap<String, List<Map<String, String>>> integerListHashMap = excelWrapper.readExcelfile("src/test/resources/objectimport.xlsx");
        Assertions.assertEquals(3, integerListHashMap.keySet().size());
    }

    @Test
    @Order(9)
    void testExcel() throws Exception {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setSourceType(InputParameter.Sourcetype.EXCEL);
        inputParameter.setInputFile("src/test/resources/objectimport.xlsx");
        inputParameter.setOutputFile(outputExcel);
        inputParameter.addMethods("get");

        jo.createFromExcelToFile(inputParameter);
        File file1 = new File(outputExcel);
        File file2 = new File("src/test/resources/testReferenceGetMethodList.yml");
        assertTrue(file1.isFile());

        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(10)
    void testExcelDBFields() throws Exception {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setSourceType(InputParameter.Sourcetype.EXCEL);
        inputParameter.setInputFile("src/test/resources/objectimport_onlyDBfieldformat.xlsx");
        inputParameter.setOutputFile(outputDBFieldsExcel);
        inputParameter.addMethods("get");

        jo.createFromExcelToFile(inputParameter);
        File file1 = new File(outputDBFieldsExcel);
        File file2 = new File("src/test/resources/testReferenceGetMethodList.yml");
        assertTrue(file1.isFile());

        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(11)
    void testExcelMySQLDBFields() throws Exception {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setSourceType(InputParameter.Sourcetype.EXCEL);
        inputParameter.setInputFile("src/test/resources/mySQLObjectimport.xlsx");
        /** Check the custom mapping in this test. */
        inputParameter.setMappingFile("src/test/resources/mysqlMapping.json");
        /** To get a mapping for VARCHAR(30) fields having only VARCHAR without brackets in the mapping.json. */
        inputParameter.setPrefixMatch(true);
        inputParameter.setOutputFile(outputMySQLDBFieldsExcel);
        //TODO
        inputParameter.addMethods("post,patch,delete");

        jo.createFromExcelToFile(inputParameter);
        File file1 = new File(outputMySQLDBFieldsExcel);
        File file2 = new File("src/test/resources/testReferenceMySQLDBFieldsExcelsheet.yml");
        assertTrue(file1.isFile());

        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(12)
    void testCreateMethodsFromSingleYamlObject() throws Exception {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setResourceId("name");
        inputParameter.setResource("pet");
        inputParameter.setInputFile("src/test/resources/Pet.yml");
        inputParameter.setOutputFile(outputSingleYamlObject);
        inputParameter.setSourceType(InputParameter.Sourcetype.YAMLFILE.toString());
        inputParameter.addMethods("put");
        inputParameter.addMethods("get");

        jo.createFromYamlToFile(inputParameter);
        File file1 = new File(outputSingleYamlObject);
        File file2 = new File("src/test/resources/testReferenceSingleYamlObject.yml");
        assertTrue(file1.isFile());

        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(13)
    void testCreateCrudFromSingleYamlObject() throws Exception {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setResourceId("name");
        inputParameter.setResource("pet");
        inputParameter.setInputFile("src/test/resources/Pet.yml");
        inputParameter.setOutputFile(outputCrudSingleYamlObject);
        inputParameter.setSourceType(InputParameter.Sourcetype.YAMLFILE.toString());
        inputParameter.setCrud();

        jo.createFromYamlToFile(inputParameter);
        File file1 = new File(outputCrudSingleYamlObject);
        File file2 = new File("src/test/resources/testReferenceCrudSingleYamlObject.yml");
        assertTrue(file1.isFile());

        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(14)
    void testCreateCrudFromMultipleYamlObject() throws Exception {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setResourceId("name");
        inputParameter.setResource("pet");
        inputParameter.setInputFile("src/test/resources/YamlObjects.yml");
        inputParameter.setOutputFile(outputCrudMultipleYamlObject);
        inputParameter.setSourceType(InputParameter.Sourcetype.YAMLFILE.toString());
        inputParameter.setCrud();

        jo.createFromYamlToFile(inputParameter);
        File file1 = new File(outputCrudMultipleYamlObject);
        File file2 = new File("src/test/resources/testReferenceCrudMultipleYamlObject.yml");
        assertTrue(file1.isFile());

        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(15)
    void testIsValidType() {
        InputParameter inputParameter = new InputParameter();
        assertTrue(inputParameter.isValidSourcetype("excel"));
        assertFalse(inputParameter.isValidSourcetype("nix"));
        assertTrue(InputParameter.isValidMethod("CRUD"));
        assertFalse(InputParameter.isValidMethod("nix"));
    }

    @Test
    @Order(16)
    void testMainYaml() {
        Main.main(new String[]{"src/test/resources/YamlObjects.yml", outputMainYaml, "pet", "name", "yamlfile", "delete,post,patch"});
        File file1 = new File(outputMainYaml);
        assertTrue(file1.isFile());

        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(17)
    void testYaml() {
        Main.main(new String[]{"src/test/resources/sample.yaml", outputMainYaml, "shoe", "name", "yamlfile", "delete,post,patch"});
        File file1 = new File(outputMainYaml);
        assertTrue(file1.isFile());

        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(18)
    void testMainExcel() throws Exception {
        Main.main(new String[]{"src/test/resources/objectimport.xlsx", outputMainExcel, "pet", "id", "excel", "delete,post,patch"});
        File file1 = new File(outputMainExcel);
        File file2 = new File("src/test/resources/testReferenceMySQLDBFieldsExcelsheet.yml");
        assertTrue(file1.isFile());
        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());

        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(19)
    void testReadMapping() throws Exception {
        String file = Util.readFromFile("src/main/resources/mapping.json");
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

    @Test
    @Order(20)
    void testCreateCrudFromMaxOneObject() throws Exception {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setResourceId("name");
        inputParameter.setResource("pet");
        inputParameter.setInputFile("src/test/resources/YamlObjects.yml");
        inputParameter.setOutputFile(outputCrudMaxOneObject);
        inputParameter.setSourceType(InputParameter.Sourcetype.YAMLFILE.toString());
        inputParameter.setCrud();
        inputParameter.setMaxobjects(1);

        jo.createFromYamlToFile(inputParameter);
        File file1 = new File(outputCrudMaxOneObject);
        File file2 = new File("src/test/resources/testReferenceCrudMaxOneObject.yml");
        assertTrue(file1.isFile());

        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

    @Test
    @Order(21)
    void testWrongInput() throws Exception {
        String string64 = "price"; //Wrong input, not Yaml format.
        InputParameter inputParameter = new InputParameter();
        inputParameter.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
        inputParameter.setSampleYamlData(string64);
        inputParameter.setOutputFile(outputMinimalString);
        inputParameter.setResource("object");

        String output = jo.createCrudFromYamlToString(inputParameter);

        assertNotNull(output);
        Assertions.assertTrue(jo.getErrorMessage().equals("Input is not Yaml format."));
    }

    @Test
    @Order(22)
    void testCustomInfo() throws Exception {
        String string64 = "Zmlyc3RuYW1lOiBNYXgKbmFtZTogTXVzdGVybWFubgpwaG9uZTogMTIzNDU2Nzg5CmVtYWlsOiAibWF4QGV4YW1wbGUuY29tIg==";
        InputParameter inputParameter = new InputParameter();
        inputParameter.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
        inputParameter.setSampleYamlBase64(string64);
        inputParameter.setResourceId("name");
        inputParameter.setResource("contact");

        OasInfo oasInfo = new OasInfo();
        oasInfo.setContactName("Jane Doe");
        oasInfo.setContactEmail("jane.doe@example.com");
        oasInfo.setDescription("Generated for JUnit test.");
        oasInfo.setTitle("Title coming from JUnit test.");
        inputParameter.setOasInfo(oasInfo);

        String outputOas3 = jo.createCrudFromYamlToString(inputParameter);
        String[] outArr = outputOas3.split("\n");
        final List<String> allLines = Arrays.asList(outArr).stream().map(String::trim).collect(Collectors.toList());
        final List<String> checkList = new ArrayList<>();
        checkList.add("description: Generated for JUnit test.");
        checkList.add("email: jane.doe@example.com");
        checkList.add("name: Jane Doe");
        checkList.add("title: Title coming from JUnit test.");
        boolean ok = false;
        try (Stream<String> lines = checkList.stream()) {
            ok = lines.allMatch(l -> allLines.contains(l));
        }
        if (ok == false) Util.writeStringToData(output, outputOas3, outputCustomInfo);
        assertTrue(ok);
    }

    @Test
    @Order(23)
    void testExcelFromInputStream() throws Exception {
        InputParameter inputParameter = new InputParameter();
        inputParameter.addMethods("get");
        FileInputStream input = new FileInputStream("src/test/resources/objectimport.xlsx");
        inputParameter.setInputStream(input);

        String fromExcelInputstreamToString = jo.createFromExcelInputstreamToString(inputParameter);

        String[] outArr = fromExcelInputstreamToString.split("\n");
        final List<String> allLines = Arrays.asList(outArr).stream().map(String::trim).collect(Collectors.toList());
        final List<String> checkList = new ArrayList<>();
        checkList.add("description: OpenAPI specification generated by Joaswizard (https://github.com/networkinss/joaswizard).");
        checkList.add("email: john.doe@example.com");
        checkList.add("operationId: findInvoiceByid");
        checkList.add("operationId: getInvoiceList");
        boolean ok = false;
        try (Stream<String> lines = checkList.stream()) {
            ok = lines.allMatch(l -> allLines.contains(l));
        }
        if (ok == false) Util.writeStringToData(output, fromExcelInputstreamToString, outputExcelInputStream);
        assertTrue(ok);
    }

    @Test
    @Order(24)
    void testContactJson() throws Exception {
        Main.createOpenApiFromYamlfile(new String[]{"src/test/resources/ContactJSON.json", outputContactJSON, "contact"});
        final List<String> list = new ArrayList<>();
        list.add("title: Contact API");
        list.add("$ref: '#/components/schemas/Contact'");
        list.add("name: John Doe");
        try (Stream<String> lines = Files.lines(Paths.get(outputContactJSON))) {
            Stream<String> f = lines.filter(l -> list.contains(l.trim()));
            long x = f.count();
            assertEquals(6.0, x);
        }

        File file1 = new File(outputContactJSON);
        if (cleanUp) {
            file1.delete();
            assertTrue(file1.isFile() == false);
        }
    }

//    @Test
//    @Order(25)
//    void testJson() {
//        Main.main(new String[]{"src/test/resources/sampleJSON.json", outputMainJSON, "shoe", "name", "jsonfile", "delete,post,patch"});
//        File file1 = new File(outputMainJSON);
//        assertTrue(file1.isFile());
//
//        if (cleanUp) {
//            file1.delete();
//            assertTrue(file1.isFile() == false);
//        }
//    }
}