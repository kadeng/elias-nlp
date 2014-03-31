package elias.agent.authoring.patterncompiler;

import java_cup.runtime.*;

public class EnhancedParser extends parser {

        public EnhancedParser(java_cup.runtime.Scanner s) {
          super(s);
        }

        public EnhancedParser() {
          super();
        }

  /** Report a fatal error.  This method takes a  message string and an
   *  additional object (to be used by specializations implemented in
   *  subclasses).  Here in the base class a very simple implementation
   *  is provided which reports the error then throws an exception.
   *
   * @param message an error message.
   * @param info    an extra object reserved for use by specialized subclasses.
   */
  public void report_fatal_error(
    String   message,
    Object   info)
    throws java.lang.Exception
    {
      /* stop parsing (not really necessary since we throw an exception, but) */
      done_parsing();

      if (info instanceof Symbol) {
        throw new ParseException("Error parsing pattern.", (Symbol)info);
      }

      /* throw an exception */
      throw new Exception("Error parsing pattern");
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Report a non fatal error (or warning).  This method takes a message
   *  string and an additional object (to be used by specializations
   *  implemented in subclasses).  Here in the base class a very simple
   *  implementation is provided which simply prints the message to
   *  System.err.
   *
   * @param message an error message.
   * @param info    an extra object reserved for use by specialized subclasses.
   */
  public void report_error(String message, Object info)
    {
        System.err.print(message);
        if (info instanceof Symbol)
          if (((Symbol)info).left != -1)
          System.err.println(" at character " + ((Symbol)info).left +
                             " of input");
          else System.err.println("");
        else System.err.println("");
    }

}