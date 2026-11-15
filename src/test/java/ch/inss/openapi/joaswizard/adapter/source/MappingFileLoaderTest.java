package ch.inss.openapi.joaswizard.adapter.source;

import ch.inss.openapi.joaswizard.application.TypeMapper.MappedType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MappingFileLoaderTest {

    @Test
    void defaultMappingEntriesAreLoaded() {
        MappedType timestamp = MappingFileLoader.load("src/main/resources/mapping.json").get("TIMESTMP");
        assertEquals("string", timestamp.type());
        assertEquals("date-time", timestamp.format());
        assertTrue(timestamp.pattern() == null || timestamp.pattern().isEmpty());
    }

    @Test
    void keysAreCaseInsensitive() {
        assertNotNull(MappingFileLoader.load("src/main/resources/mapping.json").get("timestmp"));
    }

    @Test
    void missingFileGivesNull() {
        assertNull(MappingFileLoader.load("src/test/resources/doesNotExist.json"));
    }
}
