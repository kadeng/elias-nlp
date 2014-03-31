/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.dictionary;
import elias.agent.runtime.pattern.Terminal;
import elias.agent.runtime.pattern.DefaultTokenizedInput;

/**
 *  Title: Elias Agent Description: Ihre Beschreibung Copyright: Copyright (c)
 *  1999 Company:
 *
 *@author     Kai Londenberg
 *@created    26. Maerz 2002
 *@version
 */

public class TPatternAbstractWordClass extends Terminal implements java.io.Serializable {
        static final long serialVersionUID=4497793486570609755L;
	int wcIndex;
	int wcMaxIndex;
	int matchMask[];
	int wordClassMask;
	int wordClassMaskOffset;


	public TPatternAbstractWordClass(int wcIndex, int wcMaxIndex, int matchMask[]) {
		this.wcIndex = wcIndex;
		wordClassMask = 1 << (wcIndex & 31);
		wordClassMaskOffset = (wcIndex >>> 5);
		this.wcMaxIndex = wcMaxIndex;
		this.matchMask = matchMask;
	}


	public int getMinSize() {
		return 1;
	}


	public int match(DefaultTokenizedInput input, int pos) {
		DictionaryEntryInterpreter entryInterpreter = input.getDictionaryExtension().dictionaryEntryInterpreter[pos];
		if ((entryInterpreter.entry.dataBitVector[wordClassMaskOffset] & wordClassMask) == 0) {
			//	System.out.println("Word classes did not match");
			return -1;
		}
		if (entryInterpreter.hasMatchingInterpretation(matchMask, wcIndex, wcMaxIndex)) {
			return 1;
		}
		//System.out.println("Didn't find matching interpretation.");
		return -1;
	}

        public String toJSGFString() {
          return "<VOID>"; // can't be transformed to a JSGF Grammar element.
        }
}
