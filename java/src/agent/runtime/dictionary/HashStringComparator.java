package elias.agent.runtime.dictionary;

import java.util.Comparator;

/**
 * Title:        Elias Agent
 * Description:  Ihre Beschreibung
 * Copyright:    Copyright (c) 1999
 * Company:
 * @author Kai Londenberg
 * @version
 */

public final class HashStringComparator implements Comparator {
  public static final HashStringComparator instance = new HashStringComparator();

  private HashStringComparator() {
  }

  public int compare(Object o1, Object o2) {
    int hash1 = o1.hashCode();
    int hash2 = o2.hashCode();
    if (hash1==hash2) {
      return o1.toString().compareTo(o2.toString());
    }
    if (hash1<hash2) return -1;
    return 1;
  }

  public boolean equals(Object obj) {
    if (obj == this) return true;
    return false;
  }
}