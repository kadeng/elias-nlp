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

public final class HashComparator implements Comparator {

  public static final HashComparator instance = new HashComparator();

  private HashComparator() {
  }

  public int compare(Object o1, Object o2) {
    return o1.hashCode()-o2.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == this) return true;
    return false;
  }
}