/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.dictionary;

import java.util.*;
import elias.agent.authoring.dictionarybuilder.BuilderEvent;
import elias.agent.authoring.dictionarybuilder.BuildStoppedException;
import elias.agent.authoring.dictionarybuilder.BuilderListener;
import elias.agent.authoring.dictionarybuilder.AbstractBuilder;
import elias.agent.authoring.dictionarybuilder.DictionaryBaseEntry;
import elias.agent.authoring.dictionarybuilder.DictionaryBaseReader;
import java.io.*;

public class TaggedDictionaryBuilder extends AbstractBuilder {

	private final static double log2 = Math.log(2);
	BuilderEvent builderEvent = new BuilderEvent(this);
	HashMap wordClassMap;
	WordClass wordClasses[];
	ArrayList valueList = new ArrayList();
	TreeMap valueIndices = new TreeMap();
	DictionaryBaseReader dictBase;
	int maxMeaningCount;
	int meaningCount = 0;
	int maxFreeValueCount = 0;
	int entryCount;
	int uniqueHashCodeCount = 0;
	int totalFreeValueEntryCount = 0;

	TaggedDictionary dictionary = new TaggedDictionary();
	int step = 0;

	private HashSet currentFreeValues = new HashSet(100);
	private int buffer[] = new int[10000];
	private int bufPos = 0;
	private Object currentFreeValuesArray[];
	private int minWCBitSize = 100000;
	private int maxWCBitSize = 0;
	private transient Vector builderListeners;

	private StringBuffer log = new StringBuffer();
	private StringBuffer statusMessage = new StringBuffer();
	private File targetFile;


	public TaggedDictionaryBuilder(DictionaryBaseReader initializedSortedDictionaryBase, WordClass wordClasses[], File targetFile) {
		this.wordClasses = wordClasses;
		this.targetFile = targetFile;
		dictBase = initializedSortedDictionaryBase;
		wordClassMap = new HashMap(wordClasses.length, 0.1f);
		int wordClassBits = minBitSize(wordClasses.length - 1);
                for (int i = 0; i < wordClasses.length; i++) {
			wordClasses[i].getWordClassBits().enlarge(wordClasses.length);
			minWCBitSize = (wordClasses[i].bitLength > minWCBitSize) ? minWCBitSize : wordClasses[i].bitLength;
			maxWCBitSize = (wordClasses[i].bitLength < maxWCBitSize) ? maxWCBitSize : wordClasses[i].bitLength;
			wordClassMap.put(wordClasses[i].getIdentifier(), wordClasses[i]);
			Tag tags[] = wordClasses[i].getTags();
			for (int t = 0; t < tags.length; t++) {
				Attribute att = tags[t].getAttribute();
				att.attributeMask.setBit(wordClassBits + t);
			}
		}
		dictionary = new TaggedDictionary();
                dictionary.wordClassBits = wordClassBits;
		dictionary.wordClasses = wordClasses;
		dictionary.wordClassMap = wordClassMap;
	}


	public static void main(String args[]) {
		System.out.println("" + minBitSize(7));
	}


	static int minBitSize(int value) {
		double dvalue = (double) (value + 1);
		double retval = Math.ceil(Math.log(dvalue) / log2);
		return ((int) retval);
	}


	private static int minIntSize(int bits) {
		return (int) Math.ceil(((double) bits) / 32d);
	}


	// step 1
	public void buildValues() throws Exception {
		try {
			dictBase.openReader();
			DictionaryBaseEntry entry = dictBase.readDictionaryBaseEntry();
			HashSet valueSet = new HashSet(200);
			log("Building free value set.");
			log("Number of Word classes: " + wordClasses.length);
			while (entry != null) {
				int mc = entry.getInterpretationCount();
				meaningCount += mc;
				maxMeaningCount = (mc < maxMeaningCount) ? maxMeaningCount : mc;
				entryCount++;
				valueSet.clear();
				String wordClassID[] = entry.getWordClassIDs();
				if ((entryCount % 1000) == 0) {
					msg("Building free value set. Reading entry " + entryCount);
				}
				for (int k = 0; k < mc; k++) {
					String attributes[] = entry.getInterpretationAttributes(k);
					String values[] = entry.getInterpretationValues(k);
					WordClass wordClass = (WordClass) wordClassMap.get(wordClassID[k]);
					if (wordClass == null) {
						throw new Exception("Unknown word class: " + wordClassID[k]);
					}
					int vcount = values.length;
					for (int i = 0; i < vcount; i++) {
						Attribute att = (Attribute) wordClass.freeValueAttributesMap.get(attributes[i]);
						if (att == null) {
							continue;
							// no free value attribute
						}
						AttributeValue value = AttributeValue.newInstance(att, values[i]);
						valueSet.add(value);
						Object idx = valueIndices.get(value);
						if (idx == null) {
							idx = new Integer(valueList.size());
							valueIndices.put(value, idx);
							valueList.add(value);
						}
					}
				}
				if (valueSet.size() > maxFreeValueCount) {
					maxFreeValueCount = valueSet.size();
				}
				totalFreeValueEntryCount += valueSet.size();
				entry = dictBase.readDictionaryBaseEntry();
			}
			dictionary.values = new AttributeValue[valueList.size()];
			valueList.toArray(dictionary.values);
			valueList = null;
			log("Value set built. Calculating statistics:");
			int vcount = dictionary.values.length;
			int vbits = minBitSize(vcount);
			dictionary.valuePointerBits = vbits;
			int mmbits = minBitSize(maxMeaningCount);
			dictionary.interpretationCountBits = mmbits;
			int mcbits = minBitSize(maxFreeValueCount);
			dictionary.valueCountBits = mcbits;
                        dictionary.maxFreeValueCount = maxFreeValueCount;
			dictionary.wordClassBits = minBitSize(wordClasses.length - 1);
			dictionary.wordClassCount = wordClasses.length;
			dictionary.interpretationLengthBits = minBitSize(BitVector.minIntSize(dictionary.wordClassBits + maxWCBitSize + maxFreeValueCount));
			dictionary.skipHeader = new int[maxFreeValueCount + 1];
			int baseSkip = dictionary.wordClassCount + dictionary.interpretationCountBits +
					dictionary.interpretationLengthBits + dictionary.valueCountBits;
			for (int i = 0; i < maxFreeValueCount + 1; i++) {
				dictionary.skipHeader[i] = BitVector.minIntSize(baseSkip + (i * dictionary.valuePointerBits));
			}
			float averageValuesPerEntry = ((float) totalFreeValueEntryCount) / ((float) entryCount);
			int averageMeaningCount = 1 + (meaningCount / entryCount);
			log("Dictionary entry count: " + entryCount);
			log("Total number of entry interpretations: " + meaningCount);
			log(" Maximum number of interpretations per entry: " + maxMeaningCount + " - neccessaey bits: " + mmbits);
			log("Total number of distinct free attribute values: " + vcount);
			log(" Neccessary index bits into free values: " + vbits);
			log(" Maximum number of free values per entry: " + maxFreeValueCount);
			log("Word class count: " + wordClasses.length);
			log("Maximum interpretation size: " + BitVector.minIntSize(maxWCBitSize + maxFreeValueCount) + " int values.");
			log("\n----------------------------------");
			log("Average number of free values per entry: " + averageValuesPerEntry);
			log("Average number of meanings per entry: " + ((float) meaningCount) / ((float) entryCount));
			log("----------------------------------");
		} finally {
			dictBase.closeReader();
		}
	}


	public void run() {
		try {
			this.fireBuildStarted(builderEvent);
			buildValues();
			buildEntries();
			dictionary.checkBuild();
			saveDictionary();
		} catch (BuildStoppedException ste) {
			log("Build stopped.");
		} catch (Exception exx) {
			log("Exception: " + exx.getClass().getName() + ": " + exx.getMessage());
			log("Look at console output for stack trace");
			exx.printStackTrace();
		} finally {
			this.fireBuildFinished(builderEvent);
		}
	}


	private void buildEntries() throws Exception {
		try {
			dictBase.openReader();
			DictionaryEntryBuilder entryBuilder = new DictionaryEntryBuilder(dictionary, valueIndices);
			DictionaryBaseEntry bEntry = dictBase.readDictionaryBaseEntry();
			log("Allocating arrays");
			dictionary.hashCodes = new int[entryCount];
			dictionary.entries = new DictionaryEntry[entryCount];
			System.gc();
			Runtime rt = Runtime.getRuntime();
			long usedStartMem = rt.totalMemory() - rt.freeMemory();
			log("Used memory: " + (usedStartMem / (1024 * 1024)) + "MB");
			log("Started building dictionary entries.");
			int pos = 0;
                        dictionary.UNKNOWN = entryBuilder.buildDictionaryEntry(DictionaryBaseEntry.UNKNOWN, -1);
                        dictionary.UNKNOWN_WORD_UPPER = entryBuilder.buildDictionaryEntry(DictionaryBaseEntry.UNKNOWN_WORD_UPPER, -1);
                        dictionary.UNKNOWN_WORD_LOWER = entryBuilder.buildDictionaryEntry(DictionaryBaseEntry.UNKNOWN_WORD_LOWER, -1);
			dictionary.NUMBER =  entryBuilder.buildDictionaryEntry(DictionaryBaseEntry.NUMBER, -1);
                        dictionary.CHAR = entryBuilder.buildDictionaryEntry(DictionaryBaseEntry.CHAR, -1);
                        while (bEntry != null) {
				dictionary.hashCodes[pos] = bEntry.hashCode();
				dictionary.entries[pos] = entryBuilder.buildDictionaryEntry(bEntry, pos);
				pos++;
                                if ((pos % 1000) == 0) {
					long usedMem = rt.totalMemory() - rt.freeMemory();
					msg("Building entries - " + (100 * pos) / entryCount + "% (" + pos + " of " + entryCount + ") - Used memory: " + (usedMem / (1024l * 1024l)) + "MB");
				}
				bEntry = dictBase.readDictionaryBaseEntry();
			}
			log("Finished building dictionary entries.");
			long usedMem = rt.totalMemory() - rt.freeMemory();
			log("Merged " + entryBuilder.totalMergedInterpretations + " interpretations.");
			log("Total used memory:" + usedMem);
			log("Difference: " + ((usedMem - usedStartMem) / (1024l * 1024l)) + "MB");
		} finally {
			dictBase.closeReader();
		}
	}


	private void saveDictionary() throws Exception {
		ObjectOutputStream out = null;
		FileOutputStream fout = null;
		BufferedOutputStream bout = null;
		log("Saving dictionary to <" + targetFile.toString() + ">");
		try {
			fout = new FileOutputStream(targetFile);
			bout = new BufferedOutputStream(fout);
			out = new ObjectOutputStream(bout);
			dictionary.writeTo(out);
			log("Dictionary saved successfully.");
		} finally {
			try {
				out.close();
			} catch (Exception exx) {
			}
			try {
				bout.close();
			} catch (Exception exx) {
			}
			try {
				fout.close();
			} catch (Exception exx) {
			}
		}

	}

}
