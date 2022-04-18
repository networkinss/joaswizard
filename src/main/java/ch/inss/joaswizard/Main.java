package ch.inss.joaswizard;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    private static Logger logger = null;

    static {
        FileHandler fileHandler = null;
        try {
            InputStream stream = Joaswizard.class.getClassLoader().getResourceAsStream("logging.properties");
            try {
                LogManager.getLogManager().readConfiguration(stream);
                logger = Logger.getLogger(Joaswizard.class.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileHandler = new FileHandler("joaswizard.log");
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.addHandler(fileHandler);
    }
    /** Create OAS3 document with all CRUD operations for one object. */
    public static void main(String[] args) {
        logger.info("Starting main.");
        if (args.length < 3) {
            System.out.println("Need four parameter.");
            System.out.println("Usage: <inputfile> <outpufile> <resource> <Idfield>");
            System.exit(1);
        }
        InputParameter inputParameter = new InputParameter();
        inputParameter.setInputFile(args[0]);
        inputParameter.setOutputFile(args[1]);
        inputParameter.setResource(args[2]);
        if (args.length >= 4) {
            inputParameter.setResourceId(args[3]);
        } else {
            inputParameter.setResourceId("id");
        }

        if (args.length >= 5) {
            inputParameter.setSourceType(args[4]);
        } else {
            inputParameter.setSourceType("YAML");
        }
        Joaswizard joaswizard = new Joaswizard();
        joaswizard.createCrudFile(inputParameter);
    }
}
