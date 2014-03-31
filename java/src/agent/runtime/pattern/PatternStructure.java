package elias.agent.runtime.pattern;
import java.io.Serializable;

/**
 *  Represents a complete PatternStructure which may be matched by a Matcher.
 *  An instance of this Class gets returned by the PatternCompiler.
 *  It contains a reference to it's Topmost (START) Pattern, and an Array
 *  which contains all of the Patterns of this structure. (They are still interlinked).
 *  Every Pattern may have only ONE PatternStructure. (Terminals may be wrapped, so they
 *  can be used in several)
 *
 *@author     Kai Londenberg
 *@see Matcher#match(PatternStructure, LexicalInput)
 *@see Pattern
 */
public class PatternStructure implements Serializable {
        static final long serialVersionUID = 6622947609609598910L;
        private static int max_states = 0;

        public boolean requiresDictionary = false;

        public static int getMaximumOccurredStateCount() {
          return max_states;
        }

	Pattern patterns[];
        boolean negated = false; // negate the end-result ?
	Pattern top;
        int names = 0;

	/**
	 *  Description of the Field
	 */
	public static int optimization_level = 5;

        public Pattern getTopmostFunctionalPattern() {
          return top.getFirstChild();
        }

        /**
	 *  Constructor for the PatternStructure object
	 *
	 *@param  first  Description of Parameter
	 */
	public PatternStructure(Pattern first) {
		top = Pattern.createStart(first);
		top.optimize(optimization_level, 4);
      		patterns = new Pattern[top.count()];
		top.prepare(this, 0);
		top.calcMinSize();
                for (int i=0;i<patterns.length;i++) {
                  if (patterns[i].type == Pattern.NAMED) names++;
                }
                if (patterns.length>max_states) max_states = patterns.length;
	}


        public Pattern getStart() {
		return top;
	}

        public boolean isNegated() {
          return negated;
        }

        public void setNegated(boolean flag) {
          negated = flag;
        }

        public int getStateCount() {
          return patterns.length;
        }

}

