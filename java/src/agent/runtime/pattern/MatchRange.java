/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.pattern;

import java.util.Stack;

public final class MatchRange implements java.io.Serializable, java.lang.Cloneable, elias.agent.runtime.api.Range {
	private final static Stack pool = new Stack();

	private int start;
	private int end;


	private MatchRange(int start, int end) {
		this.start = start;
		this.end = end;
	}


	public static MatchRange newMatchRange(int start, int end) {
		MatchRange res = null;
		synchronized (pool) {
			if (pool.isEmpty()) {
				return new MatchRange(start, end);
			}
			res = (MatchRange) pool.pop();
		}
		res.start = start;
		res.end = end;
		return res;
	}


	static void freeMatchRange(Object m) {
		if (pool.size() > 400) {
			return;
		}
		pool.push(m);
	}


	public int getStart() {
		return start;
	}


	public int getEnd() {
		return end;
	}


	public MatchRange duplicate() {
		return newMatchRange(start, end);
	}

}
