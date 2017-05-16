package br.ufrgs.inf.cmp269lucenetesting;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author cleber
 */
public class Searcher {

    int hits;
    SearchMode searchMode;

    public Searcher() {
        hits = 10;
        searchMode = SearchMode.NORMAL;
    }

    public Searcher(int hits, SearchMode searchMode) {
        this.hits = hits;
        this.searchMode = searchMode;
    }

    public void search() {
        System.out.println("Searching");

        HashMap<Integer, Element> queries = loadQueries();

    }

    /**
     * Loads the queries from a file into a HashMap object.
     *
     * @param queryFileContent The content of the file.
     * @return A HashMap with query numbers as keys and import
     * org.w3c.dom.Element as values.
     */
    private HashMap<Integer, Element> loadQueries() {
        HashMap<Integer, Element> queries = new HashMap<>();

        String queryFileContent = LuceneTester.readFile(LuceneTester.properties.getProperty("queries_directory"));
        if (queryFileContent == null) {
            return queries;
        }

        ByteArrayInputStream input
                = new ByteArrayInputStream(("<?xml version='1.0' encoding='ISO-8859-15' ?><root>"
                        + queryFileContent + "</root>").getBytes(Charset.forName("ISO-8859-15")));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(input);
            NodeList queriesNodes = doc.getElementsByTagName("top");
            Integer queryNumber;
            for (int index = 0; index < queriesNodes.getLength(); index++) {
                Node node = queriesNodes.item(index);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    try {
                        queryNumber = Integer.parseInt(element.getElementsByTagName("num").item(0).getTextContent().trim());
                        System.out.printf("[INFO] Query #%d found.\n", queryNumber);
                        queries.put(queryNumber, element);
                    } catch (DOMException | NumberFormatException exception) {
                        System.out.println("[ERROR] Unable to create query. Error: " + exception.getMessage());
                    }
                } else {
                    System.out.println("[WARN] Node is not an element.");
                }
            }
        } catch (Exception exception) {
            System.out.println("[ERROR] Unable to create XML parser. Error: " + exception.getMessage());
            exception.printStackTrace();
        }

        return queries;
    }

}