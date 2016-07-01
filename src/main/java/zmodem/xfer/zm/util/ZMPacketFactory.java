package zmodem.xfer.zm.util;


import zmodem.xfer.zm.packet.DataPacket;

public class ZMPacketFactory {
	
	public ZMPacketFactory(){}
	
	public DataPacket createZFilePacket(String pathname, long flen){
		return createZFilePacket(pathname,flen,0,"0",0,0);
	}
	public DataPacket createZFilePacket(String pathname, long flen, long ts, String mode/*octal*/
			                            ,int remainingfiles,long remainingBytes){
	
		StringBuilder builder = new StringBuilder();
		
		builder.append(pathname);
		builder.append('\0');
		builder.append(flen);
		builder.append(' ');
		builder.append(ts);
		builder.append(' ');
		builder.append(mode);
		builder.append(' ');
		builder.append('0');
		builder.append(' ');
		builder.append(remainingfiles);
		builder.append(' ');
		builder.append(remainingBytes);
		builder.append('0');
		
		return new DataPacket(ZModemCharacter.ZCRCW,builder.toString().getBytes());
	}
	
	
}
