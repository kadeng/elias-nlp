/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.pattern;
import java.io.Serializable;

/**
 *  Terminal representing a Sequence of Strings.
 *
 *@author     Kai Londenberg
 *@created    21. Januar 2002
 */
public final class TPatternStringSequence extends Terminal implements Serializable {
	final static long serialVersionUID = -5355543586881470109L;
	/**
	 *  The Strings forming the sequence.
	 */
	String sequence[];
	/**
	 *  Their corresponding hashcodes.
	 */
	int hashcodes[];
	/**
	 *  Length of the sequence
	 */
	int length;
	/**
	 *  Hashcode of the complete sequence.
	 */
	int hashcode;


	/**
	 *  Constructor
	 *
	 *@param  seq  Strings to be wrapped as a sequence.
	 */
	TPatternStringSequence(String seq[]) {
		sequence = seq;
		length = seq.length;
		hashcodes = new int[length];
		hashcode = 0;
		for (int i = 0; i < length; i++) {
			hashcodes[i] = seq[i].hashCode();
			hashcode += hashcodes[i] << i;
		}
	}


	public int getMinSize() {
		return length;
	}


	/**
	 *  Returns the overall hashcode of the Sequence.
	 *
	 *@return    Description of the Returned Value
	 */
	public int hashCode() {
		return hashcode;
	}


	public String toString() {
		String ret = sequence[0];
		for (int i = 1; i < sequence.length; i++) {
			ret = ret + " " + sequence[i];
		}
		return ret;
	}


	public int match(DefaultTokenizedInput input, int pos) {
		for (int i = 0; i < length; i++) {
			if (input.tokenHashcode[i + pos] != hashcodes[i]) {
				return -1;
			}
		}
		for (int i = 0; i < length; i++) {
			if (!(input.tokenStrs[i + pos].equals(sequence[i]))) {
				return -1;
			}
		}
		return length;
	}


	/**
	 *  Calculates the hashcode of the Input for the length of this Sequence,
	 *  starting from a given offset. Uses the same method for hashcode-calculation
	 *  as this Classe's own hashCode().
	 *
	 *@param  input  Input
	 *@param  pos    Offset
	 *@return        hashcode-
	 */
	final int calcHashCode(DefaultTokenizedInput input, int pos) {
		int code = 0;
		for (int i = 0; i < length; i++) {
			code += input.tokenHashcode[i + pos] << i;
		}
		return code;
	}

        public String toJSGFString() {
          StringBuffer r = new StringBuffer();
          for (int i=0;i<this.sequence.length;i++) {
            r.append(sequence[i]);
            r.append(" ");
          }
          return r.toString();
        }

}
