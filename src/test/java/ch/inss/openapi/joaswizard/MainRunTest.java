package ch.inss.openapi.joaswizard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
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
}
