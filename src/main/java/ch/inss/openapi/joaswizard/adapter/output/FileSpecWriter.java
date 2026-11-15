package ch.inss.openapi.joaswizard.adapter.output;

import ch.inss.openapi.joaswizard.application.DiagnosticCollector;
import ch.inss.openapi.joaswizard.port.SpecWriter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes the specification as UTF-8 file.
 */
public final class FileSpecWriter implements SpecWriter {

    private static final String DEFAULT_OUTPUT_FILE = "openapi.yaml";
    private static final String SEP = System.getProperty("file.separator");
    private static final String CURRENT_FOLDER = "." + SEP;
    private static final Logger logger = Logger.getLogger("ch.inss.openapi.joaswizard.Joaswizard");
    private static final Logger utilLogger = Logger.getLogger("ch.inss.openapi.joaswizard.Util");

    @Override
    public String write(String spec, String file, boolean legacyRelative, DiagnosticCollector c) {
        if (file == null || file.isEmpty()) file = DEFAULT_OUTPUT_FILE;
        boolean ok;
        if (!legacyRelative && new File(file).isAbsolute()) {
            ok = writeToPath(Paths.get(file), spec);
        } else {
            ok = writeRelative(CURRENT_FOLDER, spec, file);
        }
        if (ok) {
            logger.logp(Level.FINE, "ch.inss.openapi.joaswizard.Joaswizard", "write", "OpenAPI content written to " + file + ".");
            return file;
        }
        c.error("Could not write file " + CURRENT_FOLDER + file);
        return null;
    }

    /**
     * Writes below a folder: userFolder + separator + file.
     *
     * @return true if written.
     */
    public static boolean writeRelative(String userFolder, String data, String file) {
        if (userFolder == null) userFolder = ".";
        if (file == null) file = DEFAULT_OUTPUT_FILE;
        return writeToPath(Paths.get(userFolder + SEP + file), data);
    }

    /** Writes UTF-8 data to the path and creates missing parent folders. */
    public static boolean writeToPath(Path path, String data) {
        if (data == null) data = "";
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(path, data.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException | RuntimeException e) {
            utilLogger.logp(Level.SEVERE, "ch.inss.openapi.joaswizard.Util", "writeToPath", "Writing file failed: " + path + ": " + e);
            return false;
        }
    }
}
