package elias.agent.runtime.pattern;

import java.io.*;

/**
 * Title:        Elias Agent
 * Description:  Ihre Beschreibung
 * Copyright:    Copyright (c) 1999
 * Company:
 * @author Kai Londenberg
 * @version
 */

public class NamedString implements Serializable {


  public NamedString(String name, String string) {
    this.string = string;
    this.name = name;
  }
  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
  }
  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
  }
  public void setName(String newName) {
    name = newName;
  }
  public String getName() {
    return name;
  }
  public void setString(String newString) {
    string = newString;
  }
  public String getString() {
    return string;
  }

  public String toString() {
    return name;
  }
  public int hashCode() {
    return string.hashCode();
  }
  private String name;
  private String string;
}