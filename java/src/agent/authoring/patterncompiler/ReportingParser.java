package elias.agent.authoring.patterncompiler;

import java_cup.runtime.*;

public class ReportingParser extends parser {

  public ReportingParser() {
  }

  public void report_fatal_error(String   message, Object info)
    throws java.lang.Exception {
      throw new ParseException(message, (Symbol) info);
  }

   public void report_error(String message, Object info) {}


}