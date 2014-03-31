/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.pattern;
import java.io.Serializable;

import cern.colt.*;
import cern.colt.function.IntComparator;

/**
 *  An OR-Combination of Sequences of Strings, which all have the same length in
 *  lexemes. (That is, the Sequences all have the same length)
 *
 *@author     Administrator
 *@created    26. Juni 2001
 */
public final class TPatternFixedLengthStringSequenceList extends Terminal implements Serializable {
	final static long serialVersionUID = -7509596139673200372L;
	int length;
	int count;
	TPatternStringSequence patterns[];
	int hashcodes[];
	int min;
	int max;

	// private class SortableStringList


	/**
	 *  Constructor for the TPatternFixedLengthStringSequenceList object
	 *
	 *@param  list           Array of TPatternStringSequence's to be wrapped.
	 *@exception  Exception  Gets thrown if the List is too small, or the Sequences
	 *      don't have the same sizes.
	 */
	public TPatternFixedLengthStringSequenceList(TPatternStringSequence list[]) throws Exception {
		patterns = list;
		if (list.length < 2) {
			throw new Exception("List is too small");
		}
		length = list[0].length;
		count = list.length;
		hashcodes = new int[count];
		for (int i = 0; i < count; i++) {
			if (list[i].length != length) {
				throw new Exception("Sequence Size must be fixed");
			}
			hashcodes[i] = list[i].hashcode;
		}
		SortableFixedLengthStringSequenceList sortable = new SortableFixedLengthStringSequenceList();
		GenericSorting.quickSort(0, count, sortable, sortable);
		min = hashcodes[0];
		max = hashcodes[count - 1];
	}


	public int getMinSize() {
		return length;
	}


	public String toString() {
		return "List of String Sequences of fixed size(" + length + ") count: " + count;
	}


	public final int match(DefaultTokenizedInput input, int pos) {
		int code = patterns[0].calcHashCode(input, pos);
		if ((code < min) || (code > max)) {
			return -1;
		}
		int f = java.util.Arrays.binarySearch(hashcodes, code);
		if (f < 0) {
			return -1;
		}
		if (patterns[f].match(input, pos) != -1) {
			return length;
		}
		for (int i = f - 1; f >= 0; f--) {
			if (hashcodes[i] != code) {
				break;
			}
			if (patterns[i].match(input, pos) != -1) {
				return length;
			}
		}
		for (int i = f + 1; f < count; f++) {
			if (hashcodes[i] != code) {
				return -1;
			}
			if (patterns[i].match(input, pos) != -1) {
				return length;
			}
		}
		return -1;
	}


	private final class SortableFixedLengthStringSequenceList implements IntComparator, Swapper {

		public int compare(int index1, int index2) {
			if (hashcodes[index1] > hashcodes[index2]) {
				return 1;
			}
			if (hashcodes[index1] < hashcodes[index2]) {
				return -1;
			}
			return 0;
		}


		public void swap(int index1, int index2) {
			int ti = hashcodes[index1];
			TPatternStringSequence ts = patterns[index1];
			hashcodes[index1] = hashcodes[index2];
			patterns[index1] = patterns[index2];
			patterns[index2] = ts;
			hashcodes[index2] = ti;
		}
	}


        public String toJSGFString() {
          StringBuffer r = new StringBuffer();
          for (int i=0;i<this.patterns.length;i++) {
            r.append(patterns[i].toJSGFString());
            r.append("|");
          }
          return r.toString();
        }


}
