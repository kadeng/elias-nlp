package elias.agent.authoring.patterncompiler.transformation;

import elias.agent.runtime.pattern.PatternStructure;
import java.util.Hashtable;

 import elias.agent.runtime.dictionary.WordClassPatternCompiler;

public interface PatternTransformator {

  public void setWordClassPatternCompiler(WordClassPatternCompiler newWordClassPatternCompiler);

  public String transformPatternDefinition(String patternText, Hashtable predefinedPatterns, String basePackage) throws Exception;

}