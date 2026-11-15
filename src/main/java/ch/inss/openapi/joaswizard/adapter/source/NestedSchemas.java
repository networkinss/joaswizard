package ch.inss.openapi.joaswizard.adapter.source;

import ch.inss.openapi.joaswizard.application.TypeInferrer;
import ch.inss.openapi.joaswizard.domain.Property;
import ch.inss.openapi.joaswizard.domain.ResourceDefinition;
import ch.inss.openapi.joaswizard.domain.ResourceName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the properties of an object of a Yaml/JSON document. Map values become schemas of their own
 * (referenced with $ref), a list of maps becomes an array with items $ref.
 * <ul>
 * <li>Schema name: the key as {@link ResourceName#cap()}. A name already taken by a schema of a different shape
 * (same property keys, types and refs in the same order) gets the schema name of the parent as prefix
 * (CustomerAddress), a still taken name a number; the same shape reuses the existing schema.</li>
 * <li>A map with the only key {@code $ref} is passed through unchanged.</li>
 * <li>List of maps: items schema with the union of the keys of all elements, the first occurrence decides the type.
 * A list that also holds other values (or null) is an array of {@code object} without properties; an empty map
 * stays text ("{}").</li>
 * </ul>
 * Known limit: the grandchildren of an object that is renamed because of a collision are prefixed with the
 * unprefixed name of their parent.
 */
final class NestedSchemas {
    private static final String REF = "$ref";
    private static final String PREFIX = "#/components/schemas/";

    private final TypeInferrer inferrer;
    /** Schema name to signature of its shape, null signature: top level resource not completed yet. */
    private final Map<String, String> taken = new HashMap<>();

    NestedSchemas(TypeInferrer inferrer) {
        this.inferrer = inferrer;
    }

    /** Reserves the name of a top level resource. */
    void reserve(String schemaName) {
        taken.putIfAbsent(schemaName, null);
    }

    /** Registers the shape of a finished top level resource, so an identical nested object can reuse it. */
    void complete(String schemaName, List<Property> properties) {
        taken.put(schemaName, signature(properties));
    }

    /**
     * @param map        the object.
     * @param parentName schema name of the object, used as prefix on collisions.
     * @param out        receives the nested schemas, parents before their children (depth first).
     * @return the properties of the object.
     */
    List<Property> properties(Map<?, ?> map, String parentName, List<ResourceDefinition> out) {
        List<Property> list = new ArrayList<>();
        for (Map.Entry<?, ?> e : map.entrySet()) {
            String key = String.valueOf(e.getKey());
            Object v = e.getValue();
            if (v instanceof Map && isRef((Map<?, ?>) v)) {
                list.add(refProperty(key, "object", null, String.valueOf(((Map<?, ?>) v).get(REF)), null));
            } else if (v instanceof Map && !((Map<?, ?>) v).isEmpty()) {
                list.add(refProperty(key, "object", null, nest(key, (Map<?, ?>) v, parentName, out), null));
            } else if (v instanceof List && isMapList((List<?>) v)) {
                String items = itemsRef(key, (List<?>) v, parentName, out);
                list.add(items == null ? inferrer.infer(key, v) : refProperty(key, "array", "object", null, items));
            } else {
                list.add(inferrer.infer(key, v));
            }
        }
        return list;
    }

    private String itemsRef(String key, List<?> list, String parentName, List<ResourceDefinition> out) {
        Map<String, Object> union = new LinkedHashMap<>();
        for (Object o : list) {
            Map<?, ?> m = (Map<?, ?>) o;
            if (isRef(m)) return String.valueOf(m.get(REF));
            for (Map.Entry<?, ?> e : m.entrySet()) union.putIfAbsent(String.valueOf(e.getKey()), e.getValue());
        }
        return union.isEmpty() ? null : nest(key, union, parentName, out);
    }

    private String nest(String key, Map<?, ?> map, String parentName, List<ResourceDefinition> out) {
        String candidate = new ResourceName(key).cap();
        List<ResourceDefinition> children = new ArrayList<>();
        List<Property> props = properties(map, candidate, children);
        String sig = signature(props);
        String name = candidate;
        if (differs(name, sig)) {
            String base = parentName + candidate;
            name = base;
            for (int i = 2; differs(name, sig); i++) name = base + i;
        }
        if (!taken.containsKey(name)) {
            taken.put(name, sig);
            out.add(ResourceDefinition.nestedSchema(name, props));
        }
        out.addAll(children);
        return PREFIX + name;
    }

    private boolean differs(String name, String sig) {
        return taken.containsKey(name) && !sig.equals(taken.get(name));
    }

    private static boolean isRef(Map<?, ?> m) {
        return m.size() == 1 && m.get(REF) instanceof String;
    }

    /** Non empty list with only maps (no null, no scalars). */
    private static boolean isMapList(List<?> list) {
        if (list.isEmpty()) return false;
        for (Object o : list) if (!(o instanceof Map)) return false;
        return true;
    }

    private static Property refProperty(String key, String type, String typeArray, String ref, String itemsRef) {
        boolean array = itemsRef != null;
        return new Property(key, type, typeArray, array ? Boolean.TRUE : null, null, null, null, null, null, null,
                true, null, ref, itemsRef);
    }

    private static String signature(List<Property> properties) {
        StringBuilder s = new StringBuilder();
        for (Property p : properties) {
            s.append(p.key()).append('|').append(p.type()).append('|').append(p.typeArray()).append('|')
                    .append(p.ref()).append('|').append(p.itemsRef()).append(';');
        }
        return s.toString();
    }
}
