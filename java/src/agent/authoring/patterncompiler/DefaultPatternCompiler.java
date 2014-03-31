/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.authoring.patterncompiler;

import elias.agent.runtime.pattern.PatternStructure;
import java_cup.runtime.*;
import java.util.Hashtable;
import elias.agent.runtime.dictionary.WordClassPatternCompiler;


public class DefaultPatternCompiler implements PatternCompiler {

	protected parser patternparser = new EnhancedParser();
        WordClassPatternCompiler wordClassPatternCompiler = null;

        public void setWordClassPatternCompiler(WordClassPatternCompiler newWordClassPatternCompiler) {
          wordClassPatternCompiler = newWordClassPatternCompiler;
        }


	public DefaultPatternCompiler() {
	}


	public synchronized PatternStructure createPatternStructure(String patternText, Hashtable predefinedPatterns, WordClassPatternCompiler wcc) throws Exception {
		patternparser.setScanner(new DefaultLexer(patternText, predefinedPatterns, wcc));
		Symbol sym = patternparser.parse();
		return (PatternStructure) sym.value;

	}

        public PatternStructure createPatternStructure(String patternText, Hashtable predefinedPatterns) throws Exception {
              return createPatternStructure(patternText, predefinedPatterns, wordClassPatternCompiler);
	}

}
