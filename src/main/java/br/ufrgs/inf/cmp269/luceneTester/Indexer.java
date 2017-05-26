package br.ufrgs.inf.cmp269.luceneTester;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Indexer for SGML files. This file is based on the file from the Lucene demo,
 * available here:
 * https://github.com/apache/lucene-solr/blob/master/lucene/demo/src/java/org/apache/lucene/demo/IndexFiles.java
 * The demo is available here:
 * https://lucene.apache.org/core/6_5_1/demo/overview-summary.html
 *
 * @author cleber
 */
public class Indexer {

    private HashSet<AnalysisOption> options;

    public Indexer() {
        this(new AnalysisOption[0]);
    }

    /**
     * Creates a new indexer with options for indexing.
     *
     * @param options Options to be used when indexing.
     */
    public Indexer(AnalysisOption[] options) {
        this.options = new HashSet<>(Arrays.asList(options));
    }

    /**
     * Indexes a collection of SGML files.
     */
    public void indexCollection() {
        System.out.println("[INFO] Indexing to directory '"
                + LuceneTester.properties.getProperty("index_directory") + "'...");

        Path filesDirectory = Paths.get(LuceneTester.properties.getProperty("files_directory"));
        if (!Files.isReadable(filesDirectory)) {
            System.out.println("[ERROR] Unable to read from files directory '"
                    + filesDirectory.toAbsolutePath() + "'. Check path in the configuration file.");
            return;
        }

        Path indexDirectory = Paths.get(LuceneTester.properties.getProperty("index_directory"));
        if (!indexDirectory.toFile().exists() && !indexDirectory.toFile().mkdirs()) {
            System.out.println("[ERROR] Unable to write to index directory '"
                    + indexDirectory.toAbsolutePath() + "'. The directory could not be created. Check path in the configuration file.");
            return;
        } else if (!Files.isWritable(indexDirectory)) {
            System.out.println("[ERROR] Unable to write to index directory '"
                    + indexDirectory.toAbsolutePath() + "' The directory is not writable. Check path in the configuration file.");
            return;
        }
        
        System.out.println("[INFO] Deleting old index if any...");
        for(File file: indexDirectory.toFile().listFiles()){
            file.delete();
        }

        try (Directory luceneIndexDirectory = FSDirectory.open(indexDirectory);
                Analyzer analyzer = new StandardAnalyzer()) {
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            Date start = new Date();
            try (IndexWriter writer = new IndexWriter(luceneIndexDirectory, indexWriterConfig)) {
                processFiles(writer, filesDirectory);
            }

            Date end = new Date();
            System.out.println("[INFO] Documents indexed in " + (end.getTime() - start.getTime()) + " milliseconds.");
        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass()
                    + "\n with message: " + e.getMessage());
        }

    }

    /**
     * Processes the given file using the given writer, or if a directory is
     * given, recurses over files and directories found under the given
     * directory.
     *
     * @param writer Writer to the index where the given file/dir info will be
     * stored.
     * @param path The file to index, or the directory to recurse into to find
     * files to index.
     * @throws IOException If there is a low-level I/O error.
     */
    private void processFiles(final IndexWriter writer, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    processFile(writer, file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            processFile(writer, path);
        }
    }

    /**
     * Processes a single file, extracting documents from it and indexing them.
     *
     * @param writer Writer to the index where the given file info will be
     * stored.
     * @param path The file to index, or the directory to recurse into to find
     * files to index.
     */
    private void processFile(IndexWriter writer, Path file) {
        String fileContent = LuceneTester.readFile(file.toString());
        if (fileContent == null) {
            System.out.println("[ERROR] Unable to process " + file.toString());
            return;
        }
        System.out.println("[INFO] Processing " + file.toString());

        String documents[] = fileContent.split("(?<=</DOC>)");
        for (String document : documents) {
            document = document.trim();
            if (document.isEmpty()) {
                continue;
            }
            try {
                indexDocument(writer, buildDocument(document));
            } catch (Exception exception) {
                System.out.println("[ERROR]\t Unable to parse and process document " + file.toString() + ". Error: " + exception.getMessage());
            }
        }
    }

    /**
     * Builds a indexable document.
     *
     * @param documentContent Content of the entire document (from &lt;DOC&gt;
     * to &lt;/DOC&gt;)
     * @return
     */
    private IndexableDocument buildDocument(String documentContent) {
        IndexableDocument indexableDocument = new IndexableDocument();
        AnalisysPerformer analisysPerformer = new AnalisysPerformer(options);
        indexableDocument.setId(((documentContent.split("<DOCID>", 2))[1].split("</DOCID>", 2))[0]);
        indexableDocument.setTitle(analisysPerformer.analyze(
                ((documentContent.split("<TITLE>", 2))[1].split("</TITLE>", 2))[0]));
        indexableDocument.setContent(analisysPerformer.analyze(
                ((documentContent.split("<TEXT>", 2))[1].split("</TEXT>", 2))[0]));

        return indexableDocument;
    }

    /**
     * Indexes a single document.
     *
     * @param writer Writer to the index where the given file info will be
     * stored.
     * @param indexableDocument Indexable document to be indexed.
     * @throws IOException If there is a low-level I/O error.
     */
    private void indexDocument(IndexWriter writer, IndexableDocument indexableDocument) throws IOException {
        Document doc = new Document();

        doc.add(new StringField(IndexableDocument.ID_FIELD,
                indexableDocument.getId(), Field.Store.YES));
        doc.add(new TextField(IndexableDocument.TITLE_FIELD,
                indexableDocument.getContent(), Field.Store.NO));
        doc.add(new TextField(IndexableDocument.CONTENT_FIELD,
                indexableDocument.getContent(), Field.Store.NO));

        if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
            System.out.println("adding " + indexableDocument.getId());
            writer.addDocument(doc);
        } else {
            System.out.println("updating " + indexableDocument.getId());
            writer.updateDocument(new Term(IndexableDocument.ID_FIELD,
                    indexableDocument.getId()), doc);
        }
    }
}
