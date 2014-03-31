package elias.agent.runtime.pattern;

/**
 *  Represents a Dynamically growing array of int values.
 *
 *@author     Kai Londenberg
 */
public final class DynIntArray {
	/**
         * Integer array containing all of the values.
         */
        int array[];
        /**
         * Size of the array.
         */
	int size;


	/**
	 *
	 *@param  size  initial size of the array
	 */
	public DynIntArray(int size) {
		array = new int[size];
		this.size = size;
	}


	/**
	 *  Sets a given array index to a given value. Dynamically grows the array
         *  if neccessary
	 */
	public final void set(int index, int value) {
		try {
			array[index] = value;
		}
		catch (ArrayIndexOutOfBoundsException aioobe) {
			int newsize = 2 + ((size * 3) >> 1);
			// 1+(size*1.5)
			int newarray[] = new int[newsize];
			System.arraycopy(array, 0, newarray, 0, size);
			size = newsize;
			array = newarray;
			set(index, value);
		}
	}

}
