package zmodem.xfer.zm.packet;


import zmodem.xfer.util.*;
import zmodem.xfer.zm.util.ZDLEEncoder;
import zmodem.xfer.zm.util.ZMPacket;
import zmodem.xfer.zm.util.ZModemCharacter;

public class Header extends ZMPacket {
	
	public static Header unmarshall(Buffer buff){
		
		Format fmt = null;
		
		while(fmt==null)
			fmt = Format.fromByte(buff.get());

		if(fmt.hex())
			buff = buff.asHexBuffer();
			
		CRC crc = new CRC(fmt.crc());
		byte b;
		
		b = buff.get();
		crc.update(b);
		ZModemCharacter type = ZModemCharacter.forbyte(b);
		
		byte[] data = new byte[4];
		for(int i=0;i<data.length;i++){
			b = buff.get();
			crc.update(b);
			data[i] = b;
		}
		crc.finalize();
		
		byte[] netCrc = new byte[crc.size()];
		buff.get(netCrc);
				
		if(!Arrays.equals(netCrc, crc.getBytes()))
			throw new InvalidChecksumException();
		
		return new Header(fmt,type,data);
	}

	
	private Format format;
	private ZModemCharacter type;
	private byte[] data = {0,0,0,0};
	
	private Header(Format fFmt){
		format = fFmt;
	}
	
	public Header(Format fFmt,ZModemCharacter fType){
		this(fFmt);
		type = fType;
	}
	
	public Header(Format fFmt,ZModemCharacter fType, byte[] flags){
		this(fFmt,fType);
		setFlags(flags);
	}
	
	public Header(Format fFmt,ZModemCharacter fType, int pos){
		this(fFmt,fType);
		setPos(pos);
	}
	
	
	public ZModemCharacter type(){
		return type;
	}
	
	public Format format(){
		return format;
	}
	
	public void setFlags(byte[] flags){
		data = Arrays.copyOf(flags, flags.length);
	}
	
	public byte[] getFlags(){
		return data;
	}
	
	public void setPos(int num){
		data = Arrays.fromInt(num, Arrays.Endianness.Little);
	}
	
	public int getPos(){
		return Arrays.toInt(data,Arrays.Endianness.Little);
	}
	
	@Override
	public Buffer marshall(){
		ZDLEEncoder encoder;
		
		Buffer buff;
		if(format.hex())
			buff = HexBuffer.allocate(16);
		else
			buff = ByteBuffer.allocate(32);

		CRC crc = new CRC(format.crc());
				
		crc.update(type.value());
		buff.put(type.value());
		
		crc.update(data);
		encoder = new ZDLEEncoder(data, format);
		buff.put(encoder.zdle(),0,encoder.zdleLen());
		
		
		crc.finalize();
		
		encoder = new ZDLEEncoder(crc.getBytes(), format);
		buff.put(encoder.zdle(),0,encoder.zdleLen());
		
		buff.flip();
		
		return buff.asByteBuffer();
		
	}

	@Override
	public String toString() {
		return type+", "+format+", "+"{"+data[0]+","+data[1]+","+data[2]+","+data[3]+"}";
	}
	
}
