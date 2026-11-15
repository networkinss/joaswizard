package ch.inss.openapi.joaswizard;

import ch.inss.openapi.joaswizard.adapter.render.YamlScalar;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlScalarTest {

    private static final List<String> TRICKY = Arrays.asList(
            "Pet", "plain words here", "a: b", "a #b", "a#b", "- x", "[x]", "{x}", "*x", "&x", "!x", "|x", ">x", "'x", "\"x",
            "%x", "@x", "`x", " lead", "trail ", "multi\nline", "tab\tx", "back\\slash", "O'Neil", "say \"hi\"", "",
            "true", "yes", "no", "null", "~", "5", "-5", "3.14", "010", "0x1F", "1_000", ".inf", "^[a-z]+$", "[a-z]{3}",
            "http://x.y/z?a=b#c", "Zürich €", "ends:", "a, b", "2020-01-01", "x\r\ny", "nul\u0000l", "- ", "? x", "key: value # c");

    private static Object parseValue(String scalar) {
        Object doc = new Yaml(new SafeConstructor(new LoaderOptions())).load("k: " + scalar);
        assertTrue(doc instanceof Map, scalar);
        Map<?, ?> map = (Map<?, ?>) doc;
        assertEquals(1, map.size(), scalar);
        assertEquals("k", map.keySet().iterator().next());
        return map.get("k");
    }

    @Test
    void roundTrip() {
        for (String s : TRICKY) {
            String quoted = YamlScalar.quoteIfNeeded(s);
            Object parsed = parseValue(quoted);
            assertNotNull(parsed, "null for [" + s + "] -> " + quoted);
            assertEquals(s, String.valueOf(parsed), "[" + s + "] -> " + quoted);
            if (!(parsed instanceof String)) {
                assertTrue(parsed instanceof Number || parsed instanceof Boolean, quoted);
                assertEquals(quoted, s, "only unquoted number/boolean may change type");
            }
        }
    }

    @Test
    void stringTypedWhenQuoted() {
        for (String s : Arrays.asList("010", "0x1F", "1_000", ".inf", "~", "null", "yes", "2020-01-01", "")) {
            assertEquals(s, parseValue(YamlScalar.quoteIfNeeded(s)), s);
        }
    }

    @Test
    void plainStaysPlain() {
        for (String s : Arrays.asList("Pet", "plain words here", "5", "-5", "3.14", "true", "^[a-z]+$", "Zürich €",
                "http://x.y/z?a=b#c", "a:b", "O'Neil", "say \"hi\"")) {
            assertEquals(s, YamlScalar.quoteIfNeeded(s), s);
        }
    }

    @Test
    void quotingStyle() {
        assertEquals("'a: b'", YamlScalar.quoteIfNeeded("a: b"));
        assertEquals("'it''s: x'", YamlScalar.quoteIfNeeded("it's: x"));
        assertEquals("\"multi\\nline\"", YamlScalar.quoteIfNeeded("multi\nline"));
        assertEquals("\"tab\\tx\"", YamlScalar.quoteIfNeeded("tab\tx"));
        assertEquals("''", YamlScalar.quoteIfNeeded(""));
        assertNull(YamlScalar.quoteIfNeeded(null));
        assertEquals("'5'", YamlScalar.quoteAsString("5"));
        assertEquals("'-5.5'", YamlScalar.quoteAsString("-5.5"));
        assertEquals("abc", YamlScalar.quoteAsString("abc"));
    }

    @Test
    void flowItems() {
        for (String s : Arrays.asList("dog", "a, b", "x]", "{y}", "a: b", "5", "it's")) {
            Object doc = new Yaml(new SafeConstructor(new LoaderOptions())).load("k: [" + YamlScalar.quoteFlowItem(s) + "]");
            List<?> list = (List<?>) ((Map<?, ?>) doc).get("k");
            assertEquals(1, list.size(), s);
            assertEquals(s, String.valueOf(list.get(0)), s);
        }
    }
}
