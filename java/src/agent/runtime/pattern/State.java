package elias.agent.runtime.pattern;

/**
 *  Represents an entry in the State array of the Matcher class.
 *  Every State has a corresponding Pattern in the Array of the PatternStructure
 *  to be matched. Runtime match info about the corresponding Pattern is stored in this
 *  State of the Pattern.
 *
 *@author     Kai Londenberg
 *@created    26. Juni 2001
 *@see Matcher#states
 *@see Pattern#index
 *@see PatternStructure#patterns
 */
final class State {
	/**
         * Did the corresponding Pattern match ?
         */
        boolean match;

        /**
         * Start position of match (if it did match)
         */
	int start;
        /**
         * End position of match (if it matched)
         */
        int end;

        /**
         * Array, containing the number of the try which starts at a given position.
         * Indiced by the offset to the start position of the first try of the corresponding
         * Pattern. To be used by SEARCH and REPETITION Patterns.
         */
        DynIntArray counter = new DynIntArray(15);

        /**
         * Which child to be used at the next try
         */
	//Pattern child;

}
