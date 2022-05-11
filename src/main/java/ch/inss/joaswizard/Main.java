package ch.inss.joaswizard;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    private static Logger logger = null;

    static {
        FileHandler fileHandler = null;
        try {
            InputStream stream = Main.class.getClassLoader().getResourceAsStream("logging.properties");
            if (stream == null) {
                System.out.println("Missing logging.properties file.");
            } else {
                LogManager.getLogManager().readConfiguration(stream);
            }
            logger = Logger.getLogger(Main.class.getName());
            fileHandler = new FileHandler("joaswizard.log");
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe(e.getLocalizedMessage());
        }
        logger.addHandler(new ConsoleHandler());
        logger.addHandler(fileHandler);
    }

    /**
     * Create OAS3 document with command line arguments or input.
     */
    public static void main(String[] args) {
        logger.info("Starting main.");
        Scanner scanner = new Scanner(new InputStreamReader(System.in));
        String input = null;
        String output = null;
        String resource = null;
        String idfield = null;
        String sourcetype = null;
        if (args.length < 1) {
            System.out.println("Jo needs four parameters.");
            System.out.println("Usage: <inputfile> <outpufile> <resource> <Idfield>");
            System.out.println("You can enter them now.");
            System.out.println("Please enter file name you want to use, or, If you want to use a string, enter that one.");
            input = scanner.nextLine();
        } else {
            input = args[0];
        }
        if (args.length < 2) {
            System.out.println("Please enter the name of the outputfile or leave empty for the default 'openapi.yaml'.");
            output = scanner.nextLine();
        } else {
            output = args[1];
        }
        if (args.length < 3) {
            System.out.println("Please enter the name of the resource (objectname). Like e.g. 'pet'.");
            resource = scanner.nextLine();
        } else {
            resource = args[2];
        }
        if (args.length < 4) {
            System.out.println("Please enter the name of the field which is the key (ID) field.");
            idfield = scanner.nextLine();
        } else {
            idfield = args[3];
        }
        if (args.length < 5) {
            System.out.println("Please enter the name of the field which is the key (ID) field.");
            sourcetype = scanner.nextLine();
        } else {
            sourcetype = args[4];
        }

        InputParameter inputParameter = new InputParameter();
        inputParameter.setInputFile(input);
        inputParameter.setOutputFile(output);
        inputParameter.setResource(resource);
        inputParameter.setResourceId(idfield);
        inputParameter.setSourceType(sourcetype);

        Joaswizard joaswizard = new Joaswizard();
        joaswizard.createCrudFile(inputParameter);
    }

    public static void createOpenApiFromYamlfile(String[] args) {
        System.out.println("Start to create OpenAPi from arguments.");
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
            inputParameter.setSourceType(InputParameter.Sourcetype.YAMLFILE);
        }
        Joaswizard joaswizard = new Joaswizard();
        joaswizard.createCrudFile(inputParameter);
    }
}
