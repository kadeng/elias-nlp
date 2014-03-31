/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.dictionary;

import java.io.*;

public class Attribute implements Serializable {
	final static long serialVersionUID = 4901754988275805381L;

	BitVector attributeMask = new BitVector();

	private String identifier = "";
	private String description = "";
	private boolean freeValue = false;
	private boolean multiValue = false;
	private boolean backreference;


	public Attribute() {
	}


	public void setIdentifier(String newIdentifier) {
		identifier = newIdentifier;
	}


	public void setDescription(String newDescription) {
		description = newDescription;
	}


	public void setFreeValue(boolean newFreeValue) {
		freeValue = newFreeValue;
	}


	public void setMultiValue(boolean newMultiValue) {
		multiValue = newMultiValue;
	}


	public void setBackreference(boolean newBackreference) {
		backreference = newBackreference;
	}


	public String getIdentifier() {
		return identifier;
	}


	public String getDescription() {
		return description;
	}


	public boolean isFreeValue() {
		return freeValue;
	}


	public boolean isMultiValue() {
		return multiValue;
	}


	public boolean isEqual(Attribute att) {
		if (
				(att.identifier.equals(identifier)) &&
				(att.freeValue == freeValue) &&
				(att.isMultiValue() == multiValue)
				) {
			return true;
		}
		return false;
	}


	public boolean isBackreference() {
		return backreference;
	}


	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}


	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
		if (attributeMask == null) {
			attributeMask = new BitVector();
		}
	}

        public String toString() {
          return identifier;
        }
}
