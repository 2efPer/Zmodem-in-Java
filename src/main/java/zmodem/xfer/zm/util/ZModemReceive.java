package zmodem.xfer.zm.util;

import zmodem.util.FileAdapter;
import zmodem.xfer.util.InvalidChecksumException;
import zmodem.xfer.zm.packet.*;
import zmodem.zm.io.ZMPacketInputStream;
import zmodem.zm.io.ZMPacketOutputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class ZModemReceive {
	
	private FileAdapter destination;
	private int fOffset = 0;
	private String filename;
	
	private OutputStream fileOs = null;

	private InputStream netIs;
	private OutputStream netOs;
	
	private enum Expect{
		FILENAME,DATA,NOTHING;
	}
	
	
	public ZModemReceive(FileAdapter destDir,InputStream netin,OutputStream netout) throws IOException{
		
		if(!(destDir.isDirectory() && destDir.exists()) )
			throw new FileNotFoundException(destDir.getName());
		
		destination = destDir;
		netIs  = netin;
		netOs  = netout;
	}
	
	private FileAdapter getCurrentFile(){
		return destination.getChild(filename);
	}
	
	private int getPos(){
		FileAdapter f = getCurrentFile();
		
		if(f.exists())
			return (int)f.length();
		
		return 0;
	}
	
	private void open(int offset) throws IOException{
		boolean append = false;
		FileAdapter f = getCurrentFile();
		
		if(offset !=0 ){
			if(f.exists() && f.length() == offset)
				append = true;
			else
				offset = 0;
		}
		fileOs = f.getOutputStream(append);
		fOffset = offset;
	}
	
	private void decodeFileNameData(DataPacket p){
		StringBuilder buffer = new StringBuilder(128);
		for(byte b: p.data()){
			if(b==0) break;
			buffer.append((char)b);
		}
		
		filename = buffer.toString();
	}
	
	private void writeData(DataPacket p) throws IOException{
		
		fileOs.write(p.data());
		fOffset += p.data().length;
		
	}
	
	public void receive() {		
		ZMPacketInputStream is = new ZMPacketInputStream(netIs);
		ZMPacketOutputStream os = new ZMPacketOutputStream(netOs);
		
		Expect expect = Expect.NOTHING;
		
		byte[] recvOpt ={0,4,0,ZMOptions.with(ZMOptions.ESCCTL,ZMOptions.ESC8)};
		
		try{
			
			boolean end = false;
			int errorCount = 0;
			ZMPacket packet = null;
			while(!end){
				try{
					packet = is.read();
				}catch(InvalidChecksumException ice){
					ice.printStackTrace();
					++errorCount;
					if(errorCount>=3){
						os.write(new Cancel());
						end = true;
					}
				}
				
				if(packet instanceof Cancel){
					end = true;
				}
				
				if(packet instanceof Finish){
					end = true;
				}
				
				if(packet instanceof Header){
					Header header = (Header)packet;
					
					switch(header.type()){
					case ZRQINIT:
						os.write(new Header(Format.HEX, ZModemCharacter.ZRINIT, recvOpt));
						break;
					case ZFILE:
						expect = Expect.FILENAME;
						break;
					case ZEOF:
						os.write(new Header(Format.HEX, ZModemCharacter.ZRINIT, recvOpt));
						expect = Expect.NOTHING;
						filename = null;
						fileOs.flush();
						fileOs.close();
						fileOs = null;
						break;
					case ZDATA:
						open(header.getPos());
						expect = Expect.DATA;
						break;
					case ZFIN:
						os.write(new Header(Format.HEX, ZModemCharacter.ZFIN));
						end = true;
						break;
					default:
						end = true;
						os.write(new Cancel());
						break;
					}
				}
				
				if(packet instanceof DataPacket){
					DataPacket dataP = (DataPacket)packet;
					switch(expect){
					case NOTHING:
						os.write(new Header(Format.HEX, ZModemCharacter.ZRINIT, recvOpt));
						break;
					case FILENAME:
						decodeFileNameData(dataP);
						os.write(new Header(Format.HEX, ZModemCharacter.ZRPOS, getPos()));
						expect = Expect.NOTHING;
						break;
					case DATA:
						writeData(dataP);
						switch(dataP.type()){
						case ZCRCW:
							expect = Expect.NOTHING;
						case ZCRCQ:
							os.write(new Header(Format.HEX, ZModemCharacter.ZACK, fOffset));
							break;
						case ZCRCE:
							expect = Expect.NOTHING;
							break;
						}
					}
				}
			}
		
		
		}catch (IOException e) {
			System.out.println("IO Exception "+e.getMessage());
			/*e.printStackTrace();*/
		}
		
	}
}
