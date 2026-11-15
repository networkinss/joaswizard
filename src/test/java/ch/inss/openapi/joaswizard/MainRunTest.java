package ch.inss.openapi.joaswizard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MainRunTest {

    private static final String OUT = "target/mainruntest-pet.yaml";

    @Test
    void runWithArgsCreatesFile() {
        new File(OUT).delete();
        int rc = Main.run(new String[]{"src/test/resources/Pet.yml", OUT, "pet", "name", "yamlfile", "crud"},
                new ByteArrayInputStream(new byte[0]));
        assertEquals(0, rc);
        assertTrue(new File(OUT).isFile());
    }

    @Test
    void runWithPromptedAnswers() {
        String out = "target/mainruntest-prompted.yaml";
        new File(out).delete();
        String in = "src/test/resources/Pet.yml\n" + out + "\npet\nname\n\n\n";
        int rc = Main.run(new String[0], new ByteArrayInputStream(in.getBytes(StandardCharsets.UTF_8)));
        assertEquals(0, rc);
        assertTrue(new File(out).isFile());
    }

    @Test
    void runMissingFileReturnsOne() {
        int rc = Main.run(new String[]{"does/not/exist.yml", "target/x.yaml", "pet", "id", "yamlfile", "crud"},
                new ByteArrayInputStream(new byte[0]));
        assertEquals(1, rc);
    }

    @Test
    void runHelpReturnsZero() {
        assertEquals(0, Main.run(new String[]{"help"}, new ByteArrayInputStream(new byte[0])));
    }

    @Test
    void tooFewArgsReturnsWithoutExit() {
        assertDoesNotThrow(() -> Main.createOpenApiFromYamlfile(new String[]{"a", "b"}));
        assertDoesNotThrow(() -> Main.createOpenApiFromJSONfile(new String[0]));
    }

    private static final String OUT_DIR = "target/test-output/";

    private static long countLines(String file, List<String> wanted) throws Exception {
        return Files.readAllLines(Paths.get(file)).stream().filter(l -> wanted.contains(l.trim())).count();
    }

    @Test
    void contactYamlFileHasExpectedLines() throws Exception {
        String out = OUT_DIR + "mainContact.yml";
        Main.createOpenApiFromYamlfile(new String[]{"src/test/resources/Contact.yml", out, "contact"});
        assertEquals(6, countLines(out, List.of("title: Contact API", "$ref: '#/components/schemas/Contact'", "name: John Doe")));
    }

    @Test
    void contactJsonFileHasExpectedLines() throws Exception {
        String out = OUT_DIR + "mainContactJson.yml";
        Main.createOpenApiFromYamlfile(new String[]{"src/test/resources/ContactJSON.json", out, "contact"});
        assertEquals(6, countLines(out, List.of("title: Contact API", "$ref: '#/components/schemas/Contact'", "name: John Doe")));
    }

    @Test
    void petYamlFileHasExpectedLines() throws Exception {
        String out = OUT_DIR + "mainPet.yml";
        Main.createOpenApiFromYamlfile(new String[]{"src/test/resources/Pet.yml", out, "pet", "name", "yamlfile"});
        assertEquals(5, countLines(out, List.of("title: Pet API", "$ref: '#/components/schemas/Pet'")));
    }

    @Test
    void mainWithMultipleYamlObjectsCreatesFile() {
        String out = OUT_DIR + "mainYamlObjects.yaml";
        Main.main(new String[]{"src/test/resources/YamlObjects.yml", out, "pet", "name", "yamlfile", "delete,post,patch"});
        assertTrue(new File(out).isFile());
    }

    @Test
    void mainWithSampleYamlCreatesFile() {
        String out = OUT_DIR + "mainSample.yaml";
        Main.main(new String[]{"src/test/resources/sample.yaml", out, "shoe", "name", "yamlfile", "delete,post,patch"});
        assertTrue(new File(out).isFile());
    }
}
