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

        loadProperties((args.length > 0) ? args[0] : null);

        scanner = new Scanner(System.in);
        Searcher searcher;

        do {
            System.out.print("\nChoose one option:\n"
                    + "1 - Index collection without any analysis\n"
                    + "2 - Index collection removing stop words\n"
                    + "3 - Index collection performing stemming\n"
                    + "4 - Index collection removing stop words and performing stemming\n"
                    + "5 - Search collection without any analysis\n"
                    + "6 - Search collection removing stop words\n"
                    + "7 - Search collection performing stemming\n"
                    + "8 - Search collection removing stop words and performing stemming\n"
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
                    indexer = new Indexer();
                    indexer.indexCollection();
                    break;
                case 2:
                    indexer = new Indexer(new AnalysisOption[]{
                        AnalysisOption.STOP_WORDS});
                    indexer.indexCollection();
                    break;
                case 3:
                    indexer = new Indexer(new AnalysisOption[]{
                        AnalysisOption.STEM});
                    indexer.indexCollection();
                    break;
                case 4:
                    indexer = new Indexer(new AnalysisOption[]{
                        AnalysisOption.STOP_WORDS,
                        AnalysisOption.STEM});
                    indexer.indexCollection();
                    break;
                case 5:
                    searcher = new Searcher(100);
                    searcher.search();
                    break;
                case 6:
                    searcher = new Searcher(100, new AnalysisOption[]{
                        AnalysisOption.STOP_WORDS});
                    searcher.search();
                    break;
                case 7:
                    searcher = new Searcher(100, new AnalysisOption[]{
                        AnalysisOption.STEM});
                    searcher.search();
                    break;
                case 8:
                    searcher = new Searcher(100, new AnalysisOption[]{
                        AnalysisOption.STOP_WORDS,
                        AnalysisOption.STEM});
                    searcher.search();
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } while (option != 0);

    }

    /**
     * Loads the properties from the properties file.
     *
     * @param path Path to the properties file.
     */
    private static void loadProperties(String path) {
        properties = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream((path == null) ? "config.properties" : path);
            properties.load(input);
        } catch (IOException ex) {
            System.out.println("[ERROR] Unable to load properties. Error: " + ex.getMessage());
            System.out.println("[ERROR] Either...");
            System.out.println("[ERROR] - Place the properties file in the same folder where you are running the command line; OR");
            System.out.println("[ERROR] - Provide the URL for the properties file as the first argument in the command line.");
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
