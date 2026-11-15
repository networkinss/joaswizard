package ch.inss.openapi.joaswizard;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputParameterTest {

    @Test
    void testCopyConstructorCopiesFlags() {
        InputParameter in = new InputParameter();
        in.setDoInfo(false);
        in.setDoPaths(false);
        in.setDoSchemas(false);
        InputParameter copy = new InputParameter(in);
        assertFalse(copy.isDoInfo());
        assertFalse(copy.isDoPaths());
        assertFalse(copy.isDoSchemas());
    }

    @Test
    void testCopyConstructorDoesNotShareMethodsAndSchemaData() {
        InputParameter in = new InputParameter();
        in.addMethods("get");
        in.getSchemaData().put("a", "b");
        InputParameter copy = new InputParameter(in);
        copy.addMethods("post");
        copy.getSchemaData().put("c", "d");
        assertEquals(1, in.getMethodList().size());
        assertTrue(in.getMethodList().contains(InputParameter.Method.GET));
        assertEquals(2, copy.getMethodList().size());
        assertEquals(1, in.getSchemaData().size());
        assertEquals(2, copy.getSchemaData().size());
    }

    @Test
    void testSetInputFileYaml() {
        InputParameter in = new InputParameter();
        in.setInputFile("dir/pet.YML");
        assertEquals("dir/pet.YML", in.getInputFile());
        assertEquals(InputParameter.Sourcetype.YAMLFILE, in.getSourceType());
    }

    @Test
    void testSetInputFileExcelAndJson() {
        InputParameter in = new InputParameter();
        in.setInputFile("a.xlsx");
        assertEquals(InputParameter.Sourcetype.EXCEL, in.getSourceType());
        in.setInputFile("a.json");
        assertEquals(InputParameter.Sourcetype.JSONFILE, in.getSourceType());
    }

    @Test
    void testSetInputFileWithoutExtensionKeepsSourceType() {
        InputParameter in = new InputParameter();
        in.setSourceType(InputParameter.Sourcetype.EXCEL);
        in.setInputFile("noextension");
        assertEquals("noextension", in.getInputFile());
        assertEquals(InputParameter.Sourcetype.EXCEL, in.getSourceType());
        in.setInputFile("data.txt");
        assertEquals("data.txt", in.getInputFile());
        assertEquals(InputParameter.Sourcetype.EXCEL, in.getSourceType());
    }

    @Test
    void testSetInputFileNull() {
        InputParameter in = new InputParameter();
        in.setInputFile(null);
        assertNull(in.getInputFile());
        assertNull(in.getSourceType());
    }

    @Test
    void testCapResourceIsNormalized() {
        InputParameter in = new InputParameter();
        in.setResource("PET");
        assertEquals("Pet", in.getCapResource());
        assertEquals("Pets", in.getCapResources());
        in.setResource("PetOwner");
        assertEquals("Petowner", in.getCapResource());
        assertEquals("Petowners", in.getCapResources());
        in.setResource("pet");
        assertEquals("Pet", in.getCapResource());
        assertEquals("Pets", in.getCapResources());
    }

    @Test
    void testCheckValidAcceptsJsonLikeYaml() {
        InputParameter file = new InputParameter();
        file.setSourceType(InputParameter.Sourcetype.JSONFILE);
        file.setInputFile("pet.json");
        file.setResource("pet");
        assertTrue(file.checkValid());
        InputParameter text = new InputParameter();
        text.setSourceType(InputParameter.Sourcetype.JSONSTRING);
        text.setSampleYamlData("{\"pet\": {\"name\": \"Rex\"}}");
        assertFalse(text.checkValid(), "resource missing");
        text.setResource("pet");
        assertTrue(text.checkValid());
    }

    @Test
    void testIsValidType() {
        InputParameter inputParameter = new InputParameter();
        assertTrue(inputParameter.isValidSourcetype("excel"));
        assertFalse(inputParameter.isValidSourcetype("nix"));
        assertTrue(InputParameter.isValidMethod("CRUD"));
        assertFalse(InputParameter.isValidMethod("nix"));
    }
}
