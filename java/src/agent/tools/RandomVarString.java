package elias.agent.tools;
import java.util.*;

import elias.agent.runtime.api.*;
/**
 * <p>Title: Elias Agent</p>
 * <p>Description: Ihre Beschreibung</p>
 * <p>Copyright: Copyright (c) 1999</p>
 * <p>Company: </p>
 * @author Kai Londenberg
 * @version
 */

public class RandomVarString {

  VarString values[];

  public RandomVarString(String lines, String delim) {
    StringTokenizer st = new StringTokenizer(lines, delim, false);
    ArrayList vlist = new ArrayList();
    while (st.hasMoreTokens()) {
      vlist.add(new VarString(st.nextToken()));
    }
    values = new VarString[vlist.size()];
    vlist.toArray(values);
  }

  public RandomVarString(String values) {
     this(values, "\n");
  }

  public void appendValue(StringBuffer target, VariableScope scope, int random) {
     values[random % values.length].appendValue(target, scope);
  }



}