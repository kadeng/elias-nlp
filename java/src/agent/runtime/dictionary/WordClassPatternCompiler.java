/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.dictionary;

import java.util.*;
import ViolinStrings.Strings;
import elias.agent.runtime.pattern.Terminal;
import elias.agent.runtime.pattern.TPatternAnyToken;

public class WordClassPatternCompiler implements java.io.Serializable {
	WordClass wordClasses[];
	int wcBits;
	HashMap wordClassMap;


	public WordClassPatternCompiler(TaggedDictionary dictionary) {
		this(dictionary.wordClasses);
	}


	public WordClassPatternCompiler(WordClass wordClasses[]) {
		this.wordClasses = wordClasses;
		this.wordClassMap = new HashMap(wordClasses.length, 0.1f);
		wcBits = TaggedDictionaryBuilder.minBitSize(wordClasses.length);
		for (int i = 0; i < wordClasses.length; i++) {
			wordClassMap.put(wordClasses[i].getIdentifier(), wordClasses[i]);
		}
	}


	private static String[] getWords(String pattern) {
		char patternChars[] = pattern.toCharArray();
		int wordCount = 0;
		for (int i = 0; i < patternChars.length; i++) {
			if (patternChars[i] != ' ') {
				wordCount++;
				while ((i < patternChars.length) && (patternChars[i] != ' ')) {
					i++;
				}
				// skip rest of word
			}
		}
		String words[] = new String[wordCount];
		wordCount = 0;
		int startPos;
		for (int i = 0; i < patternChars.length; i++) {
			if (patternChars[i] != ' ') {
				// skip whitespace
				startPos = i++;
				while ((i < patternChars.length) && (patternChars[i] != ' ')) {
					++i;
				}
				// skip rest of word
				words[wordCount++] = pattern.substring(startPos, i);
			}
		}
		return words;
	}


	public Terminal compileWordClassPattern(String pattern) throws Exception {
		if (pattern.equals("TOKEN")) {
			return TPatternAnyToken.getAnyToken();
		}
		String words[] = getWords(pattern);
		if (words.length == 0) {
			throw new Exception("Empty word class pattern");
		}
		WordClass wc = (WordClass) wordClassMap.get(words[0]);
		if (wc == null) {
			throw new Exception("Word class " + words[0] + " does not exist.");
		}
		if (words.length == 1) {
			//     System.out.println("Compiled pattern <" + pattern + "> - from index " + wc.index + " to " + wc.indexTo);
			//    System.out.println(wc.getWordClassBits().toString());
			return new TPatternWordClasses(wc.getWordClassBits());
		}
		if (wordClassMap.get(words[1]) != null) {
			BitVector wordClassBits = wc.getWordClassBits().copy();
			for (int i = 1; i < words.length; i++) {
				wc = (WordClass) wordClassMap.get(words[i]);
				if (wc == null) {
					throw new Exception("Word class " + words[i] + " does not exist.");
				}
				wordClassBits.OR(wc.getWordClassBits());
			}
			return new TPatternWordClasses(wordClassBits);
		}
		BitVector matchMask = new BitVector();
		Map tm = wc.getTagMap();
		for (int i = 1; i < words.length; i++) {
			Tag t = (Tag) tm.get(words[i]);
			if (t == null) {
				throw new Exception("Word class " + words[0] + " has no <" + words[i] + "> tag.");
			}
			matchMask.setBit(wcBits + t.getIndex());
		}
		if (wc.index == wc.indexTo) {
			matchMask.setRange(0, wcBits, wc.index);
//      System.out.println("Compiled <" +  pattern + "> - Matching from " + wc.index + " to " + wc.indexTo);
//      System.out.println("Match mask = " + matchMask.toString());
			return new TPatternWordClass(wc.getIndex(), matchMask.flags);
		}
//    System.out.println("Compiled <" +  pattern + "> - Matching from " + wc.index + " to " + wc.indexTo);
//    System.out.println("Match mask = " + matchMask.toString());
		return new TPatternAbstractWordClass(wc.index, wc.indexTo, matchMask.flags);
	}



}
