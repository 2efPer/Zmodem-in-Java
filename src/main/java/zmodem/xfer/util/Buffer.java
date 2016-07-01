package zmodem.xfer.util;

public interface Buffer {
	public byte get() ;
    public Buffer get(byte[] dst, int offset, int len) ;
    public Buffer get(byte[] dst) ;
	public Buffer put(byte b) ;
	public Buffer put(byte[] dst, int offset, int len) ;
	public Buffer put(byte[] dst) ;
	public byte get(int index) ;
    public Buffer get(int index, byte[] dst, int offset, int len);
    public Buffer get(int index, byte[] dst) ;
	public Buffer put(int index, byte b) ;
	public Buffer put(int index, byte[] dst, int offset, int len) ;
	public Buffer put(int index, byte[] dst) ;
	public void flip();
	public int remaining();
	public boolean hasRemaining();
	public HexBuffer asHexBuffer();
	public ByteBuffer asByteBuffer();

}
