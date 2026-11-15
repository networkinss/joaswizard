package ch.inss.openapi.joaswizard.adapter.render;

import ch.inss.openapi.joaswizard.application.Numbers;
import com.github.jknack.handlebars.Handlebars;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.util.Map;

/**
 * Renders strings as YAML scalars that parse back to the same text and cannot inject YAML structure.
 * Also creates the Handlebars instance with the helper <code>yaml</code>.
 */
public final class YamlScalar {

    private YamlScalar() {
    }

    /**
     * Handlebars instance with the helper <code>{{yaml value}}</code> registered.
     * Hash parameter <code>string=true</code> forces number-like values to be quoted.
     */
    public static Handlebars newHandlebars() {
        Handlebars handlebars = new Handlebars();
        handlebars.registerHelper("yaml", (Object context, com.github.jknack.handlebars.Options options) -> {
            if (context == null) return "";
            if (context instanceof java.util.Collection) {
                StringBuilder b = new StringBuilder("[");
                for (Object item : (java.util.Collection<?>) context) {
                    if (b.length() > 1) b.append(", ");
                    b.append(quoteFlowItem(String.valueOf(item)));
                }
                return new Handlebars.SafeString(b.append(']').toString());
            }
            String text = String.valueOf(context);
            boolean string = Boolean.TRUE.equals(options.hash("string"));
            return new Handlebars.SafeString(string ? quoteAsString(text) : quoteIfNeeded(text));
        });
        return handlebars;
    }

    /**
     * Like {@link #quoteIfNeeded(String)}, but number-like values are single quoted (for string typed values).
     */
    public static String quoteAsString(String s) {
        if (s != null && Numbers.isNumber(s)) return singleQuote(s.trim());
        return quoteIfNeeded(s);
    }

    /**
     * Returns the plain text when it parses back as the same value, otherwise a quoted scalar.
     * Numbers and booleans stay plain (typing behaviour is kept); null returns null.
     */
    public static String quoteIfNeeded(String s) {
        if (s == null) return null;
        if (needsDoubleQuotes(s)) return doubleQuote(s);
        if (isPlainSafe(s, false)) return s;
        return singleQuote(s);
    }

    /**
     * Quotes one item of a flow sequence: additionally quotes values containing flow indicators.
     */
    public static String quoteFlowItem(String s) {
        if (needsDoubleQuotes(s)) return doubleQuote(s);
        if (isPlainSafe(s, true)) return s;
        return singleQuote(s);
    }

    private static boolean needsDoubleQuotes(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 0x20 || c == 0x7f || c == 0x85 || c == 0x2028 || c == 0x2029 || c == 0xFEFF) return true;
        }
        return false;
    }

    private static boolean isPlainSafe(String s, boolean flow) {
        if (s.isEmpty() || !s.equals(s.trim())) return false;
        Object parsed;
        try {
            Object doc = new Yaml(new SafeConstructor(new LoaderOptions())).load(flow ? "k: [" + s + "]" : "k: " + s);
            if (!(doc instanceof Map)) return false;
            Map<?, ?> map = (Map<?, ?>) doc;
            if (map.size() != 1 || !"k".equals(map.keySet().iterator().next())) return false;
            parsed = map.get("k");
            if (flow) {
                if (!(parsed instanceof java.util.List) || ((java.util.List<?>) parsed).size() != 1) return false;
                parsed = ((java.util.List<?>) parsed).get(0);
            }
        } catch (RuntimeException e) {
            return false;
        }
        if (parsed instanceof String) return parsed.equals(s);
        if (parsed instanceof Number || parsed instanceof Boolean) return String.valueOf(parsed).equals(s);
        return false;
    }

    private static String singleQuote(String s) {
        return "'" + s.replace("'", "''") + "'";
    }

    private static String doubleQuote(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break;
                case '\r': b.append("\\r"); break;
                case '\t': b.append("\\t"); break;
                default:
                    if (c < 0x20 || c == 0x7f || c == 0x85 || c == 0x2028 || c == 0x2029 || c == 0xFEFF) {
                        b.append(String.format("\\u%04X", (int) c));
                    } else {
                        b.append(c);
                    }
            }
        }
        return b.append('"').toString();
    }
}
