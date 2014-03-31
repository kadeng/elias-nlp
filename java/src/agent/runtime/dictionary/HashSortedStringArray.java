/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.dictionary;


import java.util.Arrays;

public final class HashSortedStringArray implements java.io.Serializable {
        static final long serialVersionUID = -8092153229529024862L;

	int hashcodes[];
	byte characters[][];
	int length;

	public HashSortedStringArray(String array[], boolean keepSrc) {
		Arrays.sort(array, HashStringComparator.instance);
		length = array.length;
		hashcodes = new int[length];
		characters = new byte[length][];
		if (keepSrc) {
			for (int i = 0; i < length; i++) {
				hashcodes[i] = array[i].hashCode();
				try {
                                  characters[i] = array[i].getBytes("UTF-8");
			        } catch (Exception exx) {
                                  exx.printStackTrace(); // should never happen, since UTF 8 has to be supported by Java standard
                                }
                        }
		} else {
			for (int i = 0; i < length; i++) {
				hashcodes[i] = array[i].hashCode();
				try {
                                  characters[i] = array[i].getBytes("UTF-8");
			        } catch (Exception exx) {
                                  exx.printStackTrace(); // should never happen, since UTF 8 has to be supported by Java standard
                                }
				array[i] = null;
				// Remove that reference, so that the String may be collected
			}
		}
	}


	private static boolean equals(byte bytes[], byte strbytes[]) {
		if (bytes.length != strbytes.length) {
			return false;
		}
		int length = bytes.length;
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] != strbytes[i]) {
				return false;
			}
		}
		return true;
	}


	public int getStringIndex(String str) {
		int code = str.hashCode();
		int f = Arrays.binarySearch(hashcodes, code);
		if (f < 0) {
			return f;
		}
                byte strbytes[] = null;
                try {
                  strbytes = str.getBytes("UTF-8");
		} catch (Exception exc) {
                  exc.printStackTrace();
                  return -1;
                }
                if (equals(characters[f], strbytes)) {
			return f;
		}
		for (int i = f - 1; f >= 0; f--) {
			if (hashcodes[i] != code) {
				break;
			}
			if (equals(characters[i], strbytes)) {
				return i;
			}
		}
		for (int i = f + 1; f < length; f++) {
			if (hashcodes[i] != code) {
				break;
			}
			if (equals(characters[i], strbytes)) {
				return i;
			}
		}
		return -f;
	}

        public boolean contains(String str) {
          return (getStringIndex(str)>=0);
        }

        public int countDuplicateHashEntries() {
          int dupe = 0;
          for (int i=0;i<length-1;i++) {
            if (hashcodes[i]==hashcodes[i+1]) dupe++;
          }
          return dupe;
        }

        public int countDuplicateEntries() {
          int dupe = 0;
          for (int i=0;i<length-1;i++) {
            if ((hashcodes[i]==hashcodes[i+1]) && (equals(characters[i], characters[i+1]))) {
              dupe++;
            }
          }
          return dupe;
        }

        public String getStringAt(int index) {
          try {
            return new String(characters[index], "UTF-8");
          } catch (Exception exx) {
            exx.printStackTrace();
            return null;
          }
        }

        public byte[] getUTF8BytesAt(int index) {
          return characters[index];
        }

        public int getLength() {
          return length;
        }

}
