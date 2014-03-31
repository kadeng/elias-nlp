/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.pattern;
import elias.agent.runtime.dictionary.DictionaryEntry;

/**
 *  Represents a single Input-Token (Lexem) in a natural Language Input. Both in
 *  normalized and original forms.
 *
 *@author     Kai Londenberg
 *@created    21. Januar 2002
 *@see        LexicalInput
 */

public final class Token extends Object {
	int from;
        int to;
	String value;
        byte utfBytes[];
	int hashcode;


	/**
	 *  Constructor for the Lexem object
	 *
	 *@param  normvalue  normalized String value
	 *@param  src        Original source string
	 */
	public Token(String normvalue, int from, int to) {
                if (to<from) throw new IllegalArgumentException();
		value = normvalue;
                this.from = from;
                this.to = to;
		hashcode = normvalue.hashCode();
	}


	/**
	 *  Gets the original (not-normalized) value of this Lexem.
	 *
	 *@return    The originalValue value
	 */

	/**
	 *  Gets the normalized String value of this lexem
	 *
	 *@return    The normValue value
	 */
	public String getNormValue() {
		return value;
	}


	/**
	 *  Returns the hash-code of the normalized String value.
	 *
	 *@return    Description of the Returned Value
	 */
	public int hashCode() {
		return hashcode;
	}


	/**
	 *  Returns the original (not-normalized) String value.
	 *
	 *@return    Description of the Returned Value
	 */
	public String toString() {
		return value;
	}
  public int getFrom() {
    return from;
  }
  public int getTo() {
    return to;
  }

}
