/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.pattern;
import java.io.Serializable;

import org.apache.oro.text.regex.*;

/**
 *  Terminal for a Regular Expression Pattern which shall be applied exactly to
 *  one word. Uses the ORO Regex package (org.apache.oro.text.regex)
 *
 *@author     Kai Londenberg
 *@created    21. Januar 2002
 */
public final class TPatternRegex extends Terminal implements Serializable {
        final static long serialVersionUID = -6106009879370756569L;

	/**
	 *  RegularExpression pattern to be matched against.
	 */
	Perl5Pattern pattern;
	String sregexp;


	/**
	 *  Constructor for the TPatternRegex object
	 *
	 *@param  regexp         Regular Expression String
	 *@exception  Exception  Throws Exception, if regexp could not be compiled
	 *      (Syntax error, or Null Pointer)
	 */
	public TPatternRegex(String regexp) throws Exception {
		sregexp = regexp;
		Perl5Compiler compiler = new Perl5Compiler();
		pattern = (Perl5Pattern) compiler.compile(regexp, compiler.READ_ONLY_MASK | compiler.CASE_INSENSITIVE_MASK);
	}


	public int getMinSize() {
		return 1;
	}


	public String toString() {
		return "Regex: <" + sregexp + ">";
	}


	public int match(DefaultTokenizedInput input, int pos) {
		int startChar = input.token[pos].from;
		input.regexInput.setCurrentOffset(startChar);
		//System.out.println("Starting match of " + toString() + " at character " + startChar);
		//System.out.println("Of input \"" + input.completeInput + "\"");
		if (input.regexmatcher.matchesPrefix(input.regexInput, pattern)) {
			int end = input.regexmatcher.getMatch().end(0) + startChar;
			//     System.out.println("Matched until character " + end);
			int i = pos + 1;
			Token token[] = input.token;
			for (; i < input.tokenCount; i++) {
				if (token[i].from >= end) {
					break;
				}
			}
			//   System.out.println("Matching " + (i-pos) + " tokens starting at position " + pos);
			return i - pos;
		}
		// System.out.println("No match.");
		return -1;
	}

        public String toJSGFString() {
          return "<VOID>"; // can't be transformed to a JSGF Grammar element.
        }
}
