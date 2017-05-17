package br.ufrgs.inf.cmp269lucenetesting;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cleber
 */
public class LuceneTester {

    public static final Charset ENCODING = Charset.forName("ISO-8859-15");
    public static final String STUDENT = "Cleber";

    public static Properties properties;
    private static Indexer indexer;
    private static Scanner scanner;

    public static void main(String args[]) {
        int option;
        indexer = new Indexer();
        scanner = new Scanner(System.in);
        Searcher searcher;

        loadProperties();

        do {
            System.out.print("\nChoose one option:\n"
                    + "1 - Index collection\n"
                    + "2 - Perform normal search\n"
                    + "0 - Quit\n"
                    + "Option: ");
            try {
                option = scanner.nextInt();
            } catch (InputMismatchException exception) {
                scanner.next();
                option = -1;
            }
            switch (option) {
                case 0:
                    break;
                case 1:
                    handleIndexOperation();
                    break;
                case 2:
                    searcher = new Searcher(100, SearchMode.NORMAL);
                    try {
                        searcher.search();
                    } catch (IOException ex) {
                        Logger.getLogger(LuceneTester.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } while (option != 0);

    }

    private static void loadProperties() {
        properties = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream("config.properties");
            properties.load(input);
        } catch (IOException ex) {
            System.out.println("[WARN] Unable to load properties. Error: " + ex.getMessage());
            System.exit(1);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    System.out.println("[WARN] Unable to close input stream for properties file. Error: " + e.getMessage());
                }
            }
        }
    }

    private static void handleIndexOperation() {
        if (indexer.getLastIndexDate() != null) {
            System.out.println("The collection was indexed at "
                    + indexer.getLastIndexDate() + ". Do you want to index again?\n"
                    + "Yes or No?[no]");
            String answer = scanner.next();
            if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) {
                indexer.indexCollection();
            } else {
                System.out.println("Operation cancelled.");
            }
        } else {
            indexer.indexCollection();
        }
    }

    /**
     * Reads the content of a file and stores it in a string.
     *
     * @param path Path to the file to be read.
     * @return The content of the file in a string. If some error occurs to open
     * the file, null is returned
     */
    public static String readFile(String path) {
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(path)), LuceneTester.ENCODING);
        } catch (IOException exception) {
            System.out.println("[ERROR] Unable to read file " + path + ". Error: " + exception.getMessage());
            content = null;
        }
        return content;
    }
}
