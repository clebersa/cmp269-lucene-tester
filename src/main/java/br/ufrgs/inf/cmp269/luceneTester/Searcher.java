package br.ufrgs.inf.cmp269.luceneTester;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.AttributeFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Searcher used to perform searches in a previous indexed set of files.
 *
 * @author cleber
 */
public class Searcher {

    int hits;
    private HashSet<AnalysisOption> options;

    /**
     * Created a new search with a specific configuration.
     *
     * @param hits Number of documents to be retrieved.
     */
    public Searcher(int hits) {
        this(hits, new AnalysisOption[0]);
    }

    /**
     * Created a new search with a specific configuration.
     *
     * @param hits Number of documents to be retrieved.
     * @param options Options to be used when analyzing the search query.
     */
    public Searcher(int hits, AnalysisOption[] options) {
        this.options = new HashSet<>(Arrays.asList(options));
        this.hits = hits;
    }

    /**
     * Searches for queries in the indexed files.
     */
    public void search() {
        System.out.println("[INFO] Searching...");

        HashMap<Integer, Element> queries = loadQueries();

        IndexReader reader;
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(LuceneTester.properties.getProperty("index_directory"))));
        } catch (IOException exception) {
            System.out.println("[ERROR] Unable to access index. Error: " + exception.getMessage());
            return;
        }
        IndexSearcher searcher = new IndexSearcher(reader);

        Query query;
        Iterator<Map.Entry<Integer, Element>> iterator = queries.entrySet().iterator();
        int queryCounter = 0;
        Date start = new Date();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();

            try {
                query = buildQuery((Element) pair.getValue());
            } catch (ParseException exception) {
                System.out.println("[ERROR] Unable to parse string to search from document "
                        + (Integer) pair.getKey() + " . Error: " + exception.getMessage());
                continue;
            }

            TopDocs results;
            try {
                results = searcher.search(query, hits);
            } catch (IOException exception) {
                System.out.println("[ERROR] Unable to search index. Error: " + exception.getMessage());
                continue;
            }
            ScoreDoc[] retrievedDocuments = results.scoreDocs;
            saveSearch((Integer) pair.getKey(), retrievedDocuments,
                    Math.min(results.totalHits, hits), searcher, (queryCounter > 0));
            queryCounter++;

            System.out.println("[INFO] Search #" + pair.getKey() + " completed...");
        }
        Date end = new Date();
        System.out.println("[INFO] Documents searched in " + (end.getTime() - start.getTime()) + " milliseconds.");
    }

    /**
     * Builds the query to be used in the search based in the XML element
     * representing the query obtained from the queries file.
     *
     * @param xmlDocument XML element containing the query data.
     * @return The query to be used in the search.
     * @throws ParseException If any error occurs while parsing the data for the
     * query.
     */
    private Query buildQuery(Element xmlDocument) throws ParseException {
        Analyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser(IndexableDocument.CONTENT_FIELD, analyzer);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.setLength(0);
        stringBuilder.append(xmlDocument.getElementsByTagName("ES-title").item(0).getTextContent());
        stringBuilder.append(" ");
        stringBuilder.append(xmlDocument.getElementsByTagName("ES-desc").item(0).getTextContent());
        AnalisysPerformer analisysPerformer = new AnalisysPerformer(options);
        String queryString = analisysPerformer.analyze(stringBuilder.toString());

        Query query = parser.parse(parser.escape(queryString));
        return query;
    }

    /**
     * Saves the result of a search in the output file defined in the
     * configuration file.
     *
     * @param searchNumber Number of the search.
     * @param docs Set of documents retrieved in the search.
     * @param amountDocs Amount of documents retrieved.
     * @param searcher Searcher used to search for the documents.
     * @param append Boolean to define if it should write in the beginning or
     * append to the file.
     * @param filename Output file name.
     */
    private void saveSearch(int searchNumber, ScoreDoc[] docs, int amountDocs, IndexSearcher searcher, boolean append) {
        String filename = LuceneTester.properties.getProperty("output_folder") + "search_result";
        filename = options.stream().map((option) -> "-" + option).reduce(filename, String::concat);
        filename += ".txt";

        FileWriter fileWriter;
        File file = new File(filename);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            fileWriter = new FileWriter(file, append);
        } catch (IOException exception) {
            System.out.println("[ERROR] Unable to open File Writer for . Error: " + exception.getMessage());
            return;
        }

        try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                PrintWriter printerWriter = new PrintWriter(bufferedWriter)) {
            for (int index = 0; index < amountDocs; index++) {
                org.apache.lucene.document.Document doc;
                String documentID;
                try {
                    doc = searcher.doc(docs[index].doc);
                    documentID = doc.get(IndexableDocument.ID_FIELD);
                } catch (IOException exception) {
                    System.out.println("[ERROR] Unable to retrieve information about the document "
                            + docs[index].doc + ". Error: " + exception.getMessage());
                    documentID = "[DOCUMENT ID UNKNOWN DUE TO ERROR]";
                }
                printerWriter.printf("%d\tQ0\t%s\t%2d\t%.6f\t%s\n",
                        searchNumber,
                        documentID,
                        index,
                        docs[index].score,
                        LuceneTester.STUDENT);
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Unable to write to the output file. Error: " + e.getMessage());
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("[ERROR] Unable to close File Writer. Error: " + e.getMessage());
            }
        }
    }

    /**
     * Loads the queries from the queries file into a HashMap object.
     *
     * @return A HashMap with query numbers as keys and org.w3c.dom.Element as
     * values.
     */
    private HashMap<Integer, Element> loadQueries() {
        HashMap<Integer, Element> queries = new HashMap<>();

        String queryFileContent = LuceneTester.readFile(LuceneTester.properties.getProperty("queries_file"));
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
