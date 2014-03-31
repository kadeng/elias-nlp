/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.pattern;

public class TPatternAnyToken extends Terminal implements java.io.Serializable {
	final static long serialVersionUID = 4078854825499700620L;

	private static TPatternAnyToken anytoken = new TPatternAnyToken();


	private TPatternAnyToken() {
	}


	public static TPatternAnyToken getAnyToken() {
		return anytoken;
	}


	public int getMinSize() {
		return 1;
	}


	public int match(DefaultTokenizedInput input, int pos) {
		return 1;
	}

        public String toJSGFString() {
          return "<TOKEN>";
        }
}
