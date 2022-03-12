package ch.inss.joaswizard;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JoaswizardTest {

    private final String outputPet = "output/testOutputPet.yml";
    private final String outputContact = "output/testOutputContact.yml";
    private final String referenceContact = "src/test/resources/testReferenceContact.yml";

    @Test
    void testCreateContact() throws Exception {
        Joaswizard.main(new String[]{"src/test/resources/Contact.yml", "testOutputContact.yml", "contact", "name", "file"});
        File file1 = new File(outputContact);
        File file2 = new File(referenceContact);
        assertTrue(file1.isFile());
        assertTrue(file1.isFile());
        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());

        final List<String> list = new ArrayList<>();
        list.add("title: Contact API");
        list.add("$ref: '#/components/schemas/Contact'");
        try (Stream<String> lines = Files.lines(Paths.get("output/testOutputContact.yml"))) {
            assertTrue(lines.anyMatch(l -> list.contains(l.trim())));
        }
        file1.delete();
    }

    @Test
    void testCreatePet() throws Exception {
        Joaswizard.main(new String[]{"src/test/resources/Pet.yml", "testOutputPet.yml", "pet", "name", "file"});
        File file1 = new File(outputPet);
        File file2 = new File("src/test/resources/testReferencePet.yml");
        assertTrue(file1.isFile());
        assertTrue(file1.isFile());
        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());

        final List<String> list = new ArrayList<>();
        list.add("title: Pet API");
        list.add("$ref: '#/components/schemas/Pet'");
        try (Stream<String> lines = Files.lines(Paths.get("output/testOutputPet.yml"))) {
            assertTrue(lines.anyMatch(l -> list.contains(l.trim())));
        }
        file1.delete();
    }

    @Test
    void testCreateFromString() throws Exception {
        String debug = "Zmlyc3RuYW1lOiBNYXgKbmFtZTogTXVzdGVybWFubgpwaG9uZTogMTIzNDU2Nzg5CmVtYWlsOiAibWF4QGV4YW1wbGUuY29tIg==";
        Parameter parameter = new Parameter();
        parameter.setSourceType("string");
        parameter.setSampleYamlBase64(debug);
        parameter.setOutputFile("testOutputContact.yml");
        parameter.setResourceId("name");
        parameter.setResource("contact");
        Joaswizard jo = new Joaswizard();
        jo.createCrudFile(parameter);

        File file1 = new File(outputContact);
        File file2 = new File(referenceContact);
        assertTrue(file1.isFile());
        assertTrue(file1.isFile());
        Assertions.assertEquals(FileUtils.readFileToString(file1, "utf-8"), FileUtils.readFileToString(file2, "utf-8"), "There is a breaking change, outputfile is not equal to " + file2.getCanonicalPath());
        file1.delete();
    }
}