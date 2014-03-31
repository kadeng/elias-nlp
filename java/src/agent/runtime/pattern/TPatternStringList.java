/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.pattern;
import java.io.Serializable;

import java.util.Arrays;

import cern.colt.*;
import cern.colt.function.IntComparator;

/**
 *  List of OR'ed Strings. The Array in which they are stored gets sorted by the
 *  corresponding hash-codes of the Strings. Thus, a search in the list can be
 *  done in Time proportional to O(log(elements)) with a relatively small
 *  constant factor. An additional mask that is an OR'ed value of all hashcodes
 *  allows very fast rejection of those Strings which are too small, too large,
 *  and for many other cases where they can't match - especially useful for
 *  small word-lists
 *
 *@author     Kai Londenberg
 *@created    21. Januar 2002
 */
public final class TPatternStringList extends Terminal implements Serializable {
	final static long serialVersionUID = -1644988648050805493L;
	/**
	 *  Array of OR'ed Strings
	 */
	String[] patterns;
	/**
	 *  Array of their corresponding hashcodes.
	 */
	int[] hashcodes;
	int min, max;
	/**
	 *  Amount of elements. Equal to patterns.length or hashcodes.length
	 */
	int count;


	// private class SortableStringList

	/**
	 *  Constructor of a TPatternStringList
	 *
	 *@param  patterns_  Array of Strings to be wrapped. Warning: The array will be
	 *      empty after this call !
	 */
	public TPatternStringList(String[] patterns_) {
		super();
		patterns = patterns_;
		count = patterns.length;
		hashcodes = new int[count];
		for (int i = 0; i < count; i++) {
			hashcodes[i] = patterns[i].hashCode();
		}
		SortableStringList sortable = new SortableStringList();
		GenericSorting.quickSort(0, count, sortable, sortable);
		min = hashcodes[0];
		max = hashcodes[count - 1];
	}


	public int getMinSize() {
		return 1;
	}


	public String toString() {
		return "TPatternStringList of length " + count;
	}


	public final int match(DefaultTokenizedInput input, int pos) {
		int code = input.tokenHashcode[pos];
		if ((code < min) || (code > max)) {
			return -1;
		}
		int f = Arrays.binarySearch(hashcodes, code);
		if (f < 0) {
			return -1;
		}
		if (patterns[f].equals(input.tokenStrs[pos])) {
			return 1;
		}
		for (int i = f - 1; f >= 0; f--) {
			if (hashcodes[i] != code) {
				break;
			}
			if (patterns[i] == input.tokenStrs[pos]) {
				return 1;
			}
		}
		for (int i = f + 1; f < count; f++) {
			if (hashcodes[i] != code) {
				break;
			}
			if (patterns[i] == input.tokenStrs[pos]) {
				return 1;
			}
		}
		return -1;
	}


	private final class SortableStringList implements IntComparator, Swapper {

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
			String ts = patterns[index1];
			hashcodes[index1] = hashcodes[index2];
			patterns[index1] = patterns[index2];
			patterns[index2] = ts;
			hashcodes[index2] = ti;
		}
	}

        public String toJSGFString() {
          StringBuffer r = new StringBuffer();
          for (int i=0;i<this.patterns.length;i++) {
            r.append(patterns[i]);
            r.append("|");
          }
          return r.toString();
        }

}
