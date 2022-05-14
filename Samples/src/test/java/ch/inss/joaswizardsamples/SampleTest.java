package ch.inss.joaswizardsamples;

import ch.inss.joaswizard.InputParameter;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SampleTest extends TestCase {

    final static String sep = System.getProperty("file.separator");

    public void testCreateOpenApiFromYaml()throws Exception {
        Sample sample = new Sample();
        sample.createOpenApiFromYaml();
        String output = "output" + sep + "openapi_fromyaml.yaml";

        File file1 = new File(output);
        assertTrue(file1.isFile());

        final List<String> list = new ArrayList<>();
        list.add("operationId: getPets");
        list.add("description: deletes a single pet based on the id");
        list.add("example: Underdog");
        try (Stream<String> lines = Files.lines(Paths.get(output))) {
            Stream<String> f = lines.filter(l -> list.contains(l.trim()));
            long x = f.count();
            assertEquals(3, x);
        }

        file1.delete();
        assertTrue(file1.isFile() == false);
    }

    @Test
    public void testCreateOpenApiFromExcel() throws Exception {
        Sample sample = new Sample();
        sample.createOpenApiFromExcel();
        String output = "output" + sep + "openapi_fromexcel.yaml";

        File file1 = new File(output);
        assertTrue(file1.isFile());

        final List<String> list = new ArrayList<>();
        list.add("operationId: getAllInvoices");
        list.add("description: Returns all Customers");
        list.add("description: Database type SMALLINT");
        try (Stream<String> lines = Files.lines(Paths.get(output))) {
            Stream<String> f = lines.filter(l -> list.contains(l.trim()));
            long x = f.count();
            assertEquals(3, x);
        }

        file1.delete();
        assertTrue(file1.isFile() == false);
    }

    public void testCreateOpenApiFromArguments() throws Exception{
        Sample sample = new Sample();
        String output = "output" + sep + "openapi_fromparameters.yaml";
        sample.createOpenApiFromArguments(new String[]{"./pet.yml", output, "pet", "name","yamlfile","delete,post,patch"});

        File file1 = new File(output);
        assertTrue(file1.isFile());

        final List<String> list = new ArrayList<>();
        list.add("operationId: getPets");
        list.add("description: deletes a single pet based on the name");
        list.add("example: Underdog");
        try (Stream<String> lines = Files.lines(Paths.get(output))) {
            Stream<String> f = lines.filter(l -> list.contains(l.trim()));
            long x = f.count();
            assertEquals(3, x);
        }

        file1.delete();
        assertTrue(file1.isFile() == false);
    }
}