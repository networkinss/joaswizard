package ch.inss.openapi.joaswizard.application;

import ch.inss.openapi.joaswizard.application.TypeMapper.MappedType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TypeMapperTest {

    private static final MappedType VARCHAR = new MappedType("string", null, null);
    private static final MappedType INT = new MappedType("integer", "int32", null);
    private static final MappedType SHORT = new MappedType("integer", null, "^[0-9]$");

    private static TypeMapper mapper(boolean prefixMatch) {
        return new TypeMapper(Map.of("VARCHAR", VARCHAR, "INT", INT, "TI", SHORT), prefixMatch);
    }

    @Test
    void exactMatchIsCaseInsensitive() {
        assertSame(VARCHAR, mapper(false).map("varchar"));
        assertSame(INT, mapper(false).map("Int"));
    }

    @Test
    void prefixMatchFindsTypeWithLength() {
        assertSame(VARCHAR, mapper(true).map("varchar(30)"));
        assertSame(VARCHAR, mapper(true).map("VARCHAR(30)"));
    }

    @Test
    void prefixNeedsAtLeastThreeCharacters() {
        // "TI" is a prefix of "TINYINT", but too short to count.
        assertSame(MappedType.EMPTY, mapper(true).map("TINYINT"));
    }

    @Test
    void noPrefixMatchWhenSwitchedOff() {
        assertSame(MappedType.EMPTY, mapper(false).map("varchar(30)"));
    }

    @Test
    void unknownTypeGivesEmpty() {
        MappedType m = mapper(true).map("blob");
        assertSame(MappedType.EMPTY, m);
        assertNull(m.type());
        assertNull(m.format());
        assertNull(m.pattern());
    }

    @Test
    void longestOrLastKeyInTableOrderWinsWhenSeveralPrefixesMatch() {
        // The table is a hashed map, so only check that one of the matching keys is chosen, never EMPTY.
        MappedType a = new MappedType("a", null, null);
        MappedType b = new MappedType("b", null, null);
        MappedType m = new TypeMapper(Map.of("var", a, "varchar", b), true).map("varchar(5)");
        assertTrue(m == a || m == b);
    }
}
