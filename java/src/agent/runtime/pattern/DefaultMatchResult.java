/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.pattern;

import java.util.*;
import elias.agent.runtime.api.Range;
import elias.agent.runtime.api.MatchResult;
import elias.agent.runtime.api.TokenizedInput;

/**
 *  Instances of this class store the named results of a previous pattern match.
 *  By accessing the various getMatch(name[,..]) methods, it is possible to get
 *  the part of the input text which matched a given named sub-pattern. By
 *  default, the named sub-patterns "all", "pre" and "post" are defined, which
 *  allow to access all of the text ("all") which matched the pattern, the text
 *  which preceded the match ("pre"), and the text which followed the match
 *  ("post") respectively.
 *
 *@author     Kai Londenberg
 *@created    14. Dezember 2001
 */
public class DefaultMatchResult implements elias.agent.runtime.api.MatchResult {

	private static HashMap empty_map = new HashMap(0);
	private static String empty_matches[] = new String[0];
	private static DefaultMatchResult empty_result = new DefaultMatchResult();
	int start;
	int end;

	private HashMap ranges;
	private DefaultTokenizedInput input;


	DefaultMatchResult(PatternStructure pattern, Matcher matcher) {
		input = matcher.input;
		if (!matcher.clean_match(pattern, input)) {
			ranges = empty_map;
			return;
		}
		start = pattern.top.getStartOfMatchPosition(matcher);
		end = pattern.top.getEndOfMatchPosition(matcher);
		int c = 1;
		if ((input.tokenCount - end) > 0) {
			c++;
		}
		if (start > 0) {
			c++;
		}
		ranges = new HashMap(pattern.names + c);
		addNamedPattern(start, end, input, "all");
		if ((input.tokenCount - end) > 0) {
			addNamedPattern(end, input.tokenCount, input, "post");
		}
		if (start > 0) {
			addNamedPattern(0, start, input, "pre");
		}
		for (int i = start; i < end; i++) {
			int tidx = input.matchidx[i];
			if (tidx < 0) {
				continue;
			}
			Pattern p = pattern.patterns[tidx];
			while (p.parent != null) {
				p = p.parent;
				if (p.isNamed()) {
					addNamedPattern(p, matcher, input);
				}
			}
		}
	}


	private DefaultMatchResult() {
		ranges = empty_map;
		start = 0;
		end = 0;
	}


	public static DefaultMatchResult getEmptyMatchResult() {
		return empty_result;
	}


	public NamedString[] getMatchResults() {
		NamedString result[] = new NamedString[ranges.size()];
		Iterator iter = ranges.entrySet().iterator();
		int pos = 0;
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			MatchRange range = (MatchRange) entry.getValue();
			result[pos++] = new NamedString(entry.getKey().toString(), input.getOriginal(range.getStart(), range.getEnd()));
		}
		return result;
	}


	public String getMatch(String name) {
		MatchRange rng = (MatchRange) ranges.get(name);
		if (rng == null) {
			return "";
		}
		return input.getOriginal(rng.getStart(), rng.getEnd());
	}


	public Range getMatchRange(String name) {
		MatchRange rng = (MatchRange) ranges.get(name);
		if (rng == null) {
			return null;
		}
		return rng.duplicate();
	}


	public Iterator getMatchNames() {
		return ranges.keySet().iterator();
	}


	public TokenizedInput composeInput(String composition) {
		StringTokenizer st = new StringTokenizer(composition, " ", false);
		boolean keep[] = new boolean[input.tokenCount];
		while (st.hasMoreTokens()) {
			String named = st.nextToken();
			MatchRange mr = (MatchRange) ranges.get(named);
			if (mr == null) {
				continue;
			}
			for (int i = mr.getStart(); i < mr.getEnd(); i++) {
				keep[i] = true;
			}
		}
		return input.getReduction(keep);
	}


	public TokenizedInput filterInput(String filter) {
		StringTokenizer st = new StringTokenizer(filter, " ", false);
		boolean keep[] = new boolean[input.tokenCount];
		for (int i = 0; i < keep.length; i++) {
			keep[i] = true;
		}
		while (st.hasMoreTokens()) {
			String named = st.nextToken();
			MatchRange mr = (MatchRange) ranges.get(named);
			if (mr == null) {
				continue;
			}
			for (int i = mr.getStart(); i < mr.getEnd(); i++) {
				keep[i] = false;
			}
		}
		return input.getReduction(keep);
	}


	public String toString() {
		String val = "";
		Iterator en = ranges.keySet().iterator();
		while (en.hasNext()) {
			Object key = en.next();
			val = val + "Name: " + key.toString() + " value: " + getMatch(key.toString()) + "\n";
		}
		return val;
	}


	public String fillMatchVariables(String src) {
		StringBuffer result = new StringBuffer();
		StringTokenizer st = new StringTokenizer(src, "$", true);
		while (st.hasMoreTokens()) {
			String t = st.nextToken();
			if (t.length() > 1) {
				result.append(t);
				continue;
			}
			if (t.equals("$")) {
				if (!st.hasMoreTokens()) {
					break;
				}
				String v = st.nextToken();
				if (v.equals("$")) {
					result.append("$");
				}
				result.append(v);
			} else {
				result.append(t);
			}
		}
		return result.toString();
	}


	private void addNamedPattern(Pattern p, Matcher matcher, DefaultTokenizedInput input) {
		if (!p.isNamed()) {
			return;
		}
		int pstart = p.getStartOfMatchPosition(matcher);
		int pend = p.getEndOfMatchPosition(matcher);
		if (pstart == pend) {
			return;
		}
		ranges.put(p.getName(), MatchRange.newMatchRange(pstart, pend));
	}


	private void addNamedPattern(int pstart, int pend, DefaultTokenizedInput input, String name) {
		if (pstart == pend) {
			return;
		}
		ranges.put(name, MatchRange.newMatchRange(pstart, pend));
	}

        public boolean exists(String match) {
          return ranges.containsKey(match);
        }

}
