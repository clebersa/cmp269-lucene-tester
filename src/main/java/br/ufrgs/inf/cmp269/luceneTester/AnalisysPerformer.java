package br.ufrgs.inf.cmp269.luceneTester;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.es.SpanishLightStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;

/**
 *
 * @author cleber
 */
public class AnalisysPerformer {
    
    public static void main(String args[]){
        AnalisysPerformer a = new AnalisysPerformer(new AnalysisOption[]{
            AnalysisOption.STEM, 
            AnalysisOption.STOP_WORDS
        });
        a.analyze("Estados Unidos instó hoy a Corea\n" +
" del Norte, en una reunión bilateral en la ONU, a acelerar los\n" +
" trámites para que empiecen cuanto antes las inspecciones\n" +
" internacionales de sus instalaciones nucleares declaradas, aceptadas\n" +
" en esta jornada finalmente por Pyongyang.");
    }
    
    private String originalTarget;
    private HashSet<AnalysisOption> options;
    
    public AnalisysPerformer(AnalysisOption[] options) {
        this.options = new HashSet<>(Arrays.asList(options));
    }
    
    public String analyze(String target){
        this.originalTarget = target;
        
        AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;
        StandardTokenizer standardTokenizer = new StandardTokenizer(factory);
        standardTokenizer.setReader(new StringReader(originalTarget));
        TokenStream tokenStream = standardTokenizer;
        
        System.out.println("ANTES LOWER");
        System.out.println(getAnalyzedTarget(tokenStream));
        tokenStream = new LowerCaseFilter(tokenStream);
         
        System.out.println("ANTES STOP");
        System.out.println(getAnalyzedTarget(tokenStream));
        if(options.contains(AnalysisOption.STOP_WORDS)){
            tokenStream = new StopFilter(tokenStream, SpanishAnalyzer.getDefaultStopSet());
        }
        System.out.println("ANTES STEM");
        System.out.println(getAnalyzedTarget(tokenStream));
        
        
        if(options.contains(AnalysisOption.STEM)){
            tokenStream = new SpanishLightStemFilter(tokenStream);
        }
        
        return getAnalyzedTarget(tokenStream);
    }
    
    public String getAnalyzedTarget(TokenStream tokenStream){
        String target;
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        try {
            StringBuilder sb = new StringBuilder();
            
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String term = charTermAttribute.toString();
                sb.append(term).append(" ");
            }
            target = sb.toString();
        } catch (IOException exception) {
            target = this.originalTarget;
            System.out.println("[ERROR] Unable to stringfy token stream. Error: " + exception.getMessage());
        }
        return target;
    }
}
