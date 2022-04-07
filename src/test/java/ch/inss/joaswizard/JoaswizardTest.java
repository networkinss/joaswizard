package ch.inss.joaswizard;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JoaswizardTest {

    private final String output = "output/";
    private final String outputPet = "testOutputPet.yml";
    private final String outputContact = "testOutputContact.yml";
    private final String outputString = "testOutputString.yml";
    private final String referenceContact = "src/test/resources/testReferenceContact.yml";
    private final String referencePet = "src/test/resources/testReferencePet.yml";
    private final String referenceString = "src/test/resources/testReferenceString.yml";
    
    private static Joaswizard jo = null;
    
    @BeforeAll
    private static void  beforeAll() throws Exception{
        jo = new Joaswizard();        
    }

    
    @Test
    @Order(1)
    void testContact() throws Exception {
        Joaswizard.main(new String[]{"src/test/resources/Contact.yml", outputContact, "contact"});
        final List<String> list = new ArrayList<>();
        list.add("title: Contact API");
        list.add("$ref: '#/components/schemas/Contact'");
        try (Stream<String> lines = Files.lines(Paths.get(output + outputContact))) {
            assertTrue(lines.anyMatch(l -> list.contains(l.trim())));
        }
        File file1 = new File(output + outputContact);
        file1.delete();
        assertTrue(file1.isFile() == false);
    }

    @Test
    @Order(2)
    void testFullContact() throws Exception {
        Joaswizard.main(new String[]{"src/test/resources/Contact.yml", outputContact, "contact"});
        File file1 = new File(output + outputContact);
        File file2 = new File(referenceContact);
        assertTrue(file1.isFile());
        assertTrue(file2.isFile());
        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());

        file1.delete();
        assertTrue(file1.isFile() == false);
    }

    @Test
    @Order(3)
    void testPet() throws Exception {
        Joaswizard.main(new String[]{"src/test/resources/Pet.yml", outputPet, "pet", "name", "file"});

        final List<String> list = new ArrayList<>();
        list.add("title: Pet API");
        list.add("$ref: '#/components/schemas/Pet'");
        try (Stream<String> lines = Files.lines(Paths.get(output + outputPet))) {
            assertTrue(lines.anyMatch(l -> list.contains(l.trim())));
        }
    }

    @Test
    @Order(4)
    void testSchemaPet() throws Exception {
//        String fileName = ;
        Parameter parameter = new Parameter();
        parameter.setResourceId("name");
        parameter.setResource("pet");
        
        Parameter p1 = new Parameter();
//        p1.setResource("PetObject");
        p1.setResourceId("name");
        p1.setSampleYaml(Util.readFromFile("src/test/resources/PetObject.yml"));
        String schema1 = jo.createSchema(p1);
        Util.writeStringToData("output",schema1,"testOutputSchema_Object.yml");
        
        parameter.setSampleYaml(Util.readFromFile("src/test/resources/Pet.yml"));
        String schema = jo.createSchema(parameter);
        Util.writeStringToData("output",schema,"testOutputSchema.yml");

        File file1 = new File(output + "testOutputSchema.yml");
        File file2 = new File("src/test/resources/testReferenceSchemaPet.yml");
        File file3 = new File(output + "testOutputSchema_Object.yml");
        assertTrue(file1.isFile());
        assertTrue(file2.isFile());
        assertTrue(file3.isFile());
        
        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file3, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        file1.delete();
        assertTrue(file1.isFile() == false);
    }

    @Test
    @Order(5)
    void testFullPet() throws Exception {
        File file1 = new File(output + outputPet);
        File file2 = new File(referencePet);
        assertTrue(file1.isFile());
        assertTrue(file2.isFile());
        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        file1.delete();
        assertTrue(file1.isFile() == false);
    }

    @Test
    @Order(6)
    void testCreateFromString() throws Exception {
        String debug = "Zmlyc3RuYW1lOiBNYXgKbmFtZTogTXVzdGVybWFubgpwaG9uZTogMTIzNDU2Nzg5CmVtYWlsOiAibWF4QGV4YW1wbGUuY29tIg==";
        Parameter parameter = new Parameter();
        parameter.setSourceType("string");
        parameter.setSampleYamlBase64(debug);
        parameter.setOutputFile(outputString);
        parameter.setResourceId("name");
        parameter.setResource("contact");
        
        jo.createCrudFile(parameter);

        File file1 = new File(output + outputString);
        File file2 = new File(referenceString);
        assertTrue(file1.isFile());
        assertTrue(file2.isFile());

        final List<String> list = new ArrayList<>();
        list.add("title: Contact API");
        list.add("$ref: '#/components/schemas/Contact'");
        try (Stream<String> lines = Files.lines(Paths.get(output + outputString))) {
            assertTrue(lines.anyMatch(l -> list.contains(l.trim())));
        }
        
        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        file1.delete();
        assertTrue(file1.isFile() == false);
    }

    @Test
    @Order(7)
    void testExcel() throws Exception {
//        String fileName = ;
        Parameter parameter = new Parameter();
        parameter.setResourceId("name");
        parameter.setResource("pet");

        Parameter p1 = new Parameter();
//        p1.setResource("PetObject");
        p1.setResourceId("name");
        ExcelWrapper excelWrapper = new ExcelWrapper();
        HashMap<String, List<Map<String, String>>> integerListHashMap = excelWrapper.readExcel("src/test/resources/objectimport.xlsx", null, null);
        Assertions.assertEquals(3,integerListHashMap.keySet().size());
        Assertions.assertEquals(6,integerListHashMap.keySet().iterator().next().length());
        Util.getYamlAsString(integerListHashMap);
        
        
//        String schema1 = jo.createSchema(p1);
//        Util.writeStringToData("output",schema1,"testOutputSchema_Object.yml");
//
//        File file1 = new File(output + "testOutputSchema.yml");
//        File file2 = new File("src/test/resources/testReferenceSchemaPet.yml");
//        assertTrue(file1.isFile());
//        assertTrue(file2.isFile());
//
//        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
//        file1.delete();
//        assertTrue(file1.isFile() == false);
    }
}