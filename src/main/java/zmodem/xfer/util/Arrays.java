package zmodem.xfer.util;

/**
 * copyOf is not in java 5 java.util.Array
 * @author justin
 *
 */
public class Arrays {
	public enum Endianness{Little,Big;}
    public static byte[] copyOf(byte[] original, int newLength) {
        byte[] copy = new byte[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

	public static boolean equals(byte[] a, byte[] a2) {
		return java.util.Arrays.equals(a, a2);
	}

	public static long toInteger(byte[] array, int size, Endianness endian){
		long n = 0;
		int offset=0,increment=1;
		switch(endian){
		case Little:
			increment = 1;
			offset    = 0;
			break;
		case Big:
			increment = -1;
			offset    = size-1;
			break;

		}
		
		for(int i=0;i<size;i++){
			n += (0xff&array[offset]) * (0x1 << i*8);
			offset += increment;
		}
		
		return n;
	}
	
	public static short toShort(byte[] array, Endianness endian){
		return (short)toInteger(array,2,endian);
	}
	
	public static int toInt(byte[] array, Endianness endian){
		return (int)toInteger(array,4,endian);
	}
	
	public static long toLong(byte[] array, Endianness endian){
		return toInteger(array,8,endian);
	}
	
	public static byte[] fromInteger(long n,int size, Endianness endian){
		byte[] ret = new byte[size];
		int offset=0,increment=1;
	
		switch(endian){
		case Big:
			increment = -1;
			offset    = size-1;
			break;
		case Little:
			increment = 1;
			offset    = 0;
			break;
		}
		
		for(int i=0;i<size;i++){
			ret[offset] = (byte) ( (n >> (i*8) ) & 0xFF);
			offset += increment;
		}
		
		return ret;
	}
	
	public static byte[] fromShort(short s,Endianness endian){
		return fromInteger(s, 2, endian);
	}
	public static byte[] fromInt(int i,Endianness endian){
		return fromInteger(i, 4, endian);
	}
	public static byte[] fromLong(long i,Endianness endian){
		return fromInteger(i, 8, endian);
	}
}
