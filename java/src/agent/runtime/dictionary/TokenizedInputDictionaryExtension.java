/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.dictionary;
import java.util.AbstractList;
import elias.agent.runtime.base.FastInputTokenizer;
import elias.agent.runtime.pattern.Token;
import elias.agent.runtime.pattern.DefaultTokenizedInput;

public final class TokenizedInputDictionaryExtension {
	DefaultTokenizedInput tokenizedInput;
	DictionaryEntry dictionaryEntry[];
	int tokenCount = 0;
	int capacity = 0;
	TaggedDictionary dictionary;
	DictionaryEntryInterpreter dictionaryEntryInterpreter[];


	public TokenizedInputDictionaryExtension(TaggedDictionary dictionary, DefaultTokenizedInput input) {
		this.dictionary = dictionary;
		tokenCount = input.getTokenCount();
		tokenizedInput = input;
		capacity = tokenCount;
		dictionaryEntry = new DictionaryEntry[tokenCount];
		dictionaryEntryInterpreter = new DictionaryEntryInterpreter[tokenCount];
		for (int i = 0; i < tokenCount; i++) {
			Token t = input.getToken(i);
			dictionaryEntry[i] = dictionary.lookup(t.getNormValue());
			if (dictionaryEntry[i] == null) {
				char startChar = input.toString().charAt(t.getFrom());
				dictionaryEntry[i] = FastInputTokenizer.classifyUnknown(startChar, t.getTo() - t.getFrom(), dictionary);
			}
			dictionaryEntryInterpreter[i] = dictionary.newEntryInterpreter();
			dictionaryEntryInterpreter[i].setEntry(dictionaryEntry[i]);
		}
	}


	private TokenizedInputDictionaryExtension() {
	}


	public TokenizedInputDictionaryExtension getReduction(boolean keep[], int newTokenCount, DefaultTokenizedInput reducedInput) {
		TokenizedInputDictionaryExtension result = new TokenizedInputDictionaryExtension();
		result.dictionary = dictionary;
		result.tokenCount = newTokenCount;
		result.tokenizedInput = reducedInput;
		result.capacity = newTokenCount;
		result.dictionaryEntry = new DictionaryEntry[newTokenCount];
		result.dictionaryEntryInterpreter = new DictionaryEntryInterpreter[newTokenCount];
		int pos = 0;
		for (int i = 0; i < tokenCount; i++) {
			if (!keep[i]) {
				continue;
			}
			result.dictionaryEntry[pos] = dictionaryEntry[i];
			result.dictionaryEntryInterpreter[pos++] = dictionaryEntryInterpreter[i];
		}
		return result;
	}


	public String getInfo(int idx) {
		return dictionaryEntryInterpreter[idx].getInfoString();
	}


	public void free() {
		dictionary.freeTokenizedInputExtension(this);
	}


	void setInput(DefaultTokenizedInput input) {
		ensureCapacity(input.getTokenCount());
		tokenCount = input.getTokenCount();
		tokenizedInput = input;
		for (int i = 0; i < tokenCount; i++) {
			dictionaryEntry[i] = dictionary.lookup(input.getToken(i).getNormValue());
			dictionaryEntryInterpreter[i].setEntry(dictionaryEntry[i]);
		}
	}


	private void ensureCapacity(int newCapacity) {
		if (newCapacity <= capacity) {
			return;
		}
		dictionaryEntry = new DictionaryEntry[newCapacity];
		DictionaryEntryInterpreter newDictionaryEntryInterpreter[] = new DictionaryEntryInterpreter[newCapacity];
		System.arraycopy(dictionaryEntryInterpreter, 0, newDictionaryEntryInterpreter, 0, dictionaryEntryInterpreter.length);
		for (int i = capacity; i < newCapacity; i++) {
			newDictionaryEntryInterpreter[i] = dictionary.newEntryInterpreter();
		}
		dictionaryEntryInterpreter = newDictionaryEntryInterpreter;
		capacity = newCapacity;
	}

}
