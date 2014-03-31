package elias.agent.runtime.dictionary;

/**
 * Title:        Elias Agent
 * Description:  Ihre Beschreibung
 * Copyright:    Copyright (c) 1999
 * Company:
 * @author Kai Londenberg
 * @version
 */

 import java.util.*;

public final class BitVector implements java.io.Serializable {
  static final long serialVersionUID = -7031459181815071548L;

  int flags[] = new int[0];
  int size = 0;

  static int clearTable[] = new int[32];
  static int setTable[] = new int[32];
  static int keepEndTable[] = new int[33];
  static int keepStartTable[] = new int[33];

  private static Stack bitVectorPool = new Stack();

  public static void freeInstance(BitVector bv) {
    bv.clear();
    bitVectorPool.push(bv);
  }

  public static BitVector newInstance() {
    BitVector res = null;
    if (bitVectorPool.size()>0) {
      res = (BitVector) bitVectorPool.pop();
      return res;
    }
    return new BitVector();
  }

  public static BitVector newInstance(int bits[]) {
    return new BitVector(bits);
  }

  static {
    keepEndTable[0] = ~(0);
    keepStartTable[0] = 0;
    for (int i=0;i<32;i++) {
      setTable[i] = 1 << i;
      clearTable[i] = ~setTable[i];
      keepStartTable[i+1] = keepStartTable[i] | (1 << i);
      keepEndTable[i+1] = ~keepStartTable[i+1];
    }
  }

  public BitVector() {
  }

  public BitVector(int bits[]) {
    flags = bits;
    size = bits.length * 32;
  }

  public void enlarge(int maxBitIndex) {
    if (maxBitIndex<size) return;
    size = maxBitIndex+1;
    int oldflags[] = flags;
    int length = 1;
    if (size>32) {
      length = (maxBitIndex >>> 5) + 1 ;
    }
    if (oldflags.length >= length) return;
    flags = new int[length];
    System.arraycopy(oldflags, 0, flags, 0, oldflags.length);
  }

  public void setBit(int index) {
    enlarge(index);
    int offset = (index >>> 5);
    int pos = index & 31; // Modulo 32
    flags[offset] |= setTable[pos];
  }

  void unsafeSetBit(int index) {
    int offset = (index >>> 5);
    int pos = index & 31; // Modulo 32
    flags[offset] |= setTable[pos];
  }


  public void clearBit(int index) {
    enlarge(index);
    int offset = (index >>> 5);
    int pos = index & 31; // Modulo 32
    flags[offset] &= clearTable[pos];
  }

  void unsafeClearBit(int index) {
    int offset = (index >>> 5);
    int pos = index & 31; // Modulo 32
    flags[offset] &= clearTable[pos];
  }


  public int getRange(int from, int length) {
    if (length==0) return 0;
    int to = from+length-1;
    enlarge(to);
    int fromoff = (from >>> 5);
    int tooff = (to >>> 5);
    int startbit = from & 31;
    int ret = (flags[fromoff] >>> startbit) & keepStartTable[length];
    if (fromoff!=tooff) {
      int endbit = (to & 31)+1;
      ret |= ((flags[tooff] & keepStartTable[endbit]) << (32-startbit));
    }
    return ret;
  }

  public int unsafeGetRange(int from, int length) {
    if (length==0) return 0;
    int to = from+length-1;
    int fromoff = (from >>> 5);
    int tooff = (to >>> 5);
    int startbit = from & 31;
    int ret = (flags[fromoff] >>> startbit) & keepStartTable[length];
    if (fromoff!=tooff) {
      int endbit = (to & 31)+1;
      ret |= ((flags[tooff] & keepStartTable[endbit]) << (32-startbit));
    }
    return ret;
  }


  public void setRange(int from, int length, int values) {
    if (length==0) return;
    int to = from+length-1;
    enlarge(to);
    values = values & keepStartTable[length];
    int offset = (from >>> 5);
    int endoffset = (to >>> 5);
    int startbit = from & 31;
    int endbit = (to & 31)+1;
    int mask;
    if (offset==endoffset) {
      mask = keepStartTable[startbit] | keepEndTable[endbit];
      flags[offset] &= mask; // clear range to be set with OR Operation
      flags[offset] |= (values << startbit);
      return;
    }
    mask = keepStartTable[startbit];
    flags[offset] &= mask;
    flags[offset] |= (values << startbit);
    mask = keepEndTable[endbit];
    values = values >>> (32-startbit);
    flags[endoffset] &= mask;
    flags[endoffset] |= values;
  }

  void unsafeSetRange(int from, int length, int values) {
    if (length==0) return;
    int to = from+length-1;
    values = values & keepStartTable[length];
    int offset = (from >>> 5);
    int endoffset = (to >>> 5);
    int startbit = from & 31;
    int endbit = (to & 31)+1;
    int mask;
    if (offset==endoffset) {
      mask = keepStartTable[startbit] | keepEndTable[endbit];
      flags[offset] &= mask; // clear range to be set with OR Operation
      flags[offset] |= (values << startbit);
      return;
    }
    mask = keepStartTable[startbit];
    flags[offset] &= mask;
    flags[offset] |= (values << startbit);
    mask = keepEndTable[endbit];
    values = values >>> (32-startbit);
    flags[endoffset] &= mask;
    flags[endoffset] |= values;
  }

  public void ORRange(int from, int length, int values) {
    if (length==0) return;
    int to = from+length-1;
    enlarge(to);
    int offset = (from >>> 5);
    int endoffset = (to >>> 5);
    int startbit = from & 31;
    flags[offset] |= (values << startbit);
    if (offset==endoffset) {
      return;
    }
    values = values >>> (32-startbit);
    flags[endoffset] |= values;
  }

  void unsafeORRange(int from, int length, int values) {
    if (length==0) return;
    int to = from+length-1;
    int offset = (from >>> 5);
    int endoffset = (to >>> 5);
    int startbit = from & 31;
    flags[offset] |= (values << startbit);
    if (offset==endoffset) {
      return;
    }
    values = values >>> (32-startbit);
    flags[endoffset] |= values;
  }


  public void ANDRange(int from, int length, int values) {
    if (length==0) return;
    int to = from+length-1;
    enlarge(to);
    values = values | keepEndTable[length];
    int offset = (from >>> 5);
    int endoffset = (to >>> 5);
    int startbit = from & 31;
    flags[offset] &= keepStartTable[startbit] | (values << startbit);
    if (offset==endoffset) {
      return;
    }
    values = values >>> (32-startbit);
    flags[endoffset] &= values;
  }

  void unsafeANDRange(int from, int length, int values) {
    if (length==0) return;
    int to = from+length-1;
    values = values | keepEndTable[length];
    int offset = (from >>> 5);
    int endoffset = (to >>> 5);
    int startbit = from & 31;
    flags[offset] &= keepStartTable[startbit] | (values << startbit);
    if (offset==endoffset) {
      return;
    }
    values = values >>> (32-startbit);
    flags[endoffset] &= values;
  }

  public boolean getBit(int index) {
    enlarge(index);
    int offset = (index >>> 5);
    if (offset>=flags.length) return false;
    int pos = index & 31; // Modulo 32
    return ((flags[offset] & setTable[pos])!=0);
  }

  public static void appendBitString(int bits, StringBuffer sbuf) {
    for (int i=0;i<32;i++) {
      if ((i>0) && ((i % 8)==0)) sbuf.append(',');
      if ((setTable[i] & bits)!=0) {
        sbuf.append('1');
      } else {
        sbuf.append('0');
      }
    }
  }

  public static String bitString(int bits) {
    StringBuffer result = new StringBuffer();
    appendBitString(bits, result);
    return result.toString();
  }

  boolean unsafeGetBit(int index) {
    int offset = (index >>> 5);
    if (offset>=flags.length) return false;
    int pos = index & 31; // Modulo 32
    return ((flags[offset] & setTable[pos])!=0);
  }


  public void clear() {
    size = 0;
    Arrays.fill(flags, 0);
  }

  public String toString() {
    StringBuffer s = new StringBuffer();
    int max=intSize();
    for (int i=0;i<max;i++) {
      appendBitString(flags[i], s);
      s.append(',');
    }
    return s.toString();
  }

  public BitVector copy() {
    int iLen = intSize();
    int copiedFlags[] = new int[iLen];
    System.arraycopy(flags, 0, copiedFlags, 0, iLen);
    BitVector result = new BitVector(copiedFlags);
    result.size = size;
    return result;
  }

  public int copyInto(int data[], int offset) {
    int iLen = intSize();
    System.arraycopy(flags, 0, data, offset, iLen);
    return iLen;
  }

  public void copyInto(BitVector bv) {
    bv.clear();
    if (size==0) return;
    int len = intSize();
    if (bv.flags.length<len) {
      bv.flags = new int[len];
    }
    bv.size = size;
    for (int i=0;i<len;i++) {
      bv.flags[i] = flags[i];
    }
  }

  public void AND(BitVector bv) {
    enlarge(bv.size-1);
    int iSize = intSize();
    int bviSize = bv.intSize();
    for (int i=0;i<bviSize;i++) {
      flags[i] &= bv.flags[i];
    }
    for (int i=bviSize;i<iSize;i++) {
      flags[i] = 0;
    }
  }

  public void AND_NOT(BitVector bv) {
    enlarge(bv.size-1);
    int bviSize = bv.intSize();
    for (int i=0;i<bviSize;i++) {
      flags[i] &= ~bv.flags[i];
    }
  }

  public void XOR(BitVector bv) {
    enlarge(bv.size-1);
    int bviSize = bv.intSize();
    for (int i=0;i<bviSize;i++) {
      flags[i] ^= bv.flags[i];
    }
  }

  public boolean equalTo(BitVector bv) {
    enlarge(bv.size-1);
    int iSize = intSize();
    int bviSize = bv.intSize();
    for (int i=0;i<bviSize;i++) {
      if (flags[i]!=bv.flags[i]) return false;
    }
    for (int i=bviSize;i<iSize;i++) {
      if (flags[i]!=0) return false;
    }
    return true;
  }

  public boolean equalToExceptMask(BitVector bv, BitVector mask) {
    mask.enlarge(bv.size-1);
    enlarge(bv.size-1);
    int iSize = intSize();
    int bviSize = bv.intSize();
    int miSize = mask.intSize();
    for (int i=0;i<bviSize;i++) {
      int flag = flags[i] & (~mask.flags[i]);
      int cmpFlag = bv.flags[i] & (~mask.flags[i]);
      if (flag!=cmpFlag) return false;
    }
    if (miSize>=iSize) {
      for (int i=bviSize;i<iSize;i++) {
        if ((flags[i] & (~mask.flags[i]))!=0) return false;
      }
    } else {
      for (int i=bviSize;i<miSize;i++) {
        if ((flags[i] & (~mask.flags[i]))!=0) return false;
      }
      for (int i=miSize;i<iSize;i++) {
        if (flags[i]!=0) return false;
      }
    }
    return true;
  }

  public void OR(BitVector bv) {
    enlarge(bv.size-1);
    int len = bv.intSize();
    for (int i=0;i<len;i++) {
      flags[i] |= bv.flags[i];
    }
  }

  public void NOT() {
    int len = intSize();
    for (int i=0;i<len;i++) {
      flags[i] = ~flags[i];
    }
    int trunc = size % 32;
    if (len>0) {
      flags[len-1] &= keepStartTable[trunc];
    }
  }

  public int bitSize() {
    return size;
  }

  public int intSize() {
    return minIntSize(size);
  }

  public int intCapacity() {
    return flags.length;
  }

  public boolean isNull() {
    for (int i=0;i<flags.length;i++) {
      if (flags[i]!=0) return false;
    }
    return true;
  }

  /**
   * Minimum number of integers required to hold bitCount bits
   */
  public static int minIntSize(int bitCount) {
        if (bitCount==0) return 0;
	return ((bitCount-1) >>> 5)+1;
  }

  public static void println(String txt) {
    System.out.println(txt);
  }

  public static void main(String args[]) { // Test run
    println("Running bit vector class test");
    BitVector bv = new BitVector();
    println("Empty bitvector: " + bv.toString());
    println("Setting bits 0 to 79 bit for bit");
    for (int i=0;i<79;i++) {
      bv.setBit(i);
    }
    println("Result: <" + bv.toString() + ">");
    println("Clearing bits 0 to 79 bit for bit");
    for (int i=0;i<79;i++) {
      bv.clearBit(i);
    }
    println("Result: <" + bv.toString() + ">");
    println("Setting bit 0 to 29 with OR operation");
    bv.ORRange(0, 30, ~0);
    println("Result: <" + bv.toString() + ">");
    println("Setting bit 40 to 69 with OR operation");
    bv.ORRange(40, 30, ~0);
    println("Result: <" + bv.toString() + ">");
    println("Clearing bit 1 to 29 with AND Operation");
    bv.ANDRange(1, 29, 0);
    println("Result: <" + bv.toString() + ">");
    println("Clearing bit 40 to 68 with AND Operation");
    bv.ANDRange(40, 29, 0);
    println("Result: <" + bv.toString() + ">");
    println("Setting bits 61,63, 64 and 66 bit for bit");
    bv.setBit(61);
    bv.setBit(63);
    bv.setBit(64);
    bv.setBit(66);
    println("Result: <" + bv.toString() + ">");
    println("Clearing range 62 to 67 with AND Operation");
    bv.ANDRange(62, 7, 0);
    println("Result: <" + bv.toString() + ">");
    println("OR combining range 61 to 67 with 101101");
    bv.ORRange(61, 6, Integer.parseInt("101101", 2));
    println("Result: <" + bv.toString() + ">");
    println("Getting 6 bits at position 61 - Result: " + bitString(bv.getRange(61, 6)));
    println("Getting 2 bits at position 63 - Result: " + bitString(bv.getRange(63, 3)));
    println("Getting 8 bits at position 59 - Result: " + bitString(bv.getRange(59, 8)));
    println("Setting 8 bits at position 59 to 11111111");
    bv.setRange(59, 8, Integer.parseInt("11111111111", 2));
    println("Result: <" + bv.toString() + ">");
    println("Clearing all bits.");
    bv.clear();
    int allbits = ~0;
    println("Setting all bits, from 0 to 95");
    bv.setRange(0,32, allbits);
    bv.setRange(32,32, allbits);
    bv.setRange(64,32, allbits);
    println("Result: <" + bv.toString() + ">");
    println("Getting 32 bits at position 0 - Result: " + bitString(bv.getRange(0, 32)));
    println("Getting 4 bits at position 30 - Result: " + bitString(bv.getRange(30, 4)));
    println("Getting 3 bits at position 34 - Result: " + bitString(bv.getRange(34, 3)));
    println("Getting 1 bit at  position 67 - Result: " + bitString(bv.getRange(67, 1)));
    println("Getting 0 bits at position 77 - Result: " + bitString(bv.getRange(67, 0)));

  }

}