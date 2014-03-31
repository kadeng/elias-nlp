/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.dictionary;
import elias.agent.runtime.pattern.Terminal;
import elias.agent.runtime.pattern.DefaultTokenizedInput;

public final class TPatternWordClasses extends Terminal implements java.io.Serializable {
        static final long serialVersionUID=-7469755603650459195L;

	int wordClassMask[];
	int len;


	public TPatternWordClasses(BitVector wordClassMask) {
		this.wordClassMask = new int[wordClassMask.intSize()];
		wordClassMask.copyInto(this.wordClassMask, 0);
		len = this.wordClassMask.length;
	}


	public int getMinSize() {
		return 1;
	}


	public int match(DefaultTokenizedInput input, int pos) {
		int cmp[] = input.getDictionaryExtension().dictionaryEntry[pos].dataBitVector;
		for (int i = 0; i < len; i++) {
			if ((cmp[i] & wordClassMask[i]) != wordClassMask[i]) {
				return -1;
			}
		}
		return 1;
	}

        public String toJSGFString() {
          return "<VOID>"; // can't be transformed to a JSGF Grammar element.
        }
}
