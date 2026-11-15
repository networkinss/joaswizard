package ch.inss.openapi.joaswizard;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The library must not configure logging, that is up to the application (or the CLI).
 */
class LoggingTest {

    @Test
    void testLibraryDoesNotConfigureLoggers() {
        new Joaswizard();
        new InputParameter();
        new ExcelWrapper();
        Util.fileExists("does-not-exist");
        for (Class<?> c : new Class<?>[]{Joaswizard.class, InputParameter.class, ExcelWrapper.class, Util.class}) {
            Logger l = Logger.getLogger(c.getName());
            assertEquals(0, l.getHandlers().length, c.getSimpleName() + " handlers");
            assertTrue(l.getUseParentHandlers(), c.getSimpleName() + " useParentHandlers");
            assertNull(l.getLevel(), c.getSimpleName() + " level");
        }
    }

    @Test
    void testGenerationEmitsNoInfoRecords() {
        Logger l = Logger.getLogger(Joaswizard.class.getName());
        List<LogRecord> records = new ArrayList<>();
        Handler h = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        h.setLevel(Level.ALL);
        l.addHandler(h);
        try {
            InputParameter in = new InputParameter();
            in.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
            in.setSampleYamlData("pet:\n  name: Rex\n  age: 3\n");
            in.setResource("pet");
            in.setResourceId("name");
            in.setCrud();
            String out = new Joaswizard().createFromYamlToString(in);
            assertNotNull(out);
        } finally {
            l.removeHandler(h);
        }
        for (LogRecord r : records) {
            assertNotEquals(Level.INFO, r.getLevel(), "Unexpected INFO record: " + r.getMessage());
        }
    }
}
