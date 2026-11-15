package ch.inss.openapi.joaswizard.application;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.Map;

/**
 * Maps a database type to OAS type, format and pattern using a mapping table (case-insensitive keys).
 */
public final class TypeMapper {

    /**
     * Result of a mapping.
     *
     * @param type    OAS type or null.
     * @param format  OAS format or null.
     * @param pattern OAS pattern or null.
     */
    public record MappedType(String type, String format, String pattern) {
        /** Nothing mapped. */
        public static final MappedType EMPTY = new MappedType(null, null, null);
    }

    private final Map<String, MappedType> table;
    private final boolean prefixMatch;

    /**
     * @param table       mapping table by database type; the iteration order matters for prefix matching.
     * @param prefixMatch true to also match database types by prefix (mapping key of at least 3 characters).
     */
    public TypeMapper(Map<String, MappedType> table, boolean prefixMatch) {
        this.table = table instanceof CaseInsensitiveMap ? table : new CaseInsensitiveMap<>(table);
        this.prefixMatch = prefixMatch;
    }

    /**
     * @param dbType database type, not null.
     * @return the mapping, {@link MappedType#EMPTY} if there is none. With prefix match the last matching key in
     * table order wins.
     */
    public MappedType map(String dbType) {
        MappedType mapped = table.get(dbType);
        if (mapped == null) {
            if (prefixMatch) {
                for (String mapType : table.keySet()) {
                    if (dbType.toLowerCase().startsWith(mapType.toLowerCase()) && mapType.length() >= 3) {
                        mapped = table.get(mapType);
                    }
                }
            }
        }
        return mapped == null ? MappedType.EMPTY : mapped;
    }
}
