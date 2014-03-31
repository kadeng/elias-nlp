/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.pattern;
import java.io.Serializable;

/**
 *  Terminal representing a single arbitrary String.
 *
 *@author     Kai Londenberg
 *@created    21. Januar 2002
 */
public final class TPatternString extends Terminal implements Serializable {
	final static long serialVersionUID = 4489067050751214991L;
	String value;
	int hashcode;


	/**
	 *  Constructor for the TPatternString object
	 *
	 *@param  value_  String value
	 */
	public TPatternString(String value_) {
		super();
		value = value_;
		hashcode = value.hashCode();
	}


	public int getMinSize() {
		return 1;
	}


	public String toString() {
		return value + " - hashcode: " + hashcode;
	}


	public final int match(DefaultTokenizedInput input, int pos) {
		if (input.tokenHashcode[pos] != hashcode) {
			return -1;
		}
		if (value.equals(input.tokenStrs[pos])) {
			return 1;
		}
		return -1;
	}

        public String toJSGFString() {
          return value;
        }

}
