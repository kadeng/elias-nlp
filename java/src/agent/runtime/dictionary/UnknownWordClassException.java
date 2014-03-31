package elias.agent.runtime.dictionary;

/**
 * Title:        Elias Agent
 * Description:  Ihre Beschreibung
 * Copyright:    Copyright (c) 1999
 * Company:
 * @author Kai Londenberg
 * @version
 */

public class UnknownWordClassException extends Exception {

  public UnknownWordClassException(String wordClassID) {
    super("Unknown word class: " + wordClassID);
  }
}