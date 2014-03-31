package elias.agent.authoring.patterncompiler;

import elias.agent.runtime.pattern.PatternStructure;
import java.util.Hashtable;

/**
 * Title:        Elias Agent
 * Description:  Ihre Beschreibung
 * Copyright:    Copyright (c) 1999
 * Company:
 * @author Kai Londenberg
 * @version
 */
 import elias.agent.runtime.dictionary.WordClassPatternCompiler;

public interface PatternCompiler {

    public void setWordClassPatternCompiler(WordClassPatternCompiler newWordClassPatternCompiler);

  public PatternStructure createPatternStructure(String patternText, Hashtable predefinedPatterns) throws Exception;

}