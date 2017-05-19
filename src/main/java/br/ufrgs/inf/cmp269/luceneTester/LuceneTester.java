package br.ufrgs.inf.cmp269.luceneTester;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;

/**
 * Main class of the project, start point for the tool.
 *
 * @author cleber
 */
public class LuceneTester {

    public static final Charset ENCODING = Charset.forName("ISO-8859-15");
    public static final String STUDENT = "Cleber";

    public static Properties properties;

    public static void main(String args[]) {
        int option;
        Indexer indexer;
        Scanner scanner;

        indexer = new Indexer();
        scanner = new Scanner(System.in);
        Searcher searcher;

        loadProperties();

        do {
            System.out.print("\nChoose one option:\n"
                    + "1 - Index collection\n"
                    + "2 - Perform normal search\n"
                    + "3 - Perform search removing stopwords\n"
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
                    indexer.indexCollection();
                    break;
                case 2:
                    searcher = new Searcher(100, SearchMode.NORMAL);
                    searcher.search();
                    break;
                case 3:
                    searcher = new Searcher(100, SearchMode.STOP_WORDS);
                    searcher.search();
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } while (option != 0);

    }

    /**
     * Loads the properties from the properties file.
     */
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

    /**
     * Reads the content of a file and stores it in a string.
     *
     * @param path Path to the file to be read.
     * @return The content of the file in a string. If some error occurs while
     * opening the file, null is returned
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
