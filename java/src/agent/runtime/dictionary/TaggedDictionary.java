/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.dictionary;

import java.util.*;
import java.io.*;
import elias.agent.runtime.pattern.DefaultTokenizedInput;

public final class TaggedDictionary implements java.io.Serializable {

	final static long serialVersionUID = -2004934926328692694L;

	private static HashMap dictionaryMap = new HashMap(100);

	public DictionaryEntry UNKNOWN;
	public DictionaryEntry NUMBER;
	public DictionaryEntry UNKNOWN_WORD_UPPER;
	public DictionaryEntry UNKNOWN_WORD_LOWER;
	public DictionaryEntry CHAR;

	/**
	 *  Amount of bits reserved for meaning count
	 */
	int interpretationCountBits;

	/**
	 *  Amount of bits reserved for interpretation data length (in int entries)
	 */
	int interpretationLengthBits;

	/**
	 *  Maximum count of free values per entry.
	 */
	int maxFreeValueCount = -1;

	/**
	 *  Amount of bits reserved for value count within entry
	 */
	int valueCountBits;

	/**
	 *  Amount of bits neccessary to store index into values array
	 */
	int valuePointerBits;

	/**
	 *  Amount of bits neccessary to store index of word class for interpretation
	 */
	int wordClassBits;

	/**
	 *  Amount of word classes.. short for wordClasses.length
	 */
	int wordClassCount;

	/**
	 *  The dictionary entries, consisting of an UTF-8 encoded string, and binary
	 *  encoded data to be interpreted by a DictionaryEntryInterpreter. This binary
	 *  encoded data gets produced in TaggedDictionaryBuilder
	 */
	DictionaryEntry entries[];

	/**
	 *  Hash codes of the strings of the entries. A short statistic showed, that
	 *  these are unique in about 99% of all cases for 450000 dictionary entries.
	 */
	int hashCodes[];
	AttributeValue values[];

	/**
	 *  skipHeader[vCount] contains the offset of the first interpretation of a
	 *  given entry, for a given number vCount of free value entries.
	 */
	int skipHeader[];

	WordClass wordClasses[];

	Map wordClassMap;

	private transient Stack interpreterPool = new Stack();
	private transient Stack tokenizedInputExtensionPool = new Stack();


	// maps Word class names to Word Class entries (and as such to their indices)

	/**
	 *  A Tagged dictionary may be created only by using the tagged dictionary
	 *  builder, or by loading it from a stream (see readTaggedDictionary)
	 */
	TaggedDictionary() {
	}


	public static TaggedDictionary getSingletonDictionary(String filename) throws Exception {
		TaggedDictionary tgd = (TaggedDictionary) dictionaryMap.get(filename);
		if (tgd != null) {
			return tgd;
		}
		tgd = loadFrom(new File(filename));
		dictionaryMap.put(filename, tgd);
		return tgd;
	}


	public static void removeSingletonDictionary(String filename) {
		dictionaryMap.remove(filename);
	}


	public static void removeAllSingletonDictionaries() {
		dictionaryMap.clear();
	}


	public static TaggedDictionary readTaggedDictionary(ObjectInputStream in) throws Exception {
		TaggedDictionary res = new TaggedDictionary();
		res.readFrom(in);
		return res;
	}


	public static WordClass[] loadWordClassesFrom(File f) throws Exception {
		FileInputStream fi = null;
		BufferedInputStream bi = null;
		ObjectInputStream oin = null;
		WordClass result[];
		try {
			fi = new FileInputStream(f);
			bi = new BufferedInputStream(fi, 1000000);
			oin = new ObjectInputStream(bi);
			result = (WordClass[]) oin.readObject();
		} finally {
			try {
				oin.close();
			} catch (Exception exx) {
			}
			try {
				bi.close();
			} catch (Exception exx) {
			}
			try {
				fi.close();
			} catch (Exception exx) {
			}
		}
		return result;
	}


	public static TaggedDictionary loadFrom(File f) throws Exception {
		FileInputStream fi = null;
		BufferedInputStream bi = null;
		ObjectInputStream oin = null;
		TaggedDictionary result = null;
		try {
			fi = new FileInputStream(f);
			bi = new BufferedInputStream(fi, 1000000);
			oin = new ObjectInputStream(bi);
			result = new TaggedDictionary();
			result.readFrom(oin);
		} finally {
			try {
				oin.close();
			} catch (Exception exx) {
			}
			try {
				bi.close();
			} catch (Exception exx) {
			}
			try {
				fi.close();
			} catch (Exception exx) {
			}
		}
		return result;
	}


	private static void msg(String txt) {
		System.out.println(txt);
		System.out.flush();
	}


	public DictionaryEntry getUnknownEntry() {
		return UNKNOWN;
	}


	public DictionaryEntry lookup(String word) {
		try {
			return lookup(word.getBytes("UTF-8"), word.hashCode(), word.length());
		} catch (UnsupportedEncodingException uee) {
		}
		// impossible, UTF-8 has to be supported
		return null;
	}


	public DictionaryEntry lookup(byte utfBytes[], int hashCode, int charLength) {
		int res = Arrays.binarySearch(hashCodes, hashCode);
		if (res < 0) {
			return null;
		}
		DictionaryEntry entry = entries[res];
		if (Arrays.equals(entry.entryBytes, utfBytes)) {
			return entry;
		}
		int pos = res - 1;
		while ((pos >= 0) && (hashCodes[pos] == hashCode)) {
			entry = entries[pos];
			if (Arrays.equals(entry.entryBytes, utfBytes)) {
				return entry;
			}
			--pos;
		}
		pos = res + 1;
		while ((pos < entries.length) && (hashCodes[pos] == hashCode)) {
			entry = entries[pos];
			if (Arrays.equals(entry.entryBytes, utfBytes)) {
				return entry;
			}
			++pos;
		}
		return UNKNOWN;
	}


	public void writeTo(ObjectOutputStream out) throws java.io.IOException {
		checkBuild();
		out.writeObject(wordClasses);
		out.writeObject(wordClassMap);
		out.writeObject(skipHeader);
		out.writeObject(values);
		out.writeInt(interpretationCountBits);
		out.writeInt(interpretationLengthBits);
		out.writeInt(valueCountBits);
		out.writeInt(valuePointerBits);
		out.writeInt(wordClassBits);
		out.writeInt(wordClassCount);
		out.writeInt(maxFreeValueCount);
		int entryCount = entries.length;
		out.writeInt(entryCount);
		UNKNOWN.writeTo(out);
		UNKNOWN_WORD_UPPER.writeTo(out);
		UNKNOWN_WORD_LOWER.writeTo(out);
		CHAR.writeTo(out);
		NUMBER.writeTo(out);
		for (int i = 0; i < entryCount; i++) {
			out.writeInt(hashCodes[i]);
			entries[i].writeTo(out);
		}
	}


	public DictionaryEntryInterpreter newEntryInterpreter() {
		synchronized (interpreterPool) {
			if (!interpreterPool.empty()) {
				return (DictionaryEntryInterpreter) interpreterPool.pop();
			}
		}
		return new DictionaryEntryInterpreter(this);
	}


	public TokenizedInputDictionaryExtension newTokenizedInputExtension(DefaultTokenizedInput input) {
		synchronized (tokenizedInputExtensionPool) {
			if (!tokenizedInputExtensionPool.empty()) {
				TokenizedInputDictionaryExtension result = (TokenizedInputDictionaryExtension) tokenizedInputExtensionPool.pop();
				result.setInput(input);
				return result;
			}
		}
		return new TokenizedInputDictionaryExtension(this, input);
	}


	void freeTokenizedInputExtension(TokenizedInputDictionaryExtension e) {
		if (tokenizedInputExtensionPool.size() > 1000) {
			return;
		}
		// allow them to be collected
		e.tokenizedInput = null;
		tokenizedInputExtensionPool.push(e);
	}


	void freeEntryInterpreter(DictionaryEntryInterpreter e) {
		if (interpreterPool.size() > 1000) {
			return;
		}
		// allow them to be collected
		interpreterPool.push(e);
	}


	void readFrom(ObjectInputStream in) throws Exception {
		wordClasses = (WordClass[]) in.readObject();
		wordClassMap = (Map) in.readObject();
		skipHeader = (int[]) in.readObject();
		values = (AttributeValue[]) in.readObject();
		interpretationCountBits = in.readInt();
		interpretationLengthBits = in.readInt();
		valueCountBits = in.readInt();
		valuePointerBits = in.readInt();
		wordClassBits = in.readInt();
		wordClassCount = in.readInt();
		maxFreeValueCount = in.readInt();
		int entryCount = in.readInt();
		entries = new DictionaryEntry[entryCount];
		hashCodes = new int[entryCount];
		Runtime rt = Runtime.getRuntime();
		int i = 0;
		try {
			UNKNOWN = DictionaryEntry.readDictionaryEntryFrom(in);
			UNKNOWN_WORD_UPPER = DictionaryEntry.readDictionaryEntryFrom(in);
			UNKNOWN_WORD_LOWER = DictionaryEntry.readDictionaryEntryFrom(in);
			CHAR = DictionaryEntry.readDictionaryEntryFrom(in);
			NUMBER = DictionaryEntry.readDictionaryEntryFrom(in);

			for (i = 0; i < entryCount; i++) {
				hashCodes[i] = in.readInt();
				entries[i] = DictionaryEntry.readDictionaryEntryFrom(in);
			}
		} catch (OutOfMemoryError oem) {
			oem.printStackTrace();
			throw new Exception(oem.getMessage());
		}
		interpreterPool = new Stack();
		tokenizedInputExtensionPool = new Stack();
		checkBuild();
	}


	void checkBuild() {
		// a small check, and cleaning up
		if (interpretationCountBits < 1) {
			throw new RuntimeException("Dictionary contains errors");
		}
		if (interpretationLengthBits < 1) {
			throw new RuntimeException("Dictionary contains errors");
		}
		if (valueCountBits < 1) {
			throw new RuntimeException("Dictionary contains errors");
		}
		if (valuePointerBits < 1) {
			throw new RuntimeException("Dictionary contains errors");
		}
		if (wordClassBits < 1) {
			throw new RuntimeException("Dictionary contains errors");
		}
		if (wordClassCount < 1) {
			throw new RuntimeException("Dictionary contains errors");
		}
		if (entries == null) {
			throw new RuntimeException("Dictionary contains errors");
		}
		if (entries.length < 1) {
			throw new RuntimeException("Dictionary contains errors");
		}
		if (entries[0] == null) {
			throw new RuntimeException("Dictionary contains errors");
		}
		if (skipHeader == null) {
			throw new RuntimeException("Dictionary contains errors");
		}
		if (values == null) {
			throw new RuntimeException("Dictionary contains errors");
		}
		if (wordClassMap == null) {
			throw new RuntimeException("Dictionary contains errors");
		}
		if (wordClasses == null) {
			throw new RuntimeException("Dictionary contains errors");
		}
		if (hashCodes == null) {
			throw new RuntimeException("Dictionary contains errors");
		}
		if (maxFreeValueCount < 0) {
			throw new RuntimeException("Dictionary contains errors");
		}
	}

}
