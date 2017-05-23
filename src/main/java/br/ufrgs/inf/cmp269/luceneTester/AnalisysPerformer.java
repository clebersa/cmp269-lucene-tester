package br.ufrgs.inf.cmp269.luceneTester;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;

/**
 *
 * @author cleber
 */
public class AnalisysPerformer {
    
    private String target;
    private HashSet<AnalysisOption> options;
    
    public AnalisysPerformer(AnalysisOption[] options) {
        this.options = new HashSet<>(Arrays.asList(options));
    }
    
    public String analyze(String target){
        this.target = target;
        
        if(options.contains(AnalysisOption.STOP_WORDS)){
            removeStopWords();
        }
        
        if(options.contains(AnalysisOption.SYNONYMS)){
            
        }
        
        //Synonym
        
        //Stem
        
        //Analyze
        
        //
        
        return this.target;
    }
    
    /**
     * Removes stop words from the query.
     */
    private void removeStopWords() {
        CharArraySet stopWords = SpanishAnalyzer.getDefaultStopSet();
        AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;
        StandardTokenizer standardTokenizer = new StandardTokenizer(factory);
        standardTokenizer.setReader(new StringReader(target));
        TokenStream tokenStream = standardTokenizer;
        tokenStream = new StopFilter(tokenStream, stopWords);

        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        try {
            StringBuilder sb = new StringBuilder();
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String term = charTermAttribute.toString();
                sb.append(term + " ");
            }
            target = sb.toString();
        } catch (IOException exception) {
            System.out.println("[ERROR] Unable to remove stopwords. Error: " + exception.getMessage());
        }
    }
    
    private void addSynonym(){
        
    }
}
