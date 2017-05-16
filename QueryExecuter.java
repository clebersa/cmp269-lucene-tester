
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class QueryExecuter {

    public static void main(String args[]) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage:\nQueryExecuter queryFile resultFile");
            System.exit(0);
        }

        String fileContent = "";
        try {
            fileContent = readFile(args[0], Charset.forName("ISO-8859-15"));
        } catch (IOException exception) {
            System.out.println("[ERROR] Unable to read file " + args[0] + ". Error: " + exception.getMessage());
        }

        HashMap<Integer, Element> queries = splitQueries(fileContent);
        runQueries(queries, args[1]);
        System.out.println("Execution finished.");
    }

    /**
     * Reads the content of a file and stores it in a string.
     *
     * @param path Path to the file to be read.
     * @param encoding Encoding used to read the file.
     * @return The content of the file in a string.
     * @throws IOException if any I/O error occurs.
     */
    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    /**
     * Splits a file containing queries into a HashMap object.
     *
     * @param queryFileContent The content of the file.
     * @return A HashMap with query numbers as keys and import org.w3c.dom.Element as values.
     */
    private static HashMap<Integer, Element> splitQueries(String queryFileContent) {
        ByteArrayInputStream input = 
                new ByteArrayInputStream(("<?xml version='1.0' encoding='ISO-8859-15' ?><root>"
                        + queryFileContent + "</root>").getBytes(Charset.forName("ISO-8859-15")));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        HashMap<Integer, Element> queries = new HashMap<>();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(input);
            NodeList queriesNodes = doc.getElementsByTagName("top");
            Integer queryNumber;
            for (int index = 0; index < queriesNodes.getLength(); index++) {
                Node node = queriesNodes.item(index);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    try{
                        queryNumber = Integer.parseInt(element.getElementsByTagName("num").item(0).getTextContent().trim());
                        System.out.printf("[INFO] Query #%d found.\n", queryNumber);
                        queries.put(queryNumber, element);
                    } catch (DOMException | NumberFormatException exception){
                        System.out.println("[ERROR] Unable to create query. Error: " + exception.getMessage());
                    }
                } else {
                    System.out.println("[WARN] Node is not an element.");
                }
            }
        } catch(Exception exception){
            System.out.println("[ERROR] Unable to create XML parser. Error: " + exception.getMessage());
            exception.printStackTrace();
        }
        
        return queries;
    }
    
    private static void runQueries(HashMap<Integer, Element> queries, String outputFilePath){
        
    }
}