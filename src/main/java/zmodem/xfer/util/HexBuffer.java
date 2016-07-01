package zmodem.xfer.util;


import zmodem.xfer.util.Arrays.Endianness;

public class HexBuffer implements Buffer{
	
	private static final byte[] hx={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	
	public static byte[] binToHex(byte[] bin){
		byte[] hex = new byte[bin.length*2];
		for(int i=0;i<bin.length;i++)
			System.arraycopy(toHex(bin[i]), 0, hex, i*2, 2);
		
		return hex;
	}
	public static byte[] hexToBin(byte[] hex){
		byte[] bin = new byte[hex.length/2];
		for(int i=0;i<bin.length;i++){
			byte[] bn= new byte[2];
			System.arraycopy(hex, i*2, bn, 0, 2);
			bin[i] = toByte(bn);
		}
		
		return bin;
	}
	
	private static byte toByte(byte[] array){
		int d;
		
		d = java.util.Arrays.binarySearch(hx, array[0]) * 16;
		d+= java.util.Arrays.binarySearch(hx, array[1]);
		
		return (byte)d;
	}
	
	private static byte[] toHex(byte b){
		byte[] array = new byte[2];
		
		array[0] = hx[((b>>4)&0xF)];
		array[1] = hx[(b&0xF)];
		
		return array;
	}
	
	private java.nio.ByteBuffer _wrapped;
	
	protected HexBuffer(java.nio.ByteBuffer b){
		_wrapped = b;
	}

	public static HexBuffer allocate(int capacity){
		return new HexBuffer(java.nio.ByteBuffer.allocate(capacity*2));
	}

	public static HexBuffer allocateDirect(int capacity){
		return new HexBuffer(java.nio.ByteBuffer.allocateDirect(capacity*2));
	}
	
	public Buffer slice() {
		return new HexBuffer(_wrapped.slice());
	}

	public Buffer duplicate() {
		return new HexBuffer(_wrapped.duplicate());
	}

	public Buffer asReadOnlyBuffer() {
		return new HexBuffer(_wrapped.asReadOnlyBuffer());
	}

	public byte get() {
		return toByte(new byte[]{_wrapped.get(),_wrapped.get()});
	}

    public Buffer get(byte[] dst, int offset, int len) {
    	for(;offset<len;offset++)
    		dst[offset] = get();
    	
    	return this;
    }
	
    public Buffer get(byte[] dst) {
    	return get(dst, 0, dst.length);
    }
	
	public Buffer put(byte b) {
		_wrapped.put(toHex(b));
		return this;
	}
	
	public Buffer put(byte[] dst,int offset,int len) {
		for(;offset<len;offset++)
			put(dst[offset]);
				
		return this;
	}
	
	public Buffer put(byte[] dst) {
		return put(dst,0,dst.length);
	}

	public byte get(int index) {
		return  toByte(new byte[]{_wrapped.get(index*2),_wrapped.get(index*2+1)});
	}
	
    public Buffer get(int index,byte[] dst, int offset, int len) {
    	for(;offset<len;offset++)
    		dst[offset] = get(index++);
    	
    	return this;
    }
	
    public Buffer get(int index,byte[] dst) {
    	return get(index,dst, 0, dst.length);
    }

	public Buffer put(int index, byte b) {
		byte[] array = toHex(b);
		_wrapped.put(index*2,array[0]);
		_wrapped.put(index*2+1,array[1]);
		return this;
	}
	
	public Buffer put(int index,byte[] dst,int offset, int len) {
		for(;offset<len;offset++)
			put(index++,dst[offset]);
				
		return this;
	}
	public Buffer put(int index,byte[] dst) {
		return put(index,dst,0,dst.length);
	}

	public Buffer compact() {
		_wrapped.compact();
		return this;
	}

	public boolean isDirect() {
		return _wrapped.isDirect();
	}

	public char getChar() {
		return (char)get();
	}

	public Buffer putChar(char value) {
		return put((byte)value);
	}

	public char getChar(int index) {
		return (char)get(index);
	}

	public Buffer putChar(int index, char value) {
		return put(index,(byte)value);
	}

	public ByteBuffer asByteBuffer() {
		return new ByteBuffer(_wrapped);
	}

	public short getShort() {
		return Arrays.toShort(new byte[]{get(),get()}, Endianness.Little);
	}

	public Buffer putShort(short value) {
		return put(Arrays.fromShort(value, Endianness.Little));
	}

	public short getShort(int index) {
		return Arrays.toShort(new byte[]{get(index),get(index+1)}, Endianness.Little);
	}

	public Buffer putShort(int index, short value) {
		return put(index,Arrays.fromShort(value, Endianness.Little));
	}

	public int getInt() {
		return Arrays.toInt(new byte[]{get(),get(),get(),get()}, Endianness.Little);
	}

	public Buffer putInt(int value) {
		return put(Arrays.fromInt(value, Endianness.Little));
	}

	public int getInt(int index) {
		return Arrays.toInt(new byte[]{get(index),get(index+1),get(index+2),get(index+3)}, Endianness.Little);
	}

	public Buffer putInt(int index, int value) {
		return put(index,Arrays.fromInt(value, Endianness.Little));
	}

	public long getLong() {
		return Arrays.toLong(new byte[]{get(),get(),get(),get(),get(),get(),get(),get()}, Endianness.Little);
	}

	public Buffer putLong(long value) {
		return put(Arrays.fromLong(value, Endianness.Little));
	}

	public long getLong(int index) {
		return Arrays.toLong(new byte[]{get(index),get(index+1),get(index+2),get(index+3),get(index+4),get(index+5),get(index+6),get(index+7)}, Endianness.Little);
	}

	public Buffer putLong(int index, long value) {
		return put(index,Arrays.fromLong(value, Endianness.Little));
	}

	public boolean isReadOnly() {
		return _wrapped.isReadOnly();
	}

	public void flip() {
		_wrapped.flip();
	}

	public int remaining() {
		double rem = ((double)_wrapped.remaining() /2.0d);
		return (int)Math.floor(rem);
	}

	public boolean hasRemaining() {
		return (_wrapped.remaining()>1);
	}

	public HexBuffer asHexBuffer() {
		return this;
	}
	
}
