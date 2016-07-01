package zmodem.xfer.zm.util;


import zmodem.xfer.zm.packet.Format;
import zmodem.xfer.zm.proto.Escape;

public class ZDLEEncoder {
	
	private byte[] raw;
	private byte[] zdle;
	private int    zdleLen;
	private Format format;
	
	
	public ZDLEEncoder(byte[] data){
		this(data,Format.BIN);
	}
	public ZDLEEncoder(byte[] data, Format fmt){
		raw = data;
		format = fmt;
		zdle = new byte[raw.length*2];
		encode();
	}
	
	private void putZdle(byte b){
		zdle[zdleLen] = b;
		zdleLen++;
	}
	
	private void encode(){
		byte previous = 0;
		for(byte b: raw){

			if( (!format.hex()) && Escape.mustEscape(b, previous, false)){
				putZdle(ZModemCharacter.ZDLE.value());
				b = Escape.escapeIt(b);
			}

			putZdle(b);
			previous = b;
		}
	}
	
	public byte[] raw(){
		return raw;
	}
	
	public int zdleLen(){
		return zdleLen;
	}
	
	public byte[] zdle(){
		return zdle;
		
	}
	
}
