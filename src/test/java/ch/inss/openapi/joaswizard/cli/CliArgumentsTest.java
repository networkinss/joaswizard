package ch.inss.openapi.joaswizard.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

class CliArgumentsTest {

    private static Prompter answers(String text) {
        return new Prompter(new Scanner(new StringReader(text)));
    }

    @Test
    void fullArgs() {
        CliArguments c = CliArguments.resolve(
                new String[]{"in.yml", "out.yaml", "pet", "name", "excel", "delete,post"}, answers(""));
        assertEquals(new CliArguments("in.yml", "./out.yaml", "pet", "name", "excel", "delete,post"), c);
    }

    @Test
    void missingArgsAnsweredWithDefaults() {
        CliArguments c = CliArguments.resolve(new String[0], answers("in.yml\n\npet\nid\n\n\n"));
        assertEquals(new CliArguments("in.yml", "./openapi.yaml", "pet", "id", "yamlfile", "crud"), c);
    }

    @Test
    void invalidSourcetypeArgThenRetry() {
        CliArguments c = CliArguments.resolve(
                new String[]{"in.yml", "o.yaml", "pet", "id", "bogus", "crud"}, answers("bogus2\njsonfile\n"));
        assertEquals("jsonfile", c.sourcetype());
    }

    @Test
    void invalidSourcetypeArgThenEmptyRetryGivesDefault() {
        CliArguments c = CliArguments.resolve(
                new String[]{"in.yml", "o.yaml", "pet", "id", "bogus", "crud"}, answers("\n"));
        assertEquals("yamlfile", c.sourcetype());
    }

    @Test
    void invalidMethodsArgThenRetry() {
        CliArguments c = CliArguments.resolve(
                new String[]{"in.yml", "o.yaml", "pet", "id", "yamlfile", "nonsense"}, answers("nope\nget,post\n"));
        assertEquals("get,post", c.methods());
    }

    @Test
    void emptyMethodsArgIsCrud() {
        CliArguments c = CliArguments.resolve(
                new String[]{"in.yml", "o.yaml", "pet", "id", "yamlfile", ""}, answers(""));
        assertEquals("crud", c.methods());
    }
}
