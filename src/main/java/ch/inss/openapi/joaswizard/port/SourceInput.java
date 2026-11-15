package ch.inss.openapi.joaswizard.port;

import java.io.InputStream;

/**
 * Where the sample data come from.
 */
public sealed interface SourceInput {

    /**
     * Yaml or JSON text.
     *
     * @param text the text.
     * @param file name of the file the text was read from, null if it is a string (used in messages only).
     * @param json true if the source type is JSON (JSON is valid Yaml, the same parser is used).
     */
    record YamlText(String text, String file, boolean json) implements SourceInput {
    }

    /**
     * Excel workbook file.
     *
     * @param path file path.
     */
    record ExcelFile(String path) implements SourceInput {
    }

    /**
     * Excel workbook stream.
     *
     * @param stream the stream, closed by the reader.
     */
    record ExcelStream(InputStream stream) implements SourceInput {
    }
}
