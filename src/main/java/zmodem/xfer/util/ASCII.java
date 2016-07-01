package zmodem.xfer.util;

public enum ASCII{
	
	SOH ((byte)0x01),
	STX ((byte)0x02),
	EOT ((byte)0x04),
	ENQ ((byte)0x05),
	ACK ((byte)0x06),
	BS  ((byte)0x08),
	LF  ((byte)0x0a),
	CR  ((byte)0x0d),
	XON ((byte)0x11),
	XOFF((byte)0x13),
	NAK ((byte)0x15),
	CAN ((byte)0x18);
	
	private byte value;

	private ASCII(byte b){
		value = b;
	}
	
	public byte value() {
		return value;
	}
}
