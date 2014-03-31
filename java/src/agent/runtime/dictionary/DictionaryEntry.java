package elias.agent.runtime.dictionary;

/**
 * Title:        Elias Agent
 * Description:  Ihre Beschreibung
 * Copyright:    Copyright (c) 1999
 * Company:
 * @author Kai Londenberg
 * @version
 */

 import java.io.*;

public final class DictionaryEntry implements java.io.Serializable {
      static final long serialVersionUID = -332798523721450398L;

  /**
   * Entry String encoded in UTF-8 Format. It's hash code is in the dictionary.hashCodes[]
   * array.
   */
  byte entryBytes[];

  /**
   * <PRE>
   * Entry data. This data consists of two main parts:
   *
   * Header: The information global to each entry.
   *    The header consist of:
   *      - A bit-field with a length equal to the amount of word classes
   *        (dictionary.wordClassCount). Every bit represents a word class.
   *        If this entry has an interpretation which allows it to be treated as
   *        a given word class, the corresponding bit in the bitfield is set.
   *      - A bit field with a length equal to dictionary.interpretationCountBits.
   *        This field is to be interpreted as the amount of interpretations.
   *        It will be referred to as iCount
   *      - A bit field with a length equal to dictionary.interpretationLengthBits.
   *        This field is to be interpreted as the length (in integer entries) of
   *        an interpretation entry.
   *        It will be referred to as iLength
   *      - A bit field with a length equal to dictionary.valueCountBits
   *        This field is to be interpreted as the amount of distinct values found
   *        in the free value attributes of the entry.
   *        It will be referred to as vCount
   *      - vCount bit fields with a length equal to dictionary.valueOffsetBits
   *        These fields hold indices into the dictionary.values[] array, and map a given
   *        free value entry to the corresponding ValueAttribute.
   *
   *        Following this header, are iCount interpretation data fields. These are all
   *        aligned to integer boundaries so that a fast access is possible. Obviously, the
   *        offset of the first interpretation gets determined by vCount. Therefore, if vCount is
   *        given, dictionary.skipHeader[vCount] contains the pre-calculated offset position of the first
   *        interpretation. The offset of interpretation i may be calculated as:
   *        Offset = dictionary.skipHeader[vCount] + i * iLength.
   *
   *        A given Interpretation consists of:
   *
   *        - A bit field of the length dictionary.wordClassBits.
   *          This field is the index of the word class for this interpretation. This
   *          index will be referred to as wcIDX. The word class for this interpretation
   *          is wordClass = dictionary.wordClasses[wcIDX]
   *        - a bit field with a size of wordClass.bitLength bits.
   *          This contains the tag values to be interpreted according to the word class
   *        - a bit field with a size of vCount - each bit represents a given free value
   *          for this dictionary entry, and is set if this interpretation contained the value,
   *          and is cleared if not.
   *  </PRE>
   */
  int dataBitVector[];



  public DictionaryEntry() {
  }

  public void writeTo(ObjectOutputStream out) throws IOException {
      out.writeInt(entryBytes.length);
      for (int i=0;i<entryBytes.length;i++) {
        out.writeByte(entryBytes[i]);
      }
      out.writeInt(dataBitVector.length);
      if (dataBitVector.length>10000) throw new IOException("Data bit vector too long.");
      for (int i=0;i<dataBitVector.length;i++) {
        out.writeInt(dataBitVector[i]);
      }
  }

  public static void msg(String txt) {
          System.out.println(txt);
          System.out.flush();
  }

  void readFrom(ObjectInputStream in) throws Exception {
      entryBytes = new byte[in.readInt()];
      for (int i=0;i<entryBytes.length;i++) {
        entryBytes[i] = in.readByte();
      }
      dataBitVector = new int[in.readInt()];
      for (int i=0;i<dataBitVector.length;i++) {
        dataBitVector[i] = in.readInt();
      }
  }

  public static DictionaryEntry readDictionaryEntryFrom(ObjectInputStream in) throws Exception {
    DictionaryEntry res = new DictionaryEntry();
    res.readFrom(in);
    return res;
  }

  public String toString() {
    try {
      return new String(entryBytes, "UTF-8");
    } catch (UnsupportedEncodingException usee) {} // impossible
    return null;
  }


}