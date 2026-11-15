package ch.inss.openapi.joaswizard.cli;

import java.util.Scanner;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads missing command line values interactively. All messages are logged on the logger of
 * {@code ch.inss.openapi.joaswizard.Main} with source method {@code main}, so the console output
 * stays identical to the former inline implementation.
 */
public final class Prompter {

    static final String SOURCE_CLASS = "ch.inss.openapi.joaswizard.Main";
    static final String SOURCE_METHOD = "main";

    private final Scanner scanner;
    private final Logger logger = Logger.getLogger(SOURCE_CLASS);

    public Prompter(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Returns args[i] if present, otherwise logs the prompts (INFO) and reads one line. */
    public String argOrAsk(String[] args, int i, String... prompts) {
        if (args.length > i) {
            return args[i];
        }
        for (String prompt : prompts) {
            info(prompt);
        }
        return scanner.nextLine();
    }

    /** Reads one line, an empty line gives the default. */
    public String nextOrDefault(String dflt) {
        String line = scanner.nextLine();
        return line.isEmpty() ? dflt : line;
    }

    /**
     * Returns a valid value. A value from an argument is checked first (invalidArgMsg at INFO if invalid);
     * invalid values lead to retryMsg (WARNING) and a new line is read, empty input gives the default.
     * Prompts for a missing argument must have been logged before (see {@link #argOrAsk}).
     */
    public String validated(String value, boolean fromArg, Predicate<String> ok, String dflt,
                            String invalidArgMsg, String retryMsg) {
        if (fromArg) {
            if (ok.test(value)) {
                return value;
            }
            info(invalidArgMsg);
            warning(retryMsg);
        }
        while (true) {
            String v = nextOrDefault(dflt);
            if (ok.test(v)) {
                return v;
            }
            warning(retryMsg);
        }
    }

    public void info(String msg) {
        logger.logp(Level.INFO, SOURCE_CLASS, SOURCE_METHOD, msg);
    }

    public void warning(String msg) {
        logger.logp(Level.WARNING, SOURCE_CLASS, SOURCE_METHOD, msg);
    }
}
