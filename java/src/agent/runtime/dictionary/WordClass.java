/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.dictionary;

import java.util.*;
import java.io.*;

public class WordClass implements Serializable {
            static final long serialVersionUID = 1892608715626964030L;

	int index;
        int indexTo;
	String identifier;
	String description;
	Tag[] tags;
	Map tagMap;
        Map freeValueAttributesMap;
        int bitLength = 0;
	Attribute[] attributes;
        Attribute[] multiValueAttributes;
	Attribute[] freeValueAttributes;
	WordClass parent;
        BitVector wordClassBits = new BitVector();

	public WordClass(int index, String identifier, String desc, Tag tags[], Attribute atts[], WordClass parent) {
		  this.index = index;
		  this.indexTo = index;
                  this.identifier = identifier;
		  this.description = desc;
		  this.tags = tags;
		  this.attributes = atts;
                  this.parent = parent;
		  int fc = 0;
                  int tc = 0;
                  int mc = 0;
                  wordClassBits.setBit(index);
                  WordClass currentWordClass = parent;
                  while (currentWordClass!=null) {
                    wordClassBits.setBit(currentWordClass.index);
                    if (currentWordClass.indexTo<index) currentWordClass.indexTo = index;
                    currentWordClass = currentWordClass.parent;
                  }
		  for (int i = 0; i < atts.length; i++) {
			if (atts[i].isFreeValue()) {
				++fc;
			}
                        if (atts[i].isMultiValue()) {
                          ++mc;
                        }
		  }
		  this.freeValueAttributes = new Attribute[fc];
                  this.multiValueAttributes = new Attribute[mc];
		  freeValueAttributesMap = new HashMap(fc, 0.1f);
                  fc = 0;
                  int mct = 0;
		  for (int i = 0; i < atts.length; i++) {
		  	if (atts[i].isFreeValue()) {
				freeValueAttributes[fc++] = atts[i];
		  	        freeValueAttributesMap.put(atts[i].getIdentifier(), atts[i]);
                        }
                        if (atts[i].isMultiValue()) {
                          multiValueAttributes[mct++] = atts[i];
                        }
		  }
                  tagMap = new HashMap(tags.length, 0.15f);
                  for (int i=0;i<tags.length;i++) {
                    tags[i].setIndex(i);
                    tagMap.put(tags[i].getIdentifier(), tags[i]);
                  }
                  bitLength = tags.length;
	}




	public WordClass getParent() {
		return parent;
	}

        public BitVector getWordClassBits() {
          return wordClassBits;
        }

	public int getIndex() {
		return index;
	}


	public String getIdentifier() {
		return identifier;
	}


	public String getDescription() {
		return description;
	}


	public Tag[] getTags() {
		return tags;
	}


	public Map getTagMap() {
		return tagMap;
	}


	public Attribute[] getAttributes() {
		return attributes;
	}


	public Attribute[] getFreeValueAttributes() {
		return freeValueAttributes;
	}


	public String toString() {
		return identifier;
	}


	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}


	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
	}


}
