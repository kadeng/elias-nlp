/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.pattern;
import elias.agent.runtime.dictionary.*;

/**
 *  Represents a State machine which allows to match a given input against a
 *  given PatternStructure. A Matcher may be used by just one Thread - it
 *  contains runtime data about the state of a given Match.
 *
 *@author     Kai Londenberg
 *@created    1. Januar 2002
 */
public final class Matcher {
	State states[];

	// runtime data
	Pattern patterns[];
	DefaultTokenizedInput input;
	int position;

	int trychildstack[];
	int trypositionstack[];
	int trypos;
	boolean stoptry = false;
	TaggedDictionary dictionary;


	/**
	 *  Constructor for the Matcher object
	 *
	 *@param  maxStates  maximum amount of states. That is, the maximum element
	 *      count of Patterns it is able to handle. Uses a fixed size array for
	 *      speed.
	 */
	public Matcher(int maxStates) {
		states = new State[maxStates + 1];
		trychildstack = new int[maxStates + 1];
		trypositionstack = new int[maxStates + 1];
		for (int i = 0; i < maxStates + 1; i++) {
			states[i] = new State();
		}
	}


	/**
	 *  Matches a given PatternStructure against a given Input.
	 *
	 *@param  pattern  PatternStructure to match input against.
	 *@param  input    input to be matched.
	 *@return          true, if the input matched the pattern.
	 *@see             Pattern
	 */
	public boolean match(PatternStructure pattern, DefaultTokenizedInput input) {
		this.input = input;
		trypos = 0;
		position = 0;
		boolean negated;
		try {
			patterns = pattern.patterns;
			negated = pattern.negated;
			int retry;
			if (pattern.top.match(this)) {
				return !negated;
				// true
			}
			stoptry = false;
			while ((retry = poptry()) != -1) {
				int tpos = trypos;
				if (patterns[retry].parent.retry_match(this, patterns[retry])) {
					return !negated;
					// true
				} else {
					if (stoptry) {
						// this retry encountered a negation
						trypos = tpos;
						// remove all tries from stack, which resulted from this try
					}
				}
			}
			return negated;
			// false
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			return false;
			// null pattern structures are allowed, but never match
		}
	}


	public DefaultMatchResult createMatchResult(PatternStructure pstructure, DefaultTokenizedInput input) {
		this.input = input;
		return new DefaultMatchResult(pstructure, this);
	}


	// same as match, but states have been cleared to allow a MatchResult to be built
	// used in constructor of MatchResult
	boolean clean_match(PatternStructure pattern, DefaultTokenizedInput input) {
		this.input = input;
		trypos = 0;
		position = 0;
		boolean negated;
		try {
			patterns = pattern.patterns;
			negated = pattern.negated;
			int retry;
			input.clean_matchidx(0);
			if (pattern.top.match(this)) {
				return !negated;
				// true
			}
			stoptry = false;
			while ((retry = poptry()) != -1) {
				int tpos = trypos;
				input.clean_matchidx(states[retry].start);
				if (patterns[retry].parent.retry_match(this, patterns[retry])) {
					return !negated;
					// true
				} else {
					if (stoptry) {
						// this retry encountered a negation
						trypos = tpos;
						// remove all tries from stack, which resulted from this try
					}
				}
			}
			return negated;
			// false
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			return false;
			// null pattern structures are allowed, but never match
		}
	}


	/**
	 *  Pushes a possible retry on the try-stack of this Matcher. Used by the core
	 *  Matching Algorithms of Pattern.
	 *
	 *@param  index     Description of Parameter
	 *@param  position  Description of Parameter
	 */
	final void pushtry(int index, int position) {
		trychildstack[trypos] = index;
		trypositionstack[trypos++] = position;
	}


	/**
	 *  Pops a given try back from the Try-stack. Used by the match() method.
	 *
	 *@return    index of the Pattern to start a retry with.
	 *@see       #match(PatternStruture, LexicalInput)
	 *@see       Pattern#retry_match(Matcher, Pattern)
	 */
	private final int poptry() {
		if (trypos > 0) {
			position = trypositionstack[--trypos];
			return trychildstack[trypos];
		}
		return -1;
	}

}
