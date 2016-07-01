package zmodem.xfer.zm.util;

import zmodem.util.FileAdapter;
import zmodem.xfer.util.Arrays;
import zmodem.xfer.util.InvalidChecksumException;
import zmodem.xfer.zm.packet.*;
import zmodem.zm.io.ZMPacketInputStream;
import zmodem.zm.io.ZMPacketOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;



public class ZModemSend {
	
	private static final int packLen = 1024;
	
	private Map<String,FileAdapter> files;
	private Iterator<String> iter;
	private FileAdapter file;
	private String fileName;
	private int fOffset = 0;
	private boolean atEof = false;
	private InputStream fileIs;
	private InputStream netIs;
	private OutputStream netOs;
	
	
	public ZModemSend(Map<String,FileAdapter> fls,InputStream netin,OutputStream netout) throws IOException{
		files = fls;
		iter  = files.keySet().iterator();

		fOffset = 0;
		netIs  = netin;
		netOs  = netout;
	}
	
	public boolean nextFile() throws IOException{
		
		if(!iter.hasNext())
			return false;
		
		fileName = iter.next();
		
		file = files.get(fileName);
		fileIs = file.getInputStream();
		fOffset = 0;
		
		return true;
	}
	
	private void position(int offset) throws IOException{
		if(offset!=fOffset){
			fileIs.close();
			fileIs = file.getInputStream();
			fileIs.skip(offset);
			fOffset = offset;
		}
	}
	
	private byte[] getNextBlock() throws IOException{
		byte[] data = new byte[packLen];
		int len;
		
		len = fileIs.read(data);
		
		/* we know it is a file: all the data is locally available.*/
		if(len<data.length)
			atEof = true;

		fOffset += len;
		
		if(len!=data.length)
			return Arrays.copyOf(data,len);
		else
			return data;
	}
	
	private DataPacket getNextDataPacket() throws IOException{
		byte[] data = getNextBlock();
		ZModemCharacter fe = ZModemCharacter.ZCRCW;
		if(atEof){
			fe = ZModemCharacter.ZCRCE;
			fileIs.close();
		}
		
		return new DataPacket(fe, data);
	}
	
	public void send() {
		ZMPacketFactory factory = new ZMPacketFactory();
		
		ZMPacketInputStream is = new ZMPacketInputStream(netIs);
		ZMPacketOutputStream os = new ZMPacketOutputStream(netOs);
		
		
		try{
			
			boolean end = false;
			int errorCount = 0;
			ZMPacket packet = null;
			
			while(!end){
				try{
					packet = is.read();
				}catch(InvalidChecksumException ice){
					++errorCount;
					if(errorCount>20){
						os.write(new Cancel());
						end = true;
					}
				}
				
				if(packet instanceof Cancel){
					end = true;
				}
				
				if(packet instanceof Header){
					Header header = (Header)packet;
					
					switch(header.type()){
					case ZRINIT:
						if(!nextFile()){
							os.write(new Header(Format.BIN, ZModemCharacter.ZFIN));
						}else{
							os.write( new Header(Format.BIN, ZModemCharacter.ZFILE, new byte[]{0,0,0,ZMOptions.with(ZMOptions.ZCBIN)}) );
							os.write( factory.createZFilePacket(fileName, file.length()));
						}
						break;
					case ZRPOS:
						if(!atEof)
							position(header.getPos());
					case ZACK:	
						os.write(new Header(Format.BIN, ZModemCharacter.ZDATA, fOffset));
						os.write(getNextDataPacket());
						if(atEof)
							os.write(new Header(Format.HEX, ZModemCharacter.ZEOF, fOffset));
						break;
					case ZFIN:
						end = true;
						os.write(new Finish());
						break;
					default:
						end = true;
						os.write(new Cancel());
						break;
					}
					
				}
			}
		
		
		}catch (IOException e) {
			System.out.println("IO Exception in file: "+file+", "+e.getMessage());
		}
		
	}
}
