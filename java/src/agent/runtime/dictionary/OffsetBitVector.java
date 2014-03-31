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


/**
 * Read-only bit vector implementation for int[] data arrays. offset means the offset into the
 * int array, not into the bits (which always have to start at int boundaries)
 */
public class OffsetBitVector implements java.io.Serializable {
    static final long serialVersionUID = 4127414705944742103L;

  int flags[];
  int offset;

  static int clearTable[] = new int[32];
  static int setTable[] = new int[32];
  static int keepEndTable[] = new int[33];
  static int keepStartTable[] = new int[33];

  private static Stack bitVectorPool = new Stack();

  public static void freeInstance(OffsetBitVector bv) {
    bv.flags = null;
    bitVectorPool.push(bv);
  }

  public static OffsetBitVector newInstance(int data[], int offset) {
    OffsetBitVector res = null;
    if (bitVectorPool.size()>0) {
      res = (OffsetBitVector) bitVectorPool.pop();
      res.flags = data;
      res.offset = offset;
      return res;
    }
    return new OffsetBitVector(data, offset);
  }

  static void dbmsg(String msg) {
    System.out.println(msg);
    System.out.flush();
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

  public void setData(int data[], int offset) {
    this.offset = offset;
    this.flags = data;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public OffsetBitVector(int data[], int offset) {
    flags = data;
    this.offset = offset;
  }

  int getRange(int from, int length) {
    if (length==0) return 0;
    int to = from+length-1;
    int fromoff = (from >>> 5) + offset;
    int tooff = (to >>> 5) + offset;
    int startbit = from & 31;
    int ret = (flags[fromoff] >>> startbit) & keepStartTable[length];
    if (fromoff!=tooff) {
      int endbit = (to & 31)+1;
      ret |= ((flags[tooff] & keepStartTable[endbit]) << (32-startbit));
    }
    return ret;
  }

  boolean getBit(int index) {
    int bitoffset = (index >>> 5) + offset;
    int pos = index & 31; // Modulo 32
    return ((flags[bitoffset] & setTable[pos])!=0);
  }

}