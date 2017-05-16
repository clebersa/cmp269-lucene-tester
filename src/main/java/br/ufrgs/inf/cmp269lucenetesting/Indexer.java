package br.ufrgs.inf.cmp269lucenetesting;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Indexer of files. This file is strongly based on the file from the Lucene
 * demo, available here:
 * https://lucene.apache.org/core/6_5_1/demo/src-html/org/apache/lucene/demo/IndexFiles.html
 *
 * @author cleber
 */
public class Indexer {

    private Date lastIndexDate;
    

    public Indexer() {
        //TODO: Read from index folder and discover when the last index was performed.
    }

    public Date getLastIndexDate() {
        return lastIndexDate;
    }

    public void indexCollection() {
        lastIndexDate = new Date();
        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + LuceneTester.properties.getProperty("index_directory") + "'...");

            Directory dir = FSDirectory.open(Paths.get(LuceneTester.properties.getProperty("index_directory")));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            // Optional: for better indexing performance, if you
            // are indexing many documents, increase the RAM
            // buffer.  But if you do this, increase the max heap
            // size to the JVM (eg add -Xmx512m or -Xmx1g):
            //iwc.setRAMBufferSizeMB(256.0);
            
            IndexWriter writer = new IndexWriter(dir, iwc);
            indexFiles(writer, Paths.get(LuceneTester.properties.getProperty("files_directory")));

            // NOTE: if you want to maximize search performance,
            // you can optionally call forceMerge here.  This can be
            // a terribly costly operation, so generally it's only
            // worth it when your index is relatively static (ie
            // you're done adding documents to it):
            // writer.forceMerge(1);
            
            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass()
                    + "\n with message: " + e.getMessage());
        }

    }

    /**
     * Indexes the given file using the given writer, or if a directory is
     * given, recurses over files and directories found under the given
     * directory.
     *
     * NOTE: This method indexes one document per input file. This is slow. For
     * good throughput, put multiple documents into your input file(s). An
     * example of this is in the benchmark module, which can create "line doc"
     * files, one document per line, using the
     * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
     * >WriteLineDocTask</a>.
     *
     * @param writer Writer to the index where the given file/dir info will be
     * stored
     * @param path The file to index, or the directory to recurse into to find
     * files to index
     * @throws IOException If there is a low-level I/O error
     */
    void indexFiles(final IndexWriter writer, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        processFile(writer, file);
                    } catch (IOException ignore) {
                        // don't index files that can't be read.
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            processFile(writer, path);
        }
    }

    /**
     * Reads the content of a file and stores it in a string.
     *
     * @param path Path to the file to be read.
     * @return The content of the file in a string.
     * @throws IOException if any I/O error occurs.
     */
    public String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, LuceneTester.ENCODING);
    }

    /**
     * Indexes a single file
     */
    void processFile(IndexWriter writer, Path file) throws IOException {
        System.out.println("[INFO] Processing " + file);
        String fileContent = readFile(file.toString());
        
        String documents[] = fileContent.split("(?<=</DOC>)");
        IndexableDocument indexableDocument;
        for (String document : documents) {
            document = document.trim();
            if (document.isEmpty()) {
                continue;
            }
            try {
                indexableDocument = new IndexableDocument();
                indexableDocument.setDocumentId(((document.split("<DOCID>", 2))[1].split("</DOCID>", 2))[0]);
                indexableDocument.setTitle(((document.split("<TITLE>", 2))[1].split("</TITLE>", 2))[0]);
                indexableDocument.setContent(document);
                indexDocument(writer, indexableDocument);
            } catch (Exception exception) {
                System.out.println("[ERROR]\t Unable to write document " + fileContent + ". Error: " + exception.getMessage());
            }
        }
    }

    /**
     * Indexes a single document
     */
    private void indexDocument(IndexWriter writer, IndexableDocument indexableDocument) throws IOException {
        // make a new, empty document
        Document doc = new Document();

        // Add the path of the file as a field named "path".  Use a
        // field that is indexed (i.e. searchable), but don't tokenize 
        // the field into separate words and don't index term frequency
        // or positional information:
        Field pathField = new StringField("path", indexableDocument.getDocumentId(), Field.Store.YES);
        doc.add(pathField);

        // Add the last modified date of the file a field named "modified".
        // Use a LongPoint that is indexed (i.e. efficiently filterable with
        // PointRangeQuery).  This indexes to milli-second resolution, which
        // is often too fine.  You could instead create a number based on
        // year/month/day/hour/minutes/seconds, down the resolution you require.
        // For example the long value 2011021714 would mean
        // February 17, 2011, 2-3 PM.
        // doc.add(new LongPoint("modified", lastModified));
        
        // Add the contents of the file to a field named "contents".  Specify a Reader,
        // so that the text of the file is tokenized and indexed, but not stored.
        // Note that FileReader expects the file to be in UTF-8 encoding.
        // If that's not the case searching for special characters will fail.
        //doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, Charset.forName("ISO-8859-15")))));
        doc.add(new TextField("contents", indexableDocument.getContent(), Field.Store.NO));
        doc.add(new TextField("title", indexableDocument.getTitle(), Field.Store.NO));

        if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
            // New index, so we just add the document (no old document can be there):
            System.out.println("adding " + indexableDocument.getDocumentId());
            writer.addDocument(doc);
        } else {
            // Existing index (an old copy of this document may have been indexed) so 
            // we use updateDocument instead to replace the old one matching the exact 
            // path, if present:
            System.out.println("updating " + indexableDocument.getDocumentId());
            writer.updateDocument(new Term("path", indexableDocument.getDocumentId()), doc);
        }
    }
}
