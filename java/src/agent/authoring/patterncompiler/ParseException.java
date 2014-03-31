package elias.agent.authoring.patterncompiler;

import java_cup.runtime.*;

public class ParseException extends Exception {
  Symbol symbol = null;
  Object info;


  public ParseException(String msg, Symbol sym) {
    super(msg);
    symbol = sym;
  }

  public Symbol getSymbol() {
    return symbol;
  }

  public void setInfo(Object info) {
    this.info = info;
  }

  public Object getInfo() {
    return info;
  }

  public int getStartPos() {
    return symbol.left;
  }

  public int getEndPos() {
    return symbol.right;
  }

}