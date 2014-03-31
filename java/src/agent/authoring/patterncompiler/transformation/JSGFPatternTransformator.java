/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.authoring.patterncompiler.transformation;

import elias.agent.runtime.pattern.PatternStructure;
import elias.agent.authoring.patterncompiler.PatternCompiler;
import java_cup.runtime.*;
import java.util.Hashtable;
import elias.agent.runtime.dictionary.WordClassPatternCompiler;


public class JSGFPatternTransformator implements PatternTransformator {

	protected JSGFParser patternparser = new JSGFParser();
        WordClassPatternCompiler wordClassPatternCompiler = null;

        public void setWordClassPatternCompiler(WordClassPatternCompiler newWordClassPatternCompiler) {
          wordClassPatternCompiler = newWordClassPatternCompiler;
        }

	public JSGFPatternTransformator() {
	}

	public synchronized String transformPatternDefinition(String patternText, Hashtable predefinedPatterns, String basePackage, WordClassPatternCompiler wcc) throws Exception {
		patternparser.setScanner(new TransformationLexer(patternText, predefinedPatterns, basePackage, wcc));
		Symbol sym = patternparser.parse();
		return (String) sym.value;
	}

        public String transformPatternDefinition(String patternText, Hashtable predefinedPatterns, String basePackage) throws Exception {
              return transformPatternDefinition(patternText, predefinedPatterns, basePackage, wordClassPatternCompiler);
	}

}
