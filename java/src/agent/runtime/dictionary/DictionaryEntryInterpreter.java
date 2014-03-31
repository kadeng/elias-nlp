/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.dictionary;

import java.util.*;

/**
 *  Title: Elias Agent Description: Ihre Beschreibung Copyright: Copyright (c)
 *  1999 Company:
 *
 *@author     Kai Londenberg
 *@created    4. Maerz 2002
 *@version
 */

/**
 *  Title: Elias Agent Description: Ihre Beschreibung Copyright: Copyright (c)
 *  1999 Company:
 *
 *  Title: Elias Agent Description: Ihre Beschreibung Copyright: Copyright (c)
 *  1999 Company: Methods for efficiently interpreting a dictionary entrie's
 *  binary data structure.
 *
 *@author     Kai Londenberg
 *@created    4. Maerz 2002
 */
public final class DictionaryEntryInterpreter implements java.io.Serializable {
	static final long serialVersionUID = -8787886510458932558L;
	TaggedDictionary dictionary;
	// dictionary specific data
        WordClass wordClasses[];
	private int wordClassBits;
	private int wordClassCount;
        private int wordClassMask;
	private int valuePointerBits;
	private int valuePointersOffset;
	private int valueCountBits;
	private int valueCountOffset;
	private int interpretationCountBits;
	private int interpretationCountOffset;
	private int interpretationLengthBits;
	private int interpretationLengthOffset;

	// entry specific data
	DictionaryEntry entry = null;
	OffsetBitVector bitVector = new OffsetBitVector(null, 0);
        int data[];
	int iCount;
	int iLength;
        int dataLength;
	int headerSkip;
	AttributeValue freeValues[];
        int vCount;

	DictionaryEntryInterpreter(TaggedDictionary dictionary) {
		this.dictionary = dictionary;
                this.wordClasses = dictionary.wordClasses;
		this.wordClassBits = dictionary.wordClassBits;
		this.wordClassCount = dictionary.wordClassCount;
                this.wordClassMask = BitVector.keepStartTable[wordClassBits];
		this.interpretationCountBits = dictionary.interpretationCountBits;
		this.interpretationCountOffset = wordClassCount;
		this.interpretationLengthBits = dictionary.interpretationLengthBits;
		this.interpretationLengthOffset = interpretationCountOffset + interpretationCountBits;
		this.valueCountBits = dictionary.valueCountBits;
		this.valueCountOffset = interpretationLengthOffset + interpretationLengthBits;
		this.valuePointerBits = dictionary.valuePointerBits;
		this.valuePointersOffset = valueCountOffset + valueCountBits;
  /*              msg("wordClassBits=" + wordClassBits);
                msg("wordClassCount=" + wordClassCount);
                msg("wordClasses.length=" + wordClasses.length);
                msg("interpretationCountBits=" + interpretationCountBits);
                msg("interpretationCountOffset=" + interpretationCountOffset);
                msg("interpretationLengthBits=" + interpretationLengthBits);
                msg("interpretationLengthOffset=" + interpretationLengthBits);
                msg("valueCountBits=" + valueCountBits);
                msg("valueCountOffset=" + valueCountOffset);
                msg("valuePointerBits=" + valuePointerBits);
                msg("valuePointersOffset=" + valuePointersOffset);
                msg("dictionary.maxFreeValueCount=" + dictionary.maxFreeValueCount); */
                freeValues = new AttributeValue[dictionary.maxFreeValueCount];
	}

  private static void msg(String msg) {
    System.out.println(msg);
    System.out.flush();
  }


	public void setEntry(DictionaryEntry entry) {
		this.entry = entry;
                this.data = entry.dataBitVector;
                bitVector.setData(entry.dataBitVector, 0);
                iCount = bitVector.getRange(interpretationCountOffset, interpretationCountBits);
                iLength = bitVector.getRange(interpretationLengthOffset, interpretationLengthBits);
                vCount = bitVector.getRange(valueCountOffset, valueCountBits);
                if (iLength==0) {
                  System.err.println("Entry = <" + entry.toString() + ">");
                  System.err.println("iLength==0");
                  System.err.println("iCount = " + iCount);
                  System.err.println("vCount = " + vCount);
                  System.err.println("interpretationLengthOffset = " + interpretationLengthOffset);
                  System.err.println("interpretationLengthBits = " + interpretationLengthBits);
                  System.err.println("interpretationCountOffset = " + interpretationCountOffset);
                  System.err.println("interpretationCountBits = " + interpretationCountBits);
                  System.err.println("valueCountOffset = " + valueCountOffset);
                  System.err.println("valueCountBits = " + valueCountBits);

                }
                vCount = bitVector.getRange(valueCountOffset, valueCountBits);
                dataLength = entry.dataBitVector.length;
                for (int i=0;i<vCount;i++) {
                  int valueIdx = bitVector.getRange(valuePointersOffset + (this.valuePointerBits*i), valuePointerBits);
                  freeValues[i] = dictionary.values[valueIdx];
                }
                headerSkip = dictionary.skipHeader[vCount];
	}

        public boolean isOfWordClass(int wordClassIndex) {
              bitVector.setOffset(0);
              return bitVector.getBit(wordClassIndex);
        }

        public int getInterpretationCount() {
          return iCount;
        }

        public void setInterpretation(int idx) {
              bitVector.setOffset(headerSkip + (idx*iLength));
        }

        public int getInterpretationWordClassIndex() {
              return bitVector.getRange(0, wordClassBits);
        }

        public WordClass getInterpretationWordClass() {
          return wordClasses[bitVector.getRange(0, wordClassBits)];
        }

        // slow method for visualization
        public Map getInterpretationAsMap(int index) {
          setInterpretation(index);
          WordClass wc = getInterpretationWordClass();
          Tag tags[] = wc.getTags();
          HashMap resultMap = new HashMap();
          StringBuffer bits = new StringBuffer();
          for (int i=0;i<iLength;i++) {
            if (i>0) bits.append(",");
            bits.append(BitVector.bitString(data[bitVector.offset+i]));
          }
          resultMap.put("bits", bits.toString());
          for (int i=0;i<tags.length;i++) {
            if (bitVector.getBit(wordClassBits + i)) {
              Object value = resultMap.get(tags[i].getAttribute());
              if (value!=null) {
                value = new String(value.toString() + "," + tags[i].getIdentifier());
              } else {
                value = tags[i].getIdentifier();
              }
              resultMap.put(tags[i].getAttribute(), value);
            }
          }
          for (int i=0;i<vCount;i++) {
              if (bitVector.getBit(wordClassBits + tags.length + i)) {
                Object value = resultMap.get(freeValues[i].getAttribute());
                if (value!=null) {
                  value = new String(value.toString() + "," + freeValues[i].toString());
                } else {
                  value = freeValues[i].toString();
                }
                resultMap.put(freeValues[i].getAttribute(), value);
              }
          }
          return resultMap;
        }

        public String[] getInterpretationsAsStrings() {
          String result[] = new String[iCount];
          for (int i=0;i<iCount;i++) {
            setInterpretation(i);
            result[i] = getInterpretationWordClass().getIdentifier();
          }
          return result;
        }

        public String toString() {
          return entry.toString();
        }

        public String getInfoString() {
          StringBuffer result = new StringBuffer();
          result.append("Dictionary entry for <" + entry.toString() + ">\n");
          String wordClassStrs[] = getInterpretationsAsStrings();
          for (int i=0;i<iCount;i++) {
            result.append(wordClassStrs[i]);
            Map iMap = getInterpretationAsMap(i);
            Iterator iter = iMap.entrySet().iterator();
            while (iter.hasNext()) {
              Map.Entry entry = (Map.Entry) iter.next();
              result.append(" ");
              result.append(entry.getKey().toString());
              result.append("=");
              result.append(entry.getValue().toString());
            }
          }
          return result.toString();
        }

        public DictionaryEntry[] getBackreferences() {
          int count = 0;
          for (int i=0;i<vCount;i++) {
            if (freeValues[i].isBackreference()) {
              count += ((BackreferenceAttributeValue)freeValues[i]).backReferences.length;
            }
          }
          DictionaryEntry result[] = new DictionaryEntry[count];
          count = 0;
          for (int i=0;i<vCount;i++) {
            if (freeValues[i].isBackreference()) {
              BackreferenceAttributeValue bv = ((BackreferenceAttributeValue)freeValues[i]);
              int len= bv.backReferences.length;
              for (int b=0;b<len;b++) {
                result[count++] = dictionary.entries[bv.backReferences[b]];
              }
            }
          }
          return result;
        }

        /**
         * returns this interpreter to it's dictionary's interpreter pool
         */
        public void free() {
          dictionary.freeEntryInterpreter(this);
        }

        public boolean hasMatchingInterpretation(int matchMask[]) {
          if (matchMask.length>iLength) return false;
          int len = matchMask.length;
          int pos=headerSkip;
          if (len==1) {
            int cmp = matchMask[0];
            for (;pos<dataLength;pos++) {
              if ((data[pos] & cmp)==cmp) return true;
            }
            return false;
          }
          int i;
          while(pos<dataLength) { // pretty unlikely we get here ..
            for (i=0;i<len;i++) {
              int cmp = matchMask[i];
              if ((data[pos+i] & cmp)!=cmp) break;
            }
            if (i==len) return true;
            pos+=iLength;
          }
          return false;
        }

        public boolean hasMatchingInterpretation(int matchMask[], int wordClassIndex, int wordClassIndexTo) {
          if (matchMask.length>iLength) return false;
          int len = matchMask.length;
          int pos=headerSkip;
          if (len==1) {
            int cmp = matchMask[0];
            for (;pos<dataLength;pos++) {
              int wcIdx = data[pos] & wordClassMask;
              if ((wcIdx<wordClassIndex) || (wcIdx>wordClassIndexTo)) continue;
              if ((data[pos] & cmp)==cmp) return true;
            }
            return false;
          }
          int i;
          while(pos<dataLength) { // pretty unlikely we get here ..
            int wcIdx = data[pos] & wordClassMask;
            if ((wcIdx<wordClassIndex) || (wcIdx>wordClassIndexTo)) {
              pos+=len;
              continue;
            }
            for (i=0;i<len;i++) {
              int cmp = matchMask[i];
              if ((data[pos+i] & cmp)!=cmp) break;
            }
            if (i==len) return true;
            pos+=iLength;
          }
          return false;
        }

}
