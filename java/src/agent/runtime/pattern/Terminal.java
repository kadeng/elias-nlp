/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.pattern;
import java.io.Serializable;

/**
 *  Abstract base class for all Terminal Objects.
 *
 *@author     Kai Londenberg
 *@created    21. Januar 2002
 */
public abstract class Terminal {

	/**
	 *  Gets the Minimum size in lexemes of this Terminal
	 *
	 *@return    The minSize value
	 */
	public abstract int getMinSize();


	/**
	 *  Matches a given input against this Terminal, starting from a given
	 *  position.
	 *
	 *@param  input  Input to be matched against.
	 *@param  pos    start position in the input.
	 *@return        -1 if it did not match, otherwise the length of the match.
	 */
	public abstract int match(DefaultTokenizedInput input, int pos);

        public String toJSGFString() {
          return "{Terminal: " + this.getClass().getName() + "}";
        }

}
