package br.ufrgs.inf.cmp269.luceneTester;

/**
 * Modes to be used in the index process.
 *
 * @author cleber
 */
public enum AnalysisOption {
    STOP_WORDS, //Removes stop words
    SYNONYMS, //Uses synonyms
    STEM, //Stems the
    NORMALIZATION //Removes accents, punctuation, etc
}
