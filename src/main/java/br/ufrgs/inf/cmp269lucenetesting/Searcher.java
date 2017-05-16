package br.ufrgs.inf.cmp269lucenetesting;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

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

        //IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(LuceneTester.properties.getProperty("index_directory"))));
        IndexReader reader = null;
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();

        BufferedReader in = null;
        String query = null;

        //in = Files.newBufferedReader(Paths.get(query), LuceneTester.ENCODING);
        //QueryParser parser = new QueryParser("contents", analyzer);

    }
}
