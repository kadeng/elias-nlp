/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.dictionary;

/**
 *  Title: Elias Agent Description: Ihre Beschreibung Copyright: Copyright (c)
 *  1999 Company:
 *
 *@author     Kai Londenberg
 *@created    4. Maerz 2002
 *@version
 */

public class BackreferenceAttributeValue extends AttributeValue implements java.io.Serializable {
			static final long serialVersionUID = -3445924627156475649L;
			int backReferences[] = new int[0];


	BackreferenceAttributeValue(Attribute attribute, String value) {
		super(attribute, value);
	}


	public int getBackreference(int index) {
		return backReferences[index];
	}


	public int getBackreferenceCount() {
		return backReferences.length;
	}

        void addBackreference(int index) {
          int newBackReferences[] = new int[backReferences.length+1];
          System.arraycopy(backReferences, 0, newBackReferences, 0, backReferences.length);
          newBackReferences[backReferences.length]=index;
          backReferences = newBackReferences;
        }

        public boolean isBackreference() {
          return true;
        }

}
