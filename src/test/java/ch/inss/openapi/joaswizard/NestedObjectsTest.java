package ch.inss.openapi.joaswizard;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Nested objects (Map values): own schemas with $ref. Regenerate the references with -Dnested.update=true.
 */
@SuppressWarnings("unchecked")
class NestedObjectsTest {

    private static final String DIR = "src/test/resources/";

    private String generate(String file, String resource) {
        InputParameter in = new InputParameter();
        in.setInputFile(DIR + "nested/" + file);
        in.setResource(resource);
        in.setResourceId("id");
        in.setCrud();
        GenerationResult r = new Joaswizard().generate(in);
        assertTrue(r.isOk(), r.toString());
        return r.getSpec();
    }

    private String fromString(String yaml, String resource) {
        InputParameter in = new InputParameter();
        in.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
        in.setSampleYamlData(yaml);
        in.setResource(resource);
        in.setResourceId("id");
        in.setCrud();
        GenerationResult r = new Joaswizard().generate(in);
        assertTrue(r.isOk(), r.toString());
        return r.getSpec();
    }

    private void compare(String reference, String spec) throws Exception {
        Path path = Paths.get(DIR + reference);
        if (System.getProperty("nested.update") != null) Files.writeString(path, spec, StandardCharsets.UTF_8);
        assertEquals(Files.readString(path, StandardCharsets.UTF_8), spec);
        assertRefsResolve(spec);
    }

    /** Every $ref '#/components/schemas/X' must point to an existing schema. */
    private static Map<String, Object> schemas(String spec) {
        Map<String, Object> doc = new Yaml().load(spec);
        return (Map<String, Object>) ((Map<String, Object>) doc.get("components")).get("schemas");
    }

    private static void assertRefsResolve(String spec) {
        Map<String, Object> doc = new Yaml().load(spec);
        Map<String, Object> schemas = schemas(spec);
        List<String> refs = new ArrayList<>();
        collectRefs(doc, refs);
        assertFalse(refs.isEmpty());
        for (String ref : refs) {
            String prefix = "#/components/schemas/";
            assertTrue(ref.startsWith(prefix), ref);
            assertTrue(schemas.containsKey(ref.substring(prefix.length())), "dangling " + ref);
        }
    }

    private static void collectRefs(Object o, List<String> refs) {
        if (o instanceof Map) {
            for (Map.Entry<String, Object> e : ((Map<String, Object>) o).entrySet()) {
                if ("$ref".equals(e.getKey())) refs.add((String) e.getValue());
                else collectRefs(e.getValue(), refs);
            }
        } else if (o instanceof List) {
            for (Object i : (List<Object>) o) collectRefs(i, refs);
        }
    }

    @Test
    void testNestedAddressMultiObject() throws Exception {
        String spec = generate("samplenestedaddress.yaml", "pet");
        compare("testReferenceNestedAddress.yml", spec);
        assertEquals(List.of("Pet", "Customer", "Address", "Invoice"), keys(schemas(spec), "ErrorModel"));
        assertFalse(spec.contains("/addresses"), "nested schema gets no paths");
        assertTrue(spec.contains("/customers"));
    }

    @Test
    void testExplicitRef() throws Exception {
        String spec = generate("sampleref.yaml", "pet");
        compare("testReferenceNestedRef.yml", spec);
        assertTrue(spec.contains("/addresses"), "top level Address gets paths");
        assertTrue(spec.contains("$ref: '#/components/schemas/Address'"));
    }

    @Test
    void testEnvelopeYamlAndJsonAreIdentical() throws Exception {
        String yaml = generate("samplenested.yaml", "response");
        String json = generate("samplenested.json", "response");
        assertEquals(yaml, json);
        compare("testReferenceNestedEnvelope.yml", yaml);
        Map<String, Object> schemas = schemas(yaml);
        assertEquals(List.of("Response", "Data", "Categories"), keys(schemas, "ErrorModel"));
    }

    @Test
    void testCollisionDifferentShapeGetsPrefix() {
        String spec = fromString("customer:\n  name: a\n  address:\n    street: x\n"
                + "order:\n  id: 1\n  address:\n    zip: 8000\n", "x");
        assertEquals(List.of("Customer", "Address", "Order", "OrderAddress"), keys(schemas(spec), "ErrorModel"));
        assertTrue(spec.contains("$ref: '#/components/schemas/OrderAddress'"));
        assertRefsResolve(spec);
    }

    @Test
    void testCollisionSameShapeIsReused() {
        String spec = fromString("customer:\n  name: a\n  address:\n    street: x\n"
                + "order:\n  id: 1\n  address:\n    street: y\n", "x");
        assertEquals(List.of("Customer", "Address", "Order"), keys(schemas(spec), "ErrorModel"));
        assertRefsResolve(spec);
    }

    @Test
    void testNestedNameCollidesWithTopLevelResource() {
        String spec = fromString("customer:\n  name: a\n  order:\n    id: 1\norder:\n  total: 5\n", "x");
        assertEquals(List.of("Customer", "CustomerOrder", "Order"), keys(schemas(spec), "ErrorModel"));
        assertRefsResolve(spec);
    }

    @Test
    void testDepthTwoAndOnlyTopLevelPaths() {
        String spec = fromString("name: a\nowner:\n  nick: b\n  home:\n    city: c\n", "pet");
        Map<String, Object> schemas = schemas(spec);
        assertEquals(List.of("Pet", "Owner", "Home"), keys(schemas, "ErrorModel"));
        Map<String, Object> owner = (Map<String, Object>) ((Map<String, Object>) schemas.get("Owner")).get("properties");
        assertEquals("#/components/schemas/Home", ((Map<String, Object>) owner.get("home")).get("$ref"));
        assertTrue(spec.contains("/pets"));
        assertFalse(spec.contains("/owners"));
        assertFalse(spec.contains("/homes"));
        assertTrue(spec.contains("title: Pet API"));
    }

    @Test
    void testRefPropertyHasNoSiblingsButIsRequired() {
        String spec = fromString("name: a\nowner:\n  nick: b\n", "pet");
        Map<String, Object> pet = (Map<String, Object>) schemas(spec).get("Pet");
        Map<String, Object> owner = (Map<String, Object>) ((Map<String, Object>) pet.get("properties")).get("owner");
        assertEquals(List.of("$ref"), new ArrayList<>(owner.keySet()));
        assertTrue(((List<String>) pet.get("required")).contains("owner"));
    }

    @Test
    void testMixedListStaysObjectArray() {
        String spec = fromString("name: a\nthings:\n- k: 1\n- plain\n", "pet");
        Map<String, Object> pet = (Map<String, Object>) schemas(spec).get("Pet");
        Map<String, Object> things = (Map<String, Object>) ((Map<String, Object>) pet.get("properties")).get("things");
        assertEquals("array", things.get("type"));
        assertEquals(Map.of("type", "object"), things.get("items"));
        assertEquals(List.of("Pet"), keys(schemas(spec), "ErrorModel"));
    }

    /** Schema names in order, without the given one. */
    private static List<String> keys(Map<String, Object> schemas, String skip) {
        List<String> k = new ArrayList<>(schemas.keySet());
        k.remove(skip);
        return k;
    }
}
