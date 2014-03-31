/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.dictionary;
import elias.agent.runtime.pattern.Terminal;
import elias.agent.runtime.pattern.DefaultTokenizedInput;

public final class TPatternWordClass extends Terminal implements java.io.Serializable {
        static final long serialVersionUID=1225931841130761806L;
	int wordClassMask;
	int wordClassMaskOffset;
	int matchMask[];
	int maskLen;


	public TPatternWordClass(int wordClassIndex, int matchMask[]) {
		wordClassMask = 1 << (wordClassIndex & 31);
		wordClassMaskOffset = (wordClassIndex >>> 5);
		this.matchMask = matchMask;
		maskLen = matchMask.length;
	}


	public int getMinSize() {
		return 1;
	}


	public int match(DefaultTokenizedInput input, int pos) {
		//System.out.println("Matching word class pattern against:" + input.getToken(pos).getNormValue());
		DictionaryEntryInterpreter entryInterpreter = input.getDictionaryExtension().dictionaryEntryInterpreter[pos];
		if ((entryInterpreter.entry.dataBitVector[wordClassMaskOffset] & wordClassMask) == 0) {
			//	System.out.println("Word class didn't match");
			return -1;
		}
		if (entryInterpreter.hasMatchingInterpretation(matchMask)) {
			//         System.out.println("Matching interpretation found.");
			return 1;
		}
		// System.out.println("No matching interpretation found.");
		return -1;
	}

        public String toJSGFString() {
          return "<VOID>"; // can't be transformed to a JSGF Grammar element.
        }

}
