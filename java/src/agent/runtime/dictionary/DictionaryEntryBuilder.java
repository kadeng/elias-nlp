/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.dictionary;

import java.util.*;
import elias.agent.authoring.dictionarybuilder.*;

class DictionaryEntryBuilder {
	int maxICount = 0;
	int totalMergedInterpretations = 0;

	private int vCount;
	private int iCount;
	private int resultICount;
	private int iLength;
	private BitVector data;
	private DictionaryEntry entry;
	private DictionaryBaseEntry base;
	private WordClass wordClasses[] = new WordClass[0];
	private BitVector interpretations[] = new BitVector[0];
	private WordClass resultWordClasses[] = new WordClass[0];
	private BitVector resultInterpretations[] = new BitVector[0];
	private BitVector header = new BitVector();
	private ArrayList freeValues = new ArrayList();

	private TaggedDictionary dictionary;
	private Map valueIndexMap;
	private int wordClassBits;
	private int wordClassCount;
	private int valuePointerBits;
	private int valuePointersOffset;
	private int valueCountBits;
	private int valueCountOffset;
	private int interpretationCountBits;
	private int interpretationCountOffset;
	private int interpretationLengthBits;
	private int interpretationLengthOffset;
	private int maxMultiValueAttributeCount = 0;


	/**
	 *  <PRE>
	 * Entry data. This data consists of two main parts:
	 *
	 * Header: The information global to each entry.
	 *    The header consist of:
	 *      - A bit-field with a length equal to the amount of word classes
	 *        (dictionary.wordClassCount). Every bit represents a word class.
	 *        If this entry has an interpretation which allows it to be treated as
	 *        a given word class, the corresponding bit in the bitfield is set.
	 *      - A bit field with a length equal to dictionary.interpretationCountBits.
	 *        This field is to be interpreted as the amount of interpretations.
	 *        It will be referred to as iCount
	 *      - A bit field with a length equal to dictionary.interpretationLengthBits.
	 *        This field is to be interpreted as the length (in integer entries) of
	 *        an interpretation entry.
	 *        It will be referred to as iLength
	 *      - A bit field with a length equal to dictionary.valueCountBits
	 *        This field is to be interpreted as the amount of distinct values found
	 *        in the free value attributes of the entry.
	 *        It will be referred to as vCount
	 *      - vCount bit fields with a length equal to dictionary.valueOffsetBits
	 *        These fields hold indices into the dictionary.values[] array, and map a given
	 *        free value entry to the corresponding ValueAttribute.
	 *
	 *        Following this header, are iCount interpretation data fields. These are all
	 *        aligned to integer boundaries so that a fast access is possible. Obviously, the
	 *        offset of the first interpretation gets determined by vCount. Therefore, if vCount is
	 *        given, dictionary.skipHeader[vCount] contains the pre-calculated offset position of the first
	 *        interpretation. The offset of interpretation i may be calculated as:
	 *        Offset = dictionary.skipHeader[vCount] + i * iLength.
	 *
	 *        A given Interpretation consists of:
	 *
	 *        - A bit field of the length dictionary.wordClassBits.
	 *          This field is the index of the word class for this interpretation. This
	 *          index will be referred to as wcIDX. The word class for this interpretation
	 *          is wordClass = dictionary.wordClasses[wcIDX]
	 *        - a bit field with a size of wordClass.bitLength bits.
	 *          This contains the tag values to be interpreted according to the word class
	 *        - a bit field with a size of vCount - each bit represents a given free value
	 *          for this dictionary entry, and is set if this interpretation contained the value,
	 *          and is cleared if not.
	 *  </PRE>
	 *
	 *@param  dictionary     Description of Parameter
	 *@param  valueIndexMap  Description of Parameter
	 */

	DictionaryEntryBuilder(TaggedDictionary dictionary, Map valueIndexMap) {
		this.valueIndexMap = valueIndexMap;
		this.dictionary = dictionary;
		this.wordClassBits = dictionary.wordClassBits;
		this.wordClassCount = dictionary.wordClassCount;
		this.interpretationCountBits = dictionary.interpretationCountBits;
		this.interpretationCountOffset = wordClassCount;
		this.interpretationLengthBits = dictionary.interpretationLengthBits;
		this.interpretationLengthOffset = interpretationCountOffset + interpretationCountBits;
		this.valueCountBits = dictionary.valueCountBits;
		this.valueCountOffset = interpretationLengthOffset + interpretationLengthBits;
		this.valuePointerBits = dictionary.valuePointerBits;
		this.valuePointersOffset = valueCountOffset + valueCountBits;
	}


	DictionaryEntry buildDictionaryEntry(DictionaryBaseEntry base, int entryIndex) throws Exception {
		this.base = base;
		entry = new DictionaryEntry();
		entry.entryBytes = base.toString().getBytes("UTF-8");
		data = new BitVector();
		iCount = base.getInterpretationCount();
		ensureMaxICount();
		resultICount = 0;
		header.clear();
		maxMultiValueAttributeCount = 0;
		for (int i = 0; i < iCount; i++) {
			wordClasses[i] = (WordClass) dictionary.wordClassMap.get(base.getInterpretationWordClass(i));
			if (wordClasses[i] == null) {
				throw new UnknownWordClassException(base.getInterpretationWordClass(i));
			}
			if (wordClasses[i].multiValueAttributes.length > maxMultiValueAttributeCount) {
				maxMultiValueAttributeCount = wordClasses[i].multiValueAttributes.length;
			}
			interpretations[i].clear();
			header.OR(wordClasses[i].getWordClassBits());
		}
		freeValues.clear();
		int maxTagBitSize = 0;
		for (int i = 0; i < iCount; i++) {
			String attributes[] = base.getInterpretationAttributes(i);
			String values[] = base.getInterpretationValues(i);
			WordClass wordClass = wordClasses[i];
			Tag tags[] = wordClass.getTags();
			Map tagMap = wordClass.getTagMap();
			int vlen = values.length;
			interpretations[i].setRange(0, wordClassBits, wordClass.getIndex());
			for (int v = 0; v < vlen; v++) {
				Attribute freeValueAttribute = (Attribute) wordClass.freeValueAttributesMap.get(attributes[v]);
				if (freeValueAttribute != null) {
					AttributeValue va = new AttributeValue(freeValueAttribute, values[v]);
					int freeValueIndex = this.getFreeValueIndex(va);
					interpretations[i].setBit(wordClassBits + tags.length + freeValueIndex);
					continue;
				}
				Tag tag = (Tag) tagMap.get(values[v]);
				if (tag == null) {
					continue;
				}
				interpretations[i].setBit(wordClassBits + tag.index);
			}
			int tagBitSize = tags.length;
			if (tagBitSize > maxTagBitSize) {
				maxTagBitSize = tagBitSize;
			}
		}
		int oldICount = iCount;
		// merge interpretations if possible
		combineInterpretations();
                totalMergedInterpretations += (oldICount - iCount);
		header.setRange(interpretationCountOffset, interpretationCountBits, iCount);
                vCount = freeValues.size();
		int maxBitIndex = wordClassBits + maxTagBitSize + vCount - 1;
		for (int i = 0; i < iCount; i++) {
			interpretations[i].enlarge(maxBitIndex);
		}
		iLength = BitVector.minIntSize(maxBitIndex + 1);
                header.setRange(interpretationLengthOffset, interpretationLengthBits, iLength);
                header.setRange(valueCountOffset, valueCountBits, vCount);
                int pos = this.valuePointersOffset;
		for (int i = 0; i < freeValues.size(); i++) {
			Integer IDX = (Integer) valueIndexMap.get(freeValues.get(i));
			if (IDX == null) {
				throw new Exception("Free value not found");
			}
			int idx = IDX.intValue();
                        if (idx>=dictionary.values.length) {
                          throw new Exception("Impossible !");
                        }
                        header.setRange(pos, valuePointerBits, idx);
                        pos += valuePointerBits;
                        AttributeValue freeValue = dictionary.values[idx];
                        if (freeValue.isBackreference()) {
                          ((BackreferenceAttributeValue)freeValue).addBackreference(entryIndex);
                        }
		}
		int dataSize = header.intSize() + iCount * iLength;
		int data[] = new int[dataSize];
		pos = header.copyInto(data, 0);
                for (int i = 0; i < iCount; i++) {
			pos += interpretations[i].copyInto(data, pos);
		}
		if (pos != dataSize) {
			throw new Exception("Assertion failure !");
		}
		entry.dataBitVector = data;
		return entry;
	}


	private int getFreeValueIndex(AttributeValue value) {
		for (int i = 0; i < freeValues.size(); i++) {
			if (freeValues.get(i).equals(value)) {
				return i;
			}
		}
		freeValues.add(value);
		return freeValues.size() - 1;
	}


	private void clearResultInterpretations() {
		for (int i = 0; i < iCount; i++) {
			resultInterpretations[i].clear();
		}
	}


	/**
	 *  Basic idea is, that two interpretations which differ just by one attribute
	 *  may be merged without loss of information. This may be iterated over all
	 *  attributes.
	 */
	private void combineInterpretations() {
		for (int a = 0; a < maxMultiValueAttributeCount; a++) {
                        clearResultInterpretations();
			resultICount = 0;
			for (int b = 0; b < iCount; b++) {
				BitVector baseInterpretation = interpretations[b];
                                WordClass baseWordClass = wordClasses[b];
                                if (baseWordClass.multiValueAttributes.length <= a) {
                                        interpretations[b] = resultInterpretations[resultICount];
					resultInterpretations[resultICount] = baseInterpretation;
					resultWordClasses[resultICount++] = baseWordClass;
					continue;
				}
                                BitVector blindAttributeMask = baseWordClass.multiValueAttributes[a].attributeMask;
                                int i=b+1;
                                for (;i < iCount; i++) {
                                        BitVector cmpInterpretation = interpretations[i];
					WordClass cmpWordClass = wordClasses[i];
					if (cmpWordClass != baseWordClass) {
                                  		continue;
					}
                                        if (baseInterpretation.equalToExceptMask(cmpInterpretation, blindAttributeMask)) {
					  cmpInterpretation.OR(baseInterpretation);
                                          break;
                                        }
				}
                                if (i>=iCount) {
                                  interpretations[b] = resultInterpretations[resultICount];
				  resultInterpretations[resultICount] = baseInterpretation;
				  resultWordClasses[resultICount++] = baseWordClass;
                                }
			}
                        BitVector tmp[] = interpretations;
			interpretations = resultInterpretations;
                        WordClass tmpwc[] = wordClasses;
                        wordClasses = resultWordClasses;
                        resultWordClasses = tmpwc;
			resultInterpretations = tmp;
			iCount = resultICount;
		}
	}

        private static void msg(String msg) {
          System.out.println(msg);
          System.out.flush();
        }

        private void debugCombineInterpretations() {
		msg("Merging interpretations of entry: " + this.base.toString());
                for (int a = 0; a < maxMultiValueAttributeCount; a++) {
                        msg("Interpretation count: " + iCount);
                        msg("Merge iteration: " + a);
                        clearResultInterpretations();
			resultICount = 0;
			for (int b = 0; b < iCount; b++) {
				BitVector baseInterpretation = interpretations[b];
                                WordClass baseWordClass = wordClasses[b];
                                msg("Compare base: " + baseWordClass.getIdentifier());
                                msg(baseInterpretation.toString());
                                if (baseWordClass.multiValueAttributes.length <= a) {
                                        msg("Multi value attribute " + a + " not present.");
					msg("Adding compare base to iteration result at position: " + resultICount);
                                        msg(baseInterpretation.toString());
                                        interpretations[b] = resultInterpretations[resultICount];
					resultInterpretations[resultICount] = baseInterpretation;
					resultWordClasses[resultICount++] = baseWordClass;
					continue;
				}
                                BitVector blindAttributeMask = baseWordClass.multiValueAttributes[a].attributeMask;
                                msg("Blind attribute: " + baseWordClass.multiValueAttributes[a].getIdentifier());
                                msg(blindAttributeMask.toString());
				int i = b + 1;
                                for (; i < iCount; i++) {
                                        BitVector cmpInterpretation = interpretations[i];
					WordClass cmpWordClass = wordClasses[i];
					if (cmpWordClass != baseWordClass) {
                                                msg("Interpretation " + i + " has different word class.");
						continue;
					}
                                        msg("Comparing to interpretation " + i);
					msg(cmpInterpretation.toString());
                                        if (baseInterpretation.equalToExceptMask(cmpInterpretation, blindAttributeMask)) {
					  cmpInterpretation.OR(baseInterpretation);
                                          msg("Merged to:");
                                          msg(cmpInterpretation.toString());
                                          break;
                                        }
				}
                                if (i>=iCount) {
                                  msg("Compare base has not been merged.");
                                  msg("Adding compare base to iteration result at position: " + resultICount);
                                  msg(baseInterpretation.toString());
				  interpretations[b] = resultInterpretations[resultICount];
				  resultInterpretations[resultICount] = baseInterpretation;
				  resultWordClasses[resultICount++] = baseWordClass;
                                } else {
                                  msg("Compare base has been merged, and will not be copied to result.");
                                }
			}
                        msg("Merge iteration finished.");
                        BitVector tmp[] = interpretations;
			interpretations = resultInterpretations;
                        WordClass tmpwc[] = wordClasses;
                        wordClasses = resultWordClasses;
                        resultWordClasses = tmpwc;
			resultInterpretations = tmp;
			iCount = resultICount;
		}
                msg("Merge finished.");
	}


	private void ensureMaxICount() {
		if (iCount >= maxICount) {
			// enlarge interpretation data buffer
			maxICount = iCount;
			wordClasses = new WordClass[iCount];
			// enlarge word class array
			resultWordClasses = new WordClass[iCount];
			BitVector newInterpretations[] = new BitVector[iCount];
			System.arraycopy(interpretations, 0, newInterpretations, 0, interpretations.length);
			for (int i = interpretations.length; i < iCount; i++) {
				newInterpretations[i] = BitVector.newInstance();
			}
			interpretations = newInterpretations;

			newInterpretations = new BitVector[iCount];
			System.arraycopy(resultInterpretations, 0, newInterpretations, 0, resultInterpretations.length);
			for (int i = resultInterpretations.length; i < iCount; i++) {
				newInterpretations[i] = BitVector.newInstance();
			}
			resultInterpretations = newInterpretations;
		}
	}
}
