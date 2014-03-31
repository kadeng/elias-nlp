/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.dictionary;

import java.io.Serializable;

/**
 *  Title: Elias Agent Description: Ihre Beschreibung Copyright: Copyright (c)
 *  1999 Company:
 *
 *@author     Kai Londenberg
 *@created    31. Januar 2002
 *@version
 */

public class Tag implements Serializable {
    static final long serialVersionUID = 4019250240836887410L;
	String identifier = "";
	Attribute attribute;
	String description = "";
        int index = 0;
        boolean ignore;
        boolean byDefaultSet;

	public Tag() {
	}

	public void setAttribute(Attribute newAttribute) {
		attribute = newAttribute;
	}


	public void setDescription(String newDescription) {
		description = newDescription;
	}


	public void setIdentifier(String newIdentifier) {
		identifier = newIdentifier;
	}


	public void setIndex(int newIndex) {
		index = newIndex;
	}


	public String getIdentifier() {
		return identifier;
	}


	public Attribute getAttribute() {
		return attribute;
	}


	public String getDescription() {
		return description;
	}


	public int getIndex() {
		return index;
	}


	public int hashCode() {
		return identifier.hashCode();
	}


	public boolean equals(Object obj) {
		if (!(obj instanceof Tag)) {
			return false;
		}
		Tag t = (Tag) obj;
		if (t.identifier.equals(identifier) && (t.attribute==attribute)) {
			return true;
		}
		return false;
	}

  public void setIgnore(boolean newIgnore) {
    ignore = newIgnore;
  }

  public boolean isIgnore() {
    return ignore;
  }

  public void setByDefaultSet(boolean newByDefaultSet) {
    byDefaultSet = newByDefaultSet;
  }

  public boolean isByDefaultSet() {
    return byDefaultSet;
  }
}
