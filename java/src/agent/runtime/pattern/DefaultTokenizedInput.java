/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.pattern;

import java.util.AbstractList;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import elias.agent.runtime.dictionary.*;
import java.util.*;
import ViolinStrings.Strings;
import elias.agent.runtime.api.TokenizedInput;

/**
 *  Represents a normalized, tokenized input which consists of an array of
 *  lexemes.
 *
 *@author     Kai Londenberg
 *@created    21. Januar 2002
 */
public final class DefaultTokenizedInput implements java.io.Serializable, elias.agent.runtime.api.TokenizedInput {
	Token token[];

	transient TokenizedInputDictionaryExtension dictionaryExtension = null;

	/**
	 *  Array of the corresponding strings
	 */
	String[] tokenStrs;
	/**
	 *  Size of the corresponding lexemes in character count.
	 */
	int[] tokenSize;
	/**
	 *  Hashcodes of the normalized Lexemes
	 */
	int[] tokenHashcode;
	/**
	 *  Index of the Terminal which matched this part of the input.
	 */
	int[] matchidx;
	/**
	 *  Amount of lexemes in this input.
	 */
	int tokenCount;
	/**
	 *  String containing the non-normalized form of the complete input.
	 */
	String completeInput;
	/**
	 *  RegularExpression matcher to be used by TPatternRegex.match()
	 *
	 *@see    TPatternRegex
	 */
	Perl5Matcher regexmatcher = new Perl5Matcher();
	PatternMatcherInput regexInput;


	/**
	 *  Constructor for the LexicalInput object.
	 *
	 *@param  full  Original input
	 *@param  l     AbstractList containing the lexemes (Objects of class Lexem).
	 *@see          Lexem
	 */
	public DefaultTokenizedInput(String full, AbstractList l) {
		completeInput = full;
		regexInput = new PatternMatcherInput(completeInput);
		token = new Token[l.size()];
		tokenHashcode = new int[l.size()];
		matchidx = new int[l.size()];
		tokenSize = new int[l.size()];
		tokenCount = l.size();
		tokenStrs = new String[l.size()];
		for (int i = 0; i < l.size(); i++) {
			token[i] = (Token) l.get(i);
			tokenSize[i] = token[i].value.length();
			tokenHashcode[i] = token[i].hashCode();
			tokenStrs[i] = token[i].value;
		}
	}


	private DefaultTokenizedInput() {
	}


	private static void reverseArray(Object array[]) {
		int last = array.length - 1;
		int first = 0;
		Object tmp;
		while (first < last) {
			tmp = array[first];
			array[first] = array[last];
			array[last] = tmp;
			++first;
			--last;
		}
	}


	private static void reverseArray(int array[]) {
		int last = array.length - 1;
		int first = 0;
		int tmp;
		while (first < last) {
			tmp = array[first];
			array[first] = array[last];
			array[last] = tmp;
			++first;
			--last;
		}
	}


	public String getOriginal(int fromToken, int toToken) {
		if (toToken <= fromToken) {
			return "";
		}
		int fromChar = token[fromToken].from;
		int toChar = token[toToken - 1].to;
		return this.completeInput.substring(fromChar, toChar);
	}


	public String[] getNormalizedRange(int from, int to) {
		String result[] = new String[to - from];
		int pos = 0;
		for (int i = from; i < to; i++) {
			result[pos++] = token[i].value;
		}
		return result;
	}


	public String getTokensAsString(String delim) {
		StringBuffer result = new StringBuffer();
		if (token.length == 0) {
			return "";
		}
		for (int i = 0; i < token.length - 1; i++) {
			result.append(token[i].getNormValue());
			result.append(delim);
		}

		result.append(token[token.length - 1]);
		return result.toString();
	}


	public final int getTokenCount() {
		return tokenCount;
	}


	public final Token getToken(int index) {
		return token[index];
	}


	public TokenizedInputDictionaryExtension getDictionaryExtension() {
		return dictionaryExtension;
	}


	public void reverse() {
		reverseArray(this.token);
		reverseArray(this.tokenHashcode);
		reverseArray(this.matchidx);
		reverseArray(this.tokenSize);
		reverseArray(this.tokenStrs);
	}


	/**
	 *  Returns the original unmodified input
	 *
	 *@return    Description of the Returned Value
	 */
	public final String toString() {
		return completeInput;
	}


	public void createDictionaryExtension(TaggedDictionary dictionary) {
		dictionaryExtension = dictionary.newTokenizedInputExtension(this);
	}


	public void freeDictionaryExtension() {
		if (dictionaryExtension == null) {
			return;
		}
		dictionaryExtension.free();
	}


	DefaultTokenizedInput getReduction(boolean keep[]) {
		int newTokenCount = 0;
		for (int i = 0; i < tokenCount; i++) {
			if (keep[i]) {
				newTokenCount++;
			}
		}
		DefaultTokenizedInput result = new DefaultTokenizedInput();
		result.token = new Token[newTokenCount];
		result.matchidx = new int[newTokenCount];
		result.completeInput = completeInput;
		result.regexInput = regexInput;
		result.tokenSize = new int[newTokenCount];
		result.tokenStrs = new String[newTokenCount];
		result.tokenHashcode = new int[newTokenCount];
		result.tokenCount = newTokenCount;
		result.regexmatcher = regexmatcher;
		int pos = 0;
		for (int i = 0; i < tokenCount; i++) {
			if (!keep[i]) {
				continue;
			}
			result.token[pos] = token[i];
			result.tokenSize[pos] = tokenSize[i];
			result.tokenHashcode[pos] = tokenHashcode[i];
			result.tokenStrs[pos++] = tokenStrs[i];
		}
		if (dictionaryExtension != null) {
			result.dictionaryExtension = dictionaryExtension.getReduction(keep, newTokenCount, result);
		}
		return result;
	}


	final void clean_matchidx(int startAt) {
		for (int i = startAt; i < tokenCount; i++) {
			matchidx[i] = -1;
		}
	}
}
