package zmodem.xfer.zm.util;

public enum ZMOptions {
	
	CANFDX (0x01),	/* Rx can send and receive true FDX */
	CANOVIO(0x02),	/* Rx can receive data during disk I/O */
	CANBRK (0x04),	/* Rx can send a break signal */
	CANCRY (0x08),	/* Receiver can decrypt */
	CANLZW (0x10),	/* Receiver can uncompress */
	CANFC32(0x20),	/* Receiver can use 32 bit Frame Check */
	ESCCTL (0x40),	/* Receiver expects ctl chars to be escaped */
	ESC8   (0x80),	/* Receiver expects 8th bit to be escaped */
	ZCBIN  (0x01);
	
	private byte value;
	private ZMOptions(char b){
		value = (byte)b;
	}
	private ZMOptions(int b){
		value =(byte) b;
	}
	private ZMOptions(byte b){
		value = b;
	}
	
	
	public byte value() {
		return value;
	}
	
	public static byte with(ZMOptions ... oo){
		byte r=0;
		for(ZMOptions o:oo)
			r = (byte)(r|o.value());
		return r;
	}
	
	public static ZMOptions forbyte(byte b) {
		for(ZMOptions zb : values()){
			if(zb.value()==b)
				return zb;
		}
		return null;
	}

}
