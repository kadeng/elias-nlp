/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.authoring.patterncompiler.transformation;

import java_cup.runtime.*;
import elias.agent.authoring.patterncompiler.Range;
import com.stevesoft.pat.*;
import java.util.*;
import elias.agent.runtime.pattern.PatternStructure;
import elias.agent.runtime.pattern.TPatternAnyToken;
import elias.agent.runtime.pattern.TPatternRegex;
import elias.agent.runtime.pattern.TPatternString;
import elias.agent.runtime.dictionary.WordClassPatternCompiler;
import elias.agent.authoring.patterncompiler.UnknownPatternException;

public class TransformationLexer extends sym implements java_cup.runtime.Scanner {
	static int default_operand = BEFORE;
	static Regex patterns[] = new Regex[15];
	static Regex ignore;

	Vector symbols = new Vector();
	Iterator sym_iterator = null;
        WordClassPatternCompiler wcPatternCompiler;
        String basePackage;

        public TransformationLexer(String input, Hashtable predefined, String basePackage) throws Exception {
          this(input, predefined, basePackage, null);
        }

	public TransformationLexer(String input, Hashtable predefined, String basePackage, WordClassPatternCompiler wcc) throws Exception {
		boolean allowterminal = true;
                this.basePackage = basePackage;
		wcPatternCompiler = wcc;
                int pos = 0;
		Symbol current = new Symbol(error);
		String match1;
		String match2;
		String match3;
		String match4;
		String match5;
		String match9;
                String match10;
		Range rng;
		Symbol last = null;
		//System.out.println("Tokenizing: ");
		for (int i = 2; i < 15; i++) {
			if (patterns[i] == null) {
				continue;
			}
			ignore.matchAt(input, pos);
			pos = ignore.matchedTo();
			if (patterns[i].matchAt(input, pos)) {
				current = new Symbol(i, pos, patterns[i].matchedTo());
				if ((symbols.size() > 0)) {
					last = (Symbol) symbols.get(symbols.size() - 1);
					if (((last.sym == TERMINAL) || (last.sym == NAMED) || (last.sym == RBRACK) || (last.sym == OPTRBRACK))
							 && ((current.sym == TERMINAL) || (current.sym == LBRACK) ||
							(current.sym == OPTLBRACK) || (current.sym == SEARCH) ||
							(current.sym == NOT) || current.sym == REPEAT)) {
						symbols.add(new Symbol(default_operand, pos, pos, null));
						//System.out.println("token " + symbols.size() + " =: default operand");
					}
				}
				symbols.add(current);
				pos = patterns[i].matchedTo();
				//System.out.println("token " + symbols.size() + " =:" + patterns[i].stringMatched());
				switch (i) {
					case REPEAT:
						match1 = patterns[i].stringMatched(1);
						match2 = patterns[i].stringMatched(2);
						match3 = patterns[i].stringMatched(3);
						rng = new Range(0, 100000);
						if (!(match1 == null)) {
							rng.from = Integer.parseInt(match1);
							if (match2 == null) {
								rng.to = rng.from;
							}
						}
						if (!(match3 == null)) {
							rng.to = Integer.parseInt(match3);
						}
						current.value = rng;
						break;
					case SEARCH:
						match1 = patterns[i].stringMatched(1);
						match2 = patterns[i].stringMatched(2);
						rng = new Range(0, 100000);
						if (!(match1 == null)) {
							rng.from = Integer.parseInt(match1);
						}
						if (!(match2 == null)) {
							rng.to = Integer.parseInt(match2) - 1;
						}
						current.value = rng;
						break;
					case NAMED:
						current.value = patterns[i].stringMatched(1);
						break;
					case PREDEFINED_PATTERN:
						String name = patterns[i].stringMatched(1);
						current.value = basePackage + ".Global." + name;
						break;
					case TERMINAL:
						match1 = patterns[i].stringMatched(1);
						match2 = patterns[i].stringMatched(2);
						match3 = patterns[i].stringMatched(3);
						match4 = patterns[i].stringMatched(4);
						match5 = patterns[i].stringMatched(5);
						match9 = patterns[i].stringMatched(9);
                                                match10 = patterns[i].stringMatched(10);
						if (match9 != null) {
							current.value = new TPatternString("<" + basePackage + ".Global.TOKEN>");
						} else if (match10!=null) {
                                                        if (wcPatternCompiler==null) {
                                                          throw new Exception("Cannot compile word class patterns without dictionary.");
                                                        }
                                                        current.value = wcPatternCompiler.compileWordClassPattern(match10);
                                                } else if (match2 != null) {
							current.value = new TPatternString(normalize(match3));
						} else {
							if (match4 != null) {
								if (match5 == null) {
									current.value = new TPatternString("");
								} else {
									current.value = new TPatternRegex(normalize(match5));
								}
							} else {
								current.value = new TPatternString(normalize(match1));
							}
						}
				}
				// switch
				i = 1;
			}
			// if
		}
		// for
		sym_iterator = symbols.iterator();
	}


	public Symbol next_token() throws java.lang.Exception {
		if (sym_iterator.hasNext()) {
			return (Symbol) sym_iterator.next();
		} else {
			return null;
		}
	}

        public String normalize(String str) {
          if (str.length() == 1) {
            char c = str.charAt(0);
            switch (c) {
              case '?':
              case '!':
              case ';':
              case ':':
              case '.':
              case '#':
              return "<NULL>";
            }
          }
          return "\"" + str + "\"";
        }

	static {
		com.stevesoft.pat.Key.registeredTo("Aster.Toadlilly.Rose v1.4/329921690");

		ignore = new Regex("[ \n]*");
		patterns[AND] = new Regex("&");
		patterns[OR] = new Regex("\\|");
		patterns[BEFORE] = null;
		patterns[NOT] = new Regex("!");
		patterns[LBRACK] = new Regex("\\(");
		patterns[RBRACK] = new Regex("\\)");
		patterns[OPTLBRACK] = new Regex("\\[");
		patterns[OPTRBRACK] = new Regex("\\]");
		patterns[REPEAT] = new Regex("([0-9]+)?(\\-)?([0-9]+)?\\*");
		patterns[SEARCH] = new Regex(">([0-9]+)?>([0-9]+)?");
		patterns[NAMED] = new Regex("=([a-zA-Z]+)");
		patterns[PREDEFINED_PATTERN] = new Regex("#([a-zA-Z][a-zA-Z0-9]*)#");
		patterns[TERMINAL] = new Regex("((\"([^\"]*)\")|('([^']*)')|([a-zA-ZöäüÜÖÄß]+)|([0-9]+(\\.[0-9]+)?)|(<TOKEN>)|<([^>]+)>|[^ =\n])");
	}
}
