package ch.inss.openapi.joaswizard.application;

import ch.inss.openapi.joaswizard.GenerationResult;
import ch.inss.openapi.joaswizard.InputParameter;
import ch.inss.openapi.joaswizard.Joaswizard;
import ch.inss.openapi.joaswizard.adapter.source.YamlSource;
import ch.inss.openapi.joaswizard.domain.Property;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Type inference from parsed Yaml values.
 */
class TypeInferrerTest {

    private final TypeInferrer inferrer = new TypeInferrer();

    /** Parses "v: <yaml>" like the Yaml source does and infers the property. */
    private Property of(String yamlValue) {
        Map<String, Object> m = YamlSource.readYaml("v: " + yamlValue);
        return inferrer.infer("v", m.get("v"));
    }

    @Test
    void integer() {
        Property p = of("42");
        assertEquals("integer", p.type());
        assertEquals("42", p.example());
        assertNull(p.format());
        assertNull(p.minLength());
    }

    @Test
    void longNeedsInt64() {
        Property p = of("12345678901");
        assertEquals("integer", p.type());
        assertEquals("int64", p.format());
        assertEquals("12345678901", p.example());
        assertEquals("int64", inferrer.infer("v", BigInteger.valueOf(Long.MAX_VALUE)).format());
        assertNull(inferrer.infer("v", new BigInteger("123456789012345678901234567890")).format());
    }

    @Test
    void doubleIsNumber() {
        Property p = of("12.05");
        assertEquals("number", p.type());
        assertEquals("12.05", p.example());
        assertEquals("number", of("1.0").type());
    }

    @Test
    void booleanIsBoolean() {
        Property p = of("true");
        assertEquals("boolean", p.type());
        assertEquals("true", p.example());
        assertNull(p.minLength());
        assertEquals("false", of("false").example());
    }

    @Test
    void quotedNumberStaysString() {
        Property p = of("\"5\"");
        assertEquals("string", p.type());
        assertEquals("5", p.example());
        assertEquals(1, p.minLength());
        assertEquals("string", of("'true'").type());
    }

    @Test
    void stringMinLengthIsTrimmedLength() {
        Property p = inferrer.infer("v", "  hello ");
        assertEquals("string", p.type());
        assertEquals("hello", p.example());
        assertEquals(5, p.minLength());
    }

    @Test
    void dateOnly() {
        Property p = of("2024-03-15");
        assertEquals("string", p.type());
        assertEquals("date", p.format());
        assertEquals("2024-03-15", p.example());
    }

    @Test
    void dateTime() {
        Property p = of("2024-03-15T10:20:30Z");
        assertEquals("string", p.type());
        assertEquals("date-time", p.format());
        assertEquals("2024-03-15T10:20:30Z", p.example());
        assertEquals("2024-03-15T08:20:30Z", of("2024-03-15T10:20:30+02:00").example());
    }

    @Test
    void nullIsStringWithoutExample() {
        Property p = of("null");
        assertEquals("string", p.type());
        assertNull(p.example());
        assertNull(p.minLength());
    }

    @Test
    void emptyList() {
        Property p = of("[]");
        assertEquals("array", p.type());
        assertEquals("string", p.typeArray());
        assertNull(p.example());
        assertTrue(p.array());
    }

    @Test
    void intList() {
        Property p = of("[10, 15, 20]");
        assertEquals("array", p.type());
        assertEquals("integer", p.typeArray());
        assertEquals("[10, 15, 20]", p.example());
        assertNull(p.minLength());
    }

    @Test
    void numberAndBooleanList() {
        assertEquals("number", of("[1, 2.5]").typeArray());
        assertEquals("boolean", of("[true, false]").typeArray());
    }

    @Test
    void mixedListIsStrings() {
        Property p = of("[1, abc, true]");
        assertEquals("string", p.typeArray());
        assertEquals("[1, abc, true]", p.example());
    }

    @Test
    void stringListMinLengthIsLongestElement() {
        Property p = of("[dog, 4paws]");
        assertEquals("string", p.typeArray());
        assertEquals(5, p.minLength());
        assertEquals("[dog, 4paws]", p.example());
    }

    @Test
    void listOfMapsIsObjectPlaceholder() {
        Property p = of("[{a: 1}]");
        assertEquals("array", p.type());
        assertEquals("object", p.typeArray());
        assertNull(p.example());
    }

    @Test
    void mapKeepsTextBehaviour() {
        Property p = of("{a: 1}");
        assertEquals("string", p.type());
        assertEquals("{a=1}", p.example());
    }

    @Test
    @SuppressWarnings("unchecked")
    void renderedSpec() {
        String yaml = "item:\n"
                + "  active: true\n"
                + "  code: \"5\"\n"
                + "  flag: \"true\"\n"
                + "  born: 2024-03-15\n"
                + "  seen: 2024-03-15T10:20:30Z\n"
                + "  note:\n"
                + "  tags: ['1', b]\n"
                + "  none: []\n";
        InputParameter in = new InputParameter();
        in.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
        in.setSampleYamlData(yaml);
        in.setResource("item");
        in.setResourceId("code");
        GenerationResult r = new Joaswizard().generate(in);
        assertTrue(r.isOk(), r.toString());
        Map<String, Object> doc = new org.yaml.snakeyaml.Yaml(new org.yaml.snakeyaml.constructor.SafeConstructor(new org.yaml.snakeyaml.LoaderOptions())).load(r.getSpec());
        Map<String, Object> props = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) doc.get("components")).get("schemas")).get("Item")).get("properties");
        assertEquals("boolean", ((Map<String, Object>) props.get("active")).get("type"));
        assertEquals(Boolean.TRUE, ((Map<String, Object>) props.get("active")).get("example"));
        assertEquals("5", ((Map<String, Object>) props.get("code")).get("example"));
        assertEquals("true", ((Map<String, Object>) props.get("flag")).get("example"));
        assertEquals("date", ((Map<String, Object>) props.get("born")).get("format"));
        assertEquals("2024-03-15", ((Map<String, Object>) props.get("born")).get("example"));
        assertEquals("date-time", ((Map<String, Object>) props.get("seen")).get("format"));
        assertEquals("string", ((Map<String, Object>) props.get("note")).get("type"));
        assertFalse(((Map<String, Object>) props.get("note")).containsKey("example"));
        assertEquals(List.of("1", "b"), ((Map<String, Object>) props.get("tags")).get("example"));
        assertFalse(((Map<String, Object>) props.get("none")).containsKey("example"));
    }
}
