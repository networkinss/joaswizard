package ch.inss.openapi.joaswizard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void testWriteStringToDataUtf8(@TempDir Path dir) throws Exception {
        String data = "Grüße €";
        assertTrue(Util.writeStringToData(dir.toString(), data, "sub/out.txt"));
        assertEquals(data, new String(Files.readAllBytes(dir.resolve("sub/out.txt")), StandardCharsets.UTF_8));
    }

    @Test
    void testWriteStringToDataFailure(@TempDir Path dir) throws Exception {
        Path blocker = dir.resolve("blocker");
        Files.write(blocker, new byte[]{1});
        // parent "folder" is a regular file, so writing must fail and return false
        assertFalse(Util.writeStringToData(blocker.toString(), "x", "out.txt"));
        // target is an existing directory
        Files.createDirectories(dir.resolve("isdir"));
        assertFalse(Util.writeStringToData(dir.toString(), "x", "isdir"));
    }

    @Test
    void testWriteStringToDataNoParent() {
        // empty folder gives a path without a usable parent segment; must not throw NPE
        assertDoesNotThrow(() -> Util.writeStringToData("", "x", ""));
    }

    @Test
    void testIsNumberValid() {
        for (String s : new String[]{"-1", "3.14", "-0.5", "42", " 7 ", "0"}) {
            assertTrue(Util.isNumber(s), s);
        }
    }

    @Test
    void testIsNumberInvalid() {
        for (String s : new String[]{null, "", "  ", "1.2.3", "...", ".", "-", "1e5", "abc", "1.", ".5", "--1"}) {
            assertFalse(Util.isNumber(s), String.valueOf(s));
        }
    }

    @Test
    void testReadYamlRejectsGlobalTag() {
        assertThrows(Exception.class, () -> Util.readYamlFromString("a: !!java.io.File \"x\""));
        assertThrows(Exception.class, () -> Util.readYamlFromString("!!javax.script.ScriptEngineManager [!!java.net.URL [\"http://localhost/x\"]]"));
    }

    @Test
    void testReadYamlKeepsInsertionOrder() {
        java.util.LinkedHashMap<String, Object> m = Util.readYamlFromString("z: 1\na: 2\nm:\n  k: v\n");
        assertEquals(java.util.Arrays.asList("z", "a", "m"), new java.util.ArrayList<>(m.keySet()));
        assertEquals(1, m.get("z"));
    }
}
