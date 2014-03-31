/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.pattern;
import java.io.Serializable;

/**
 *  A String wrapped in a Terminal Object - used by Pattern.NAMED so that there
 *  is no need for an additional String field in Pattern. This Class is not
 *  *really* to be used as a Terminal - but if tried, it will always match with
 *  a length of 0.
 *
 *@author     Kai Londenberg
 *@created    21. Januar 2002
 */
public final class TName extends Terminal implements Serializable {
	final static long serialVersionUID = -8673749633826279359L;
	String name;


	/**
	 *  Constructor for the TName object
	 *
	 *@param  name  String value of this Name
	 */
	public TName(String name) {
		this.name = name;
	}


	public int getMinSize() {
		return 0;
	}


	public String toString() {
		return name;
	}


	public final int match(DefaultTokenizedInput input, int pos) {
		return 0;
	}

}
