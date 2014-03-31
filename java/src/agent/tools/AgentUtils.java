package elias.agent.tools;


import elias.agent.runtime.api.*;
import java.util.*;

/**
 * <p>Title: Elias Agent</p>
 * <p>Description: Ihre Beschreibung</p>
 * <p>Copyright: Copyright (c) 1999</p>
 * <p>Company: </p>
 * @author Kai Londenberg
 * @version
 */

public class AgentUtils {

   public static HashMap rvars = new HashMap(1000,0.4f);
   public final static VarString StaticString = new VarString("");

   public static String translate(String src, TalkletRuntime runtime) {
       VarString v = (VarString)rvars.get(src);
       if (v==null) {
          if (src.indexOf("#")<0) {
             rvars.put(src, StaticString);
             return src;
          } else {
            v = new VarString(src);
            rvars.put(src, v);
          }
       }
       if (v==StaticString) return src;
       StringBuffer result = new StringBuffer();
       v.appendValue(result, runtime);
       return result.toString();
   }

   public static String fillVariables(String src, String delim, Talklet talklet, Topic topic, MatchResult match) {
       StringTokenizer st = new StringTokenizer(src, delim, true);
       StringBuffer result = new StringBuffer();
       int s = 0;
       while (st.hasMoreTokens()) {
         String t = st.nextToken();
         boolean d = t.equals(delim);
         switch (s) {
            case 0:
              if (d) {
                 s = 1;
              } else {
                 result.append(t);
              }
              break;
            case 1:
              if (d) {
                 result.append(delim);
                 s = 0;
              } else {
                 // Variable einf�gen
                 if ((match!=null) && (match.exists(t))) {
                    result.append(match.getMatch(t));
                 } else if ((topic!=null) && (topic.getLocal(t)!=null)) {
                    result.append(topic.getLocal(t).toString());
                 } else if ((talklet!=null) && (talklet.getGlobal(t)!=null)) {
                    result.append(talklet.getGlobal(t).toString());
                 }
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
       return result.toString();
   }

   public static void extractMatches(MatchResult match, Talklet talklet, Topic topic) {
       Iterator iter = match.getMatchNames();
       while (iter.hasNext()) {
         String mname = iter.next().toString();
         try {
         if (topic.localExists(mname)) {
              if (String.class.equals(topic.getLocalType(mname))) {
                 topic.setLocal(mname, match.getMatch(mname));
              }
         }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         try {
         if (talklet.globalExists(mname)) {
              if (String.class.equals(talklet.getGlobalType(mname))) {
                 talklet.setGlobal(mname, match.getMatch(mname));
              }
         }
         } catch (Exception exx) {
           exx.printStackTrace();
         }

       }
   }

   public static void main(String args[]) {
      System.out.println(fillVariables("#Hallo# das #ist# Test ##2","#",null, null,null));
    }

}