/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.runtime.dictionary;

import java.io.*;
import java.util.Arrays;

public class AttributeValue implements Serializable, Comparable {
	final static long serialVersionUID = 2395663164496417913L;
	Attribute attribute;
	int hashCode;
	byte data[];


	protected AttributeValue(Attribute attribute, byte data[], int hashCode) {
		this.attribute = attribute;
		this.hashCode = hashCode;
		this.data = data;
	}


	protected AttributeValue(Attribute attribute, String value) {
		this.attribute = attribute;
		this.hashCode = value.hashCode();
		try {
			data = value.getBytes("UTF-8");
		} catch (UnsupportedEncodingException uee) {
		}
		// impossible, since UTF-8 has to be supported
	}


	public static AttributeValue newInstance(Attribute attribute, String value) {
		if (!attribute.isBackreference()) {
			return new AttributeValue(attribute, value);
		}
		return new BackreferenceAttributeValue(attribute, value);
	}


	public byte[] getData() {
		return data;
	}


	public Attribute getAttribute() {
		return attribute;
	}


	public String toString() {
		try {
			return new String(data, "UTF-8");
		} catch (UnsupportedEncodingException uee) {
			return null;
			// impossible
		}
	}


	public int hashCode() {
		return hashCode;
	}


	public boolean equals(Object obj) {
		if (!(obj instanceof AttributeValue)) {
			return false;
		}
		if (obj.hashCode() != hashCode) {
			return false;
		}
		AttributeValue cva = (AttributeValue) obj;
		if ((cva.attribute == attribute) && Arrays.equals(cva.data, data)) {
			return true;
		}
		return false;
	}


	public int compareTo(Object o) {
		AttributeValue va = (AttributeValue) o;
		if (hashCode == va.hashCode) {
			if (va.attribute != attribute) {
				return attribute.getIdentifier().compareTo(va.attribute.getIdentifier());
			}
			if (Arrays.equals(va.data, data)) {
				return 0;
			}
			// pretty unlikely we continue past this point
			if (data.length < va.data.length) {
				return -1;
			}
			if (data.length > va.data.length) {
				return 1;
			}
			for (int i = 0; i < va.data.length; i++) {
				if (data[i] < va.data[i]) {
					return -1;
				}
				if (data[i] > va.data[i]) {
					return 1;
				}
			}
			return 0;
			// impossible, since equality has been ruled out already
		}
		if (hashCode > va.hashCode) {
			return 1;
		}
		return -1;
	}

        public boolean isBackreference() {
          return false;
        }

}
