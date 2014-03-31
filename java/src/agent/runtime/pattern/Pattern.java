/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.pattern;

import java.util.Iterator;
import java.util.*;
import java.io.Serializable;

/**
 *  Base class for a Pattern structure. Instead of having several subclasses,
 *  this is a single *final* class for all Types. This eliminates slow abstract
 *  method invocation.
 *
 *@author     Kai Londenberg
 *@created    2. Juli 2001
 */
public final class Pattern implements Cloneable, Serializable {
	final static long serialVersionUID = 3979811307894244629L;

	/**
	 *  type constant for the Start type of Pattern Element. A Start Pattern
	 *  represents the topmost Element of every usable Pattern Structure.
	 */
	final static int START = 0;
	/**
	 *  type constant for the Sequence type of Pattern Element. A Sequence Pattern
	 *  represents a Sequence of other Pattern Elements. When matching a Sequence,
	 *  every child pattern has to match.
	 */
	final static int SEQUENCE = 1;
	/**
	 *  type constant for the Or (Alternation) type of Pattern Element. If any of
	 *  the child-elements matches, this Pattern type returns true.
	 */
	final static int OR = 2;
	/**
	 *  type constant for the And Combination type of Pattern Element. Each of the
	 *  AND combined child patterns has to match (at the same start position), or
	 *  it will return false.
	 */
	final static int AND = 3;
	/**
	 *  type constant for the Repetition type of Pattern Element. It's child has to
	 *  be repeated a minimum, and may repeat up to a maximum amount of times.
	 */
	final static int REPETITION = 4;
	/**
	 *  type constant for the Negation type of Pattern Element. It simply negates
	 *  the result of it's child element.
	 */
	final static int NOT = 5;
	/**
	 *  type constant for the Option type of Pattern Element. It's child Pattern
	 *  may or may not match, it will return true in any case.
	 */
	final static int OPTIONAL = 6;
	/**
	 *  type constant for the Search type of Pattern Element. Searches for the
	 *  occurrence of it's child pattern within a given range from the current
	 *  position.
	 */
	final static int SEARCH = 7;
	/**
	 *  Type constant for a Terminal Pattern Element. Delegates the actual Matching
	 *  to an Object of the base Type Terminal. (Like TPatternString, TPatternRegex
	 *  or TPatternStringList).
	 */
	final static int TERMINAL = 8;

	final static int NAMED = 9;

	/**
	 *  Integer field, holding the type of this Pattern
	 */
	int type;

	/**
	 *  index of this pattern in the pattern array.
	 */
	int index;

	/**
	 *  Minimum size (in lexemes) of this pattern.
	 */
	int minSize = 0;

	/**
	 *  Reference to parent Pattern
	 */
	Pattern parent = null;

	// first child of this Pattern
	private Pattern firstchild = null;

	// pointer to the next sibling of this Pattern
	private Pattern next = null;

	// amount of child patterns
	// private int childcount = 0;

	// start and end of range for Repetition and Search
	private int from;
	private int to;

	// Reference to a Terminal object for terminal Patterns
	private Terminal terminal = null;


	/**
	 *  May be constructed by own methods only. see below the static createXXXX()
	 *  static Method factory constructors
	 */
	private Pattern() {
	}


	// ****** static Constructors ********************

	/**
	 *  Creates a TERMINAL Pattern
	 *
	 *@param  term  Terminal Object to be wrapped
	 *@return       the new Pattern Object.
	 *@see          #TERMINAL
	 */
	public static Pattern createTerminal(Terminal term) {
		Pattern p = new Pattern();
		p.type = TERMINAL;
		p.terminal = term;
		p.minSize = term.getMinSize();
		return p;
	}


	/**
	 *  Creates a NOT Pattern (Negation)
	 *
	 *@param  patt  Pattern to be negated.
	 *@return       The new Pattern Object.
	 *@see          #NOT
	 */
	public static Pattern createNegation(Pattern patt) {
		Pattern p = new Pattern();
		p.type = NOT;
		p.addChild(patt);
		p.minSize = 0;
		return p;
	}


	public static Pattern createNamed(String name, Pattern patt) {
		Pattern p = new Pattern();
		p.type = NAMED;
		p.addChild(patt);
		p.terminal = new TName(name);
		return p;
	}


	/**
	 *  Creates a START Pattern (topmost Element of every PatternStructure)
	 *
	 *@param  patt  Pattern to be wrapped in a Start-Element.
	 *@return       the new Pattern Object.
	 *@see          #OPTIONAL
	 */
	public static Pattern createStart(Pattern patt) {
		Pattern p = new Pattern();
		p.type = START;
		p.addChild(patt);
		return p;
	}


	/**
	 *  Creates an OPTIONAL Pattern
	 *
	 *@param  patt  Pattern to be wrapped as being optional
	 *@return       The new Pattern Object
	 *@see          #OPTIONAL
	 */
	public static Pattern createOptional(Pattern patt) {
		Pattern p = new Pattern();
		p.type = OPTIONAL;
		p.addChild(patt);
		p.minSize = 0;
		return p;
	}


	/**
	 *  Creates a SEARCH Pattern
	 *
	 *@param  patt  Pattern to be searched.
	 *@param  from  from offset
	 *@param  to    to offset of current position
	 *@return       The new Search Pattern
	 *@see          #SEARCH
	 */
	public static Pattern createSearch(Pattern patt, int from, int to) {
		Pattern p = new Pattern();
		p.type = SEARCH;
		p.minSize = from + patt.minSize;
		p.addChild(patt);
		p.from = from;
		// start of range
		p.to = to - from;
		// length of range
		return p;
	}


	/**
	 *  Creates a REPETITION Pattern
	 *
	 *@param  patt  Pattern to be repeated
	 *@param  from  from .. times
	 *@param  to    up to .. times
	 *@return       the new Pattern
	 *@see          #REPETITION
	 */
	public static Pattern createRepetition(Pattern patt, int from, int to) {
		Pattern p = new Pattern();
		p.type = REPETITION;
		p.minSize = from * patt.minSize;
		p.addChild(patt);
		p.from = from;
		// even numbers for nextstep,
		p.to = to;
		// uneven ones for stepbacks in the Matcher
		return p;
	}


	/**
	 *  Creates a SEQUENCE Pattern. Childs to be added by calling addChild(child)
	 *
	 *@return    The new Pattern Object.
	 *@see       #addChild(Pattern)
	 *@see       #SEQUENCE
	 */
	public static Pattern createSequence() {
		Pattern p = new Pattern();
		p.type = SEQUENCE;
		return p;
	}


	/**
	 *  Creates a AND Pattern. Childs to be added by calling addChild(child)
	 *
	 *@return    The new Pattern Object.
	 *@see       #addChild(Pattern)
	 *@see       #AND
	 */
	public static Pattern createAnd() {
		Pattern p = new Pattern();
		p.type = AND;
		return p;
	}


	/**
	 *  Creates an OR Pattern. Childs to be added by calling addChild(child)
	 *
	 *@return    The new Pattern Object.
	 *@see       #addChild(Pattern)
	 *@see       #OR
	 */
	public static Pattern createOr() {
		Pattern p = new Pattern();
		p.type = OR;
		return p;
	}



	public Pattern getCopy(Pattern newparent) {
		Pattern p = new Pattern();
		p.type = type;
		p.parent = newparent;
		p.index = 0;
		p.from = from;
		p.to = to;
		p.terminal = terminal;
		p.firstchild = null;
		if (firstchild != null) {
			p.firstchild = firstchild.getCopy(p);
		}
		if (next != null) {
			p.next = next.getCopy(newparent);
		}
		return p;
	}


	// Helper methods to be used in constructing a Pattern structure

	/**
	 *  Adds a child to this Pattern. To be used by the Parser only. May not be
	 *  called, after the PatternStructure has been created !
	 *
	 *@param  child  new Child.
	 *@see           #prepare(PatternStructure, int)
	 */
	public void addChild(Pattern child) {
		if (child == null) {
			return;
		}
		child.parent = this;
		if (firstchild == null) {
			firstchild = child;
		} else {
			Pattern c = firstchild;
			while (c.next != null) {
				c = c.next;
			}
			c.next = child;
		}
		while (child.next != null) {
			child = child.next;
			child.parent = this;
		}
	}


	/**
	 *  Optimizes the Pattern. May not be called, after the Pattern structure has
	 *  been created.
	 *
	 *@param  level      Optimization level (0-5)
	 *@param  threshold  Threshold at which similar elements will be grouped.
	 */
	public void optimize(int level, int threshold) {
		if (level < 1) {
			return;
		}
		straighten();
		Pattern child = firstchild;
		while (child != null) {
			// optimize children first ..
			child.optimize(level, threshold);
			child = child.next;
		}
		if (level < 3) {
			return;
		}
		switch (type) {
			case OR:
				optimizeORStrings(threshold);
				if (level > 4) {
					optimizeORedStringSequences(threshold);
				}
				break;
			case SEQUENCE:
				optimizeSequenceStrings();
				break;
		}
		if (level == 3) {
			return;
		}
		switch (type) {
			case OR:
			case SEQUENCE:
			case AND:
				// if those types have just one child, replace them
				// by their child.
				if (firstchild.next == null) {
					type = firstchild.type;
					from = firstchild.from;
					to = firstchild.to;
					terminal = firstchild.terminal;
					firstchild = firstchild.firstchild;
				}
		}
	}


	/**
	 *  Recursively prints info about this Pattern structure. Usually only needed
	 *  to be called on the Start-Pattern
	 *
	 *@param  depth  Indentation level
	 */
	public void printInfo(int depth) {
		switch (type) {
			case START:
				System.out.println(info(depth) + "Pattern structure of :");
				break;
			case TERMINAL:
				System.out.println(info(depth) + "Terminal: " + terminal.toString());
				break;
			case SEQUENCE:
				System.out.println(info(depth) + "Sequence of");
				break;
			case AND:
				System.out.println(info(depth) + "And Combination of");
				break;
			case OR:
				System.out.println(info(depth) + "Or Combination of");
				break;
			case NOT:
				System.out.println(info(depth) + "Negation of");
				break;
			case NAMED:
				System.out.println(info(depth) + "Name " + terminal.toString() + " for");
				break;
			case OPTIONAL:
				System.out.println(info(depth) + "Option of");
				break;
			case SEARCH:
				System.out.println(info(depth) + "Search within " + from + " to " + to);
				break;
			case REPETITION:
				System.out.println(info(depth) + "Repetition (from " + from + " to " + to + ") times of");
				break;
		}
		Pattern child = firstchild;
		while (!(child == null)) {
			child.printInfo(depth + 1);
			child = child.next;
		}
	}


	/**
	 *  Prints info about this Pattern (non recursively)
	 *
	 *@param  matcher  Matcher
	 */
	public void printTrace(Matcher matcher) {
		int depth = 1;
		System.out.print("Pos<" + matcher.position + ">");
		switch (type) {
			case START:
				System.out.println(info(depth) + "Pattern structure of :");
				break;
			case TERMINAL:
				System.out.println(info(depth) + "Terminal: " + terminal.toString());
				break;
			case SEQUENCE:
				System.out.println(info(depth) + "Sequence of");
				break;
			case AND:
				System.out.println(info(depth) + "And Combination of");
				break;
			case OR:
				System.out.println(info(depth) + "Or Combination of");
				break;
			case NOT:
				System.out.println(info(depth) + "Negation of");
				break;
			case OPTIONAL:
				System.out.println(info(depth) + "Option of");
				break;
			case SEARCH:
				System.out.println(info(depth) + "Search within " + from + " to " + to);
				break;
			case REPETITION:
				System.out.println(info(depth) + "Repetition (from " + from + " to " + to + ") times of");
				break;
			case NAMED:
				System.out.println(info(depth) + "Named: " + terminal.toString());
				break;
		}
	}


	int getEndOfMatchPosition(Matcher matcher) {
		switch (type) {
			case START:
			case NAMED:
				return firstchild.getEndOfMatchPosition(matcher);
			case NOT:
				return matcher.states[index].start;
		}
		return matcher.states[index].end;
	}


	int getStartOfMatchPosition(Matcher matcher) {
		switch (type) {
			case START:
				return firstchild.getStartOfMatchPosition(matcher);
		}
		return matcher.states[index].start;
	}


	boolean isNamed() {
		return type == NAMED;
	}


	String getName() {
		return terminal.toString();
	}


	Pattern getFirstChild() {
		return firstchild;
	}


	/**
	 *  Core Pattern Matching Algorithm: Try to match, traversing down the Tree
	 *  from this Pattern. Called by Matcher.match()
	 *
	 *@param  matcher  Description of Parameter
	 *@return          Description of the Returned Value
	 *@see             Matcher#match(PatternStructure, LexicalInput)
	 */

	final boolean match(Matcher matcher) {
		//System.out.print("Matching ->");
		//printTrace(matcher);
		if (matcher.input.tokenCount - matcher.position - minSize < 0) {
			matcher.states[index].match = false;
			return false;
		}

		DynIntArray counter;
		Pattern child;
		int res;
		int i;
		final State state = matcher.states[index];
		state.start = matcher.position;
		// end of initialization part, now the code
		switch (type) {
			case START:
			case NAMED:
				return firstchild.match(matcher);
			case TERMINAL:
				res = terminal.match(matcher.input, matcher.position);
				if (res == -1) {
					state.match = false;
					return false;
				}
				if (res == 1) {
					matcher.input.matchidx[matcher.position++] = index;
				} else {
					for (i = 0; i < res; i++) {
						matcher.input.matchidx[matcher.position++] = index;
					}
				}
				state.match = true;
				state.end = matcher.position;
				return true;
			case SEQUENCE:
				child = firstchild;

				while (child != null) {
					if (!child.match(matcher)) {
						matcher.position = state.start;
						state.match = false;
						return false;
					}
					child = child.next;
				}
				state.match = true;
				state.end = matcher.position;
				return true;
			case OR:
				child = firstchild;
				while (child != null) {
					if (child.match(matcher)) {
						state.match = true;
						child = child.next;
						if (child != null) {
							matcher.pushtry(child.index, state.start);
						}
						state.end = matcher.position;
						return true;
					}
					child = child.next;
				}
				state.match = false;
				return false;
			case NOT:
				if (!firstchild.match(matcher)) {
					matcher.position = state.start;
					state.match = true;
					state.end = matcher.position;
					return true;
				}
				state.match = false;
				matcher.position = state.start;
				return false;
			case AND:
				child = firstchild;
				i = state.start;
				while (child != null) {
					matcher.position = state.start;
					if (!child.match(matcher)) {
						matcher.position = state.start;
						state.match = false;
						return false;
					}
					child = child.next;
					i = (i > matcher.position) ? i : matcher.position;
				}
				state.match = true;
				state.end = i;
				matcher.position = i;
				return true;
			case OPTIONAL:
				state.match = true;
				if (firstchild.match(matcher)) {
					matcher.pushtry(index + 1, state.start);
				}
				state.end = matcher.position;
				return true;
			case SEARCH:
				matcher.position += from;
				res = matcher.input.tokenCount - firstchild.minSize;

				// retrieve and initialize position dependent counter
				counter = state.counter;
				i = 0;
				while ((to >= i) && (res - matcher.position >= 0)) {
					counter.set(matcher.position, i++);
					if (firstchild.match(matcher)) {
						if ((to >= i) && (res - matcher.position >= 0)) {
							counter.set(matcher.position + 1, i);
							matcher.pushtry(index + 1, matcher.states[firstchild.index].start + 1);
						}
						state.match = true;
						state.end = matcher.position;
						return true;
					}
					matcher.position++;
				}
				matcher.position = state.start;
				state.match = false;
				return false;
			case REPETITION:
				// retrieve and initialize position dependent counter
				counter = state.counter;
				i = 0;
				while (i < from) {
					counter.set(matcher.position, i++);
					if (!firstchild.match(matcher)) {
						matcher.position = state.start;
						state.match = false;
						return false;
					}
				}
				while (i <= to) {
					// ok, minimum reached, now be greedy
					res = matcher.position;
					counter.set(res, i);
					if ((firstchild.match(matcher)) && (matcher.position>res)) {
                                                matcher.pushtry(index + 1, res);
					} else {
						// finished - can't repeat anymore
						state.match = true;
						state.end = res;
						matcher.position = res;
						return true;
					}
					i++;
				}
				state.match = false;
				matcher.position = state.start;
				return false;
		}
		// switch
		return state.match;
	}


	// match(matcher, nexttry)

	/**
	 *  Core Pattern Matching Algorithm: Retry with a given child Called by
	 *  Matcher.match()
	 *
	 *@param  matcher  Description of Parameter
	 *@param  child    Description of Parameter
	 *@return          Description of the Returned Value
	 *@see             Matcher#match(PatternStructure, LexicalInput)
	 */
	final boolean retry_match(Matcher matcher, Pattern child) {
		//System.out.print("Retrying ->");
		//printTrace(matcher);
		DynIntArray counter;
		int res;
		int i;
		final State state = matcher.states[index];
		//System.out.println("Retry at Position " + matcher.position);
		// end of initialization part, now the code
		switch (type) {
			case OR:
				while (child != null) {
					if (child.match(matcher)) {
						child = child.next;
						if ((state.match) && (state.end == matcher.position)) {
							continue;
						}
						state.match = true;
						if (child != null) {
							matcher.pushtry(child.index, state.start);
						}
						state.end = matcher.position;
						return parent.continue_match(matcher, this, true);
					}
					child = child.next;
				}
				if (!state.match) {
					return false;
				}
				state.match = false;
				return parent.continue_match(matcher, this, false);
			case OPTIONAL:
				matcher.position = state.start;
				state.end = state.start;
				return parent.continue_match(matcher, this, true);
			case SEARCH:
				counter = state.counter;
				i = counter.array[matcher.position];
				res = matcher.input.tokenCount - firstchild.minSize;
				while ((to >= i) && (res - matcher.position >= 0)) {
					counter.set(matcher.position, i++);
					if (firstchild.match(matcher)) {
						if ((to >= i) && (res - matcher.position >= 0)) {
							counter.set(matcher.position + 1, i);
							matcher.pushtry(index + 1, matcher.states[firstchild.index].start + 1);
						}
						if ((state.match) && (state.end == matcher.position)) {
							return false;
						}
						state.match = true;
						state.end = matcher.position;
						return parent.continue_match(matcher, this, true);
					}
					matcher.position++;
				}
				if ((matcher.position == state.start) && (!state.match)) {
					return false;
				}
				matcher.position = state.start;
				state.match = false;
				return parent.continue_match(matcher, this, false);
			case REPETITION:
				state.end = matcher.position;
				return parent.continue_match(matcher, this, true);
		}
		// switch
		return false;
	}


	/**
	 *  Returns the amount of Pattern elements (recursively traverses the Patterns
	 *  down from this one)
	 *
	 *@return    Description of the Returned Value
	 */
	int count() {
		int sum = 1;
		Pattern child = firstchild;
		while (child != null) {
			sum += child.count();
			child = child.next;
		}
		return sum;
	}


	/**
	 *  Recursively Prepares this Pattern and all of it's children, by assigning
	 *  them the correct indices, and inserting them into a Pattern Structure.
	 *  After having been prepared, the Patterns may not be changed anymore.
	 *
	 *@param  structure  PatternStructure to be inserted into
	 *@param  idx        current index
	 *@return            returns the next free index to be used.
	 *@see               #index
	 */
	int prepare(PatternStructure structure, int idx) {
		index = idx++;
		structure.patterns[index] = this;
		for (Pattern child = firstchild; child != null; child = child.next) {
			idx = child.prepare(structure, idx);
		}
		return idx;
	}


	/**
	 *  Recursively calculates the minimum Size of this Pattern and all of it's
	 *  children.
	 */
	void calcMinSize() {
		switch (type) {
			case NAMED:
			// same as START
			case START:
				firstchild.calcMinSize();
				minSize = firstchild.minSize;
				break;
			case NOT:
				minSize = 0;
				firstchild.calcMinSize();
				break;
			case OPTIONAL:
				minSize = 0;
				firstchild.calcMinSize();
				break;
			case TERMINAL:
				minSize = terminal.getMinSize();
				break;
			case REPETITION:
				firstchild.calcMinSize();
				minSize = from * firstchild.minSize;
				break;
			case SEARCH:
				firstchild.calcMinSize();
				minSize = from + firstchild.minSize;
				break;
			default:
				minSize = 0;
				for (Pattern child = firstchild; child != null; child = child.next) {
					child.calcMinSize();
					switch (type) {
						case SEQUENCE:
							minSize += child.minSize;
							break;
						case AND:
							if (child == firstchild) {
								minSize = firstchild.minSize;
								break;
							}
							minSize = (child.minSize > minSize) ? child.minSize : minSize;
							break;
						case OR:
							if (child == firstchild) {
								minSize = firstchild.minSize;
								break;
							}
							minSize = (child.minSize < minSize) ? child.minSize : minSize;
							break;
					}
					// switch
				}
			// for
		}
		// switch
	}


	/**
	 *  Returns true if this Pattern wraps a TPatternString
	 *
	 *@return    The string value
	 */
	private boolean isString() {
		return ((type == TERMINAL) && (terminal instanceof TPatternString));
	}


	/**
	 *  Returns true, if this Pattern wraps a TPatternStringSequence
	 *
	 *@return    The stringSequence value
	 */
	private boolean isStringSequence() {
		return ((type == TERMINAL) && (terminal instanceof TPatternStringSequence));
	}


	// retry_match(matcher, child)

	/**
	 *  Core Pattern Matching Algorithm: Continue up the tree from a given retry.
	 *  Called by retry_match() and by itself.
	 *
	 *@param  matcher       Matcher Object (States)
	 *@param  child         child to continue with
	 *@param  childmatched  did the child match or not ?
	 *@return               true, if the Pattern matched.
	 *@see                  #retry_match(Matcher, Pattern)
	 */
	private final boolean continue_match(Matcher matcher, Pattern child, boolean childmatched) {
		//System.out.print("Continuing (" + ((childmatched) ? "true)->" : "false)->"));
		//printTrace(matcher);
		DynIntArray counter;
		int res;
		int i;
		final State state = matcher.states[index];
		switch (type) {
			case START:
				return childmatched;
			case NAMED:
				return parent.continue_match(matcher, this, childmatched);
			case SEQUENCE:
				if (!childmatched) {
					if (!state.match) {
						return false;
					}
					matcher.position = state.start;
					state.match = false;
					return parent.continue_match(matcher, this, false);
				}
                                child = child.next;
				while (child != null) {
					if (!child.match(matcher)) {
						if (!state.match) {
							return false;
						}
						matcher.position = state.start;
						state.match = false;
						return parent.continue_match(matcher, this, false);
					}
					child = child.next;
				}
				if (state.match && (state.end == matcher.position)) {
					return false;
				}
				state.match = true;
				state.end = matcher.position;
				return parent.continue_match(matcher, this, true);
			case OR:
				child = child.next;
				if (childmatched) {
					if ((!state.match) || (state.end != matcher.position)) {
						state.match = true;
						if (child != null) {
							matcher.pushtry(child.index, matcher.position);
						}
						state.end = matcher.position;
						return parent.continue_match(matcher, this, true);
					}
				}
				while (child != null) {
					if (child.match(matcher)) {
						child = child.next;
						if ((state.match) && (state.end == matcher.position)) {
							continue;
						}
						state.match = true;
						if (child != null) {
							matcher.pushtry(child.index, matcher.position);
						}
						state.end = matcher.position;
						return parent.continue_match(matcher, this, true);
					}
					child = child.next;
				}
				if (!state.match) {
					return false;
				}
				return parent.continue_match(matcher, this, false);
			case NOT:
				matcher.stoptry = true;
				// stop this try, and all of it's generated tries
				return false;
			// No retry in negated elements. Just one chance to return false, and thus return true
			/*
			 *  if ((!childmatched && state.match) || (childmatched && !state.match)) {
			 *  matcher.position = state.start;
			 *  return false;
			 *  }
			 *  state.match = !childmatched;
			 *  matcher.position = state.start;
			 *  return parent.continue_match(matcher, this, state.match);
			 */
			case AND:
				if (!childmatched) {
					matcher.position = state.start;
					return parent.continue_match(matcher, this, false);
				}
				i = matcher.position;
				child = child.next;
				while (child != null) {
					matcher.position = state.start;
					if (!child.match(matcher)) {
						matcher.position = state.start;
						state.match = false;
						return false;
					}
					if (matcher.position > i) {
						i = matcher.position;
					}
					child = child.next;
				}
				matcher.position = i;
				if ((state.match) && (state.end == matcher.position)) {
					return false;
				}
				state.match = true;
				state.end = matcher.position;
				return parent.continue_match(matcher, this, true);
			case OPTIONAL:
				if (childmatched) {
					if (state.end == matcher.position) {
						return false;
					}
					state.end = matcher.position;
				} else {
					state.end = state.start;
					matcher.position = state.start;
				}
				return parent.continue_match(matcher, child, true);
			case SEARCH:
				counter = state.counter;
                                i = counter.array[matcher.states[index+1].start+1];
				res = matcher.input.tokenCount - firstchild.minSize;
				if (childmatched) {
					if ((to >= i++) && (res - matcher.position >= 0)) {
						counter.set(matcher.position + 1, i);
						matcher.pushtry(index + 1, matcher.states[firstchild.index].start + 1);
					}
					if ((state.match) && (state.end == matcher.position)) {
						return false;
					}
					state.match = true;
					state.end = matcher.position;
					return parent.continue_match(matcher, this, true);
				}
				matcher.position++;
				while ((to >= i) && (res - matcher.position >= 0)) {
					counter.set(matcher.position, i++);
					if (firstchild.match(matcher)) {
						if ((to >= i) && (res - matcher.position >= 0)) {
							counter.set(matcher.position + 1, i);
							matcher.pushtry(index + 1, matcher.states[firstchild.index].start + 1);
						}
						state.match = true;
						state.end = matcher.position;
						return parent.continue_match(matcher, this, true);
					}
					matcher.position++;
				}
				matcher.position = state.start;
				state.match = false;
				return parent.continue_match(matcher, this, false);
			case REPETITION:
				// retrieve and initialize position dependent counter
				if (!childmatched) {
					if (!state.match) {
						return false;
					}
					state.match = false;
					matcher.position = state.start;
					parent.continue_match(matcher, this, false);
				}
				counter = state.counter;
				i = counter.array[matcher.states[child.index].start];
				if ((matcher.position <= state.end) && ((1 + i) == counter.array[matcher.position])) {
					return false;
				}
				while (i < from) {
					counter.set(matcher.position, i++);
					if (!firstchild.match(matcher)) {
						matcher.position = state.start;
						state.match = false;
						return parent.continue_match(matcher, this, false);
					}
				}
				while (i <= to) {
					// ok, minimum reached, now be greedy
					res = matcher.position;
					counter.set(res, i);
					if (firstchild.match(matcher)) {
						// push the try before this as a try onto the trystack
						matcher.pushtry(index + 1, res);
					} else {
						// finished - can't repeat anymore
						state.match = true;
						state.end = res;
						matcher.position = res;
						return parent.continue_match(matcher, this, true);
					}
					i++;
				}
				state.match = false;
				matcher.position = state.start;
				return parent.continue_match(matcher, this, false);
		}
		return false;
	}


	/**
	 *  Removes the next Sibling of Pattern p
	 *
	 *@param  p  Pattern whose next Sibling is to be removed.
	 *@return    the removed Pattern
	 */
	private Pattern removeNext(Pattern p) {
		Pattern ret;
		if (p == null) {
			ret = firstchild;
			if (firstchild == null) {
				return null;
			}
			firstchild = firstchild.next;
			return firstchild;
		}
		ret = p.next;
		if (ret == null) {
			return null;
		}
		p.next = ret.next;
		return ret;
	}


	/**
	 *  returns the amount of children of this Pattern
	 *
	 *@return    Description of the Returned Value
	 */
	private int countChildren() {
		Pattern child = firstchild;
		int i = 0;
		while (child != null) {
			i++;
		}
		return i;
	}


	/**
	 *  Returns true if this pattern has only Terminal children.
	 *
	 *@return    Description of the Returned Value
	 */
	private boolean onlyTerminalChildren() {
		Pattern child = firstchild;
		while (child != null) {
			if (child.type != TERMINAL) {
				return false;
			}
		}
		return true;
	}


	/**
	 *  Calls optimizeEnclosedEqualTypes() on OR, SEQUENCE and AND Patterns. Thus
	 *  removing unneccessary Brackets in those constructs.
	 */
	private void straighten() {
		Pattern child = firstchild;
		while (child != null) {
			// optimize children first ..
			child.straighten();
			child = child.next;
		}
		switch (type) {
			case OR:
			case SEQUENCE:
			case AND:
				optimizeEnclosedEqualTypes();
		}
	}


	/**
	 *  Optimizes enclosed equal types. That is, reduces constructs like A OR B OR
	 *  (C OR D) to A OR B OR C OR D
	 */
	private void optimizeEnclosedEqualTypes() {
		Pattern child = firstchild;
		Pattern prev = null;
		while (child != null) {
			if (child.type == type) {
				removeNext(prev);
				if (prev == null) {
					firstchild = child.firstchild;
					prev = firstchild;
				} else {
					prev.next = child.firstchild;
				}
				if (prev != null) {
					while (prev.next != null) {
						prev = prev.next;
					}
				}
				addChild(child.next);
				child = child.next;
			} else {
				prev = child;
				child = child.next;
			}
		}
	}


	/**
	 *  Optimizes Sequences of Strings, reducing them to TPatternStringSequence
	 *  Terminals.
	 */
	private void optimizeSequenceStrings() {
		Pattern prestart = null;
		Pattern start;
		Pattern prev = null;
		Pattern replacement;
		Pattern child = null;
		start = firstchild;

		while (start != null) {
			int count = 0;
			while ((start != null) && (!(start.isString()))) {
				prestart = start;
				start = start.next;
			}
			child = start;
			while ((child != null) && (child.isString())) {
				count++;
				prev = child;
				child = child.next;
			}
			if (count < 2) {
				if (child != null) {
					prestart = prev;
					start = child;
					continue;
				} else {
					return;
				}
			}
			String strings[] = new String[count];
			child = start;
			count = 0;
			while ((child != null) && (child.isString())) {
				strings[count++] = ((TPatternString) child.terminal).value;
				prev = child;
				child = child.next;
			}
			replacement = Pattern.createTerminal(new TPatternStringSequence(strings));
			replacement.parent = this;
			if (prestart == null) {
				firstchild = replacement;
			} else {
				prestart.next = replacement;
			}
			replacement.next = child;
			prestart = replacement;
			start = child;
		}
	}


	/**
	 *  Optimizes OR'ed Strings, reducing them to TPatternStringList Terminals. If
	 *  there are less OR'ed Strings than the threshold, they will not be reduced.
	 *
	 *@param  threshold  threshold for reduction.
	 */
	private void optimizeORStrings(int threshold) {
		if (type != OR) {
			return;
		}
		Pattern child = firstchild;
		int count = 0;
		while (child != null) {
			if (child.type == TERMINAL) {
				if (child.isString()) {
					count++;
				}
			}
			child = child.next;
		}
		if (count < threshold) {
			return;
		}
		String strings[] = new String[count];
		child = firstchild;
		Pattern prev = null;
		count = 0;
		while (child != null) {
			if (child.type == TERMINAL) {
				if (child.isString()) {
					strings[count++] = ((TPatternString) child.terminal).value;
					// remove pattern
					if (prev != null) {
						prev.next = child.next;
					} else {
						firstchild = child.next;
					}
					child = child.next;
					continue;
				}
			}
			prev = child;
			child = child.next;
		}
		// and now replace them all with a String List
		Pattern replacement = Pattern.createTerminal(
				new TPatternStringList(strings));
		replacement.next = firstchild;
		replacement.parent = this;
		firstchild = replacement;
	}


	/**
	 *  Optimizes OR'ed Sequences of TPatternStringSequences. If enough (more or
	 *  equal than threshold) Sequences of the same length are OR'ed, they get
	 *  reduced to TPatternFixedLengthStringSequenceList.
	 *
	 *@param  threshold  threshold for reduction
	 */
	private void optimizeORedStringSequences(int threshold) {
		Pattern child = firstchild;
		int count = 0;
		int i;
		while (child != null) {
			count++;
			child = child.next;
		}
		if (count < threshold) {
			return;
		}
		Pattern parray[] = new Pattern[count];
		count = 0;
		int length = 0;
		child = firstchild;
		while (child != null) {
			parray[count++] = child;

			if (child.isStringSequence()) {
				int len = ((TPatternStringSequence) child.terminal).length;
				length = (length > len) ? length : len;
			}
			child = child.next;
			parray[count - 1].next = null;
		}
		firstchild = null;
		// remove all Patterns for later use ..

		for (; length > 1; length--) {
			int amount = 0;
			for (i = 0; i < count; i++) {
				if (parray[i] == null) {
					continue;
				}
				if (parray[i].isStringSequence()) {
					if (length == ((TPatternStringSequence) parray[i].terminal).length) {
						amount++;
					}
				}
			}
			if (amount < threshold) {
				// if not enough, put them back to children
				for (i = 0; i < count; i++) {
					if (parray[i] == null) {
						continue;
					}
					if (parray[i].isStringSequence()) {
						if (length == ((TPatternStringSequence) parray[i].terminal).length) {
							addChild(parray[i]);
							parray[i] = null;
						}
					}
				}
			}
			if (amount < threshold) {
				continue;
			}
			// if enough, create Terminal from them and put that back instead
			TPatternStringSequence sseq[] = new TPatternStringSequence[amount];
			amount = 0;
			for (i = 0; i < count; i++) {
				if (parray[i] == null) {
					continue;
				}
				if (parray[i].isStringSequence()) {
					if (length == ((TPatternStringSequence) parray[i].terminal).length) {
						sseq[amount++] = (TPatternStringSequence) parray[i].terminal;
						parray[i] = null;
					}
				}
			}
			try {
				addChild(Pattern.createTerminal(new TPatternFixedLengthStringSequenceList(sseq)));
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
		for (i = 0; i < count; i++) {
			// now put the rest back
			if (parray[i] != null) {
				addChild(parray[i]);
			}
		}
	}


	private String info(int count) {
		char indent[] = new char[count];
		for (int i = 0; i < count; i++) {
			indent[i] = ' ';
		}
		return (new String(indent)) + index + ":" + minSize + ":";
	}

}
