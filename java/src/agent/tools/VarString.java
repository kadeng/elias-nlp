package elias.agent.tools;

import java.util.*;

/**
 * <p>Title: Elias Agent</p>
 * <p>Description: Ihre Beschreibung</p>
 * <p>Copyright: Copyright (c) 1999</p>
 * <p>Company: </p>
 * @author Kai Londenberg
 * @version
 */

public class VarString {

  String values[];
  String vars[];

  public VarString(String src, String delim) {
       StringTokenizer st = new StringTokenizer(src, delim, true);
       int s = 0;
       ArrayList vallist = new ArrayList();
       ArrayList varlist = new ArrayList();
       while (st.hasMoreTokens()) {
         String t = st.nextToken();
         boolean d = t.equals(delim);
         if (d) t = "";
         switch (s) {
            case 0:
              if (d) {
                 s = 1;
              }
              vallist.add(t);
              break;
            case 1:
              if (d) {
                 String p = vallist.get(vallist.size()-1).toString();
                 vallist.set(vallist.size()-1, p+delim);
                 s = 0;
              } else {
                 varlist.add(t);
                 s = 2;
              }
              break;
            case 2:
              if (d) {
                 s = 0;
              } else {
                 // Nicht moeglich
                 throw new RuntimeException("Illegaler Ausfuehrungspfad");
              }
         }
       }
       values = new String[vallist.size()];
       vallist.toArray(values);
       vars = new String[varlist.size()];
       varlist.toArray(vars);
  }

  public VarString(String str) {
      this(str, "#");
  }

  public void appendValue(StringBuffer target, VariableScope scope) {
      for (int i=0;i<values.length;i++) {
          target.append(values[i]);
          if (i<vars.length) scope.appendValue(target, vars[i]);
      }
  }
}