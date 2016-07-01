package zmodem.zm.io;

import zmodem.xfer.io.ObjectOutputStream;
import zmodem.xfer.util.ASCII;
import zmodem.xfer.util.Buffer;
import zmodem.xfer.zm.packet.DataPacket;
import zmodem.xfer.zm.packet.Format;
import zmodem.xfer.zm.packet.Header;
import zmodem.xfer.zm.util.ZMPacket;
import zmodem.xfer.zm.util.ZModemCharacter;

import java.io.IOException;
import java.io.OutputStream;



public class ZMPacketOutputStream extends ObjectOutputStream<ZMPacket> {

	private OutputStream os;
	
	public ZMPacketOutputStream(OutputStream netOs) {
		os = netOs;
	}

	public void implWrite(byte b) throws IOException{
		//System.out.printf("%02x",b);
		os.write(b);
	}
	
	@Override
	public void write(ZMPacket o) throws IOException {
		Buffer buff = o.marshall();
		Format fmt = null;
		
		if(o instanceof Header)
			fmt = ((Header)o).format();
		
	
		if(fmt!=null){
			for(int i=0;i<fmt.width();i++)
				implWrite(ZModemCharacter.ZPAD.value());
			
			implWrite(ZModemCharacter.ZDLE.value());
			implWrite(fmt.character());
		}
		
		while(buff.hasRemaining())
			implWrite(buff.get());
		
		if(fmt!=null) if(fmt.hex()){
			implWrite(ASCII.CR.value());
			implWrite(ASCII.LF.value());
			implWrite(ASCII.XON.value());
		}

		if(o instanceof DataPacket) if( ((DataPacket)o).type()==ZModemCharacter.ZCRCW)
			implWrite(ASCII.XON.value());
		
		
		os.flush();
		
		//System.out.println(" >> "+o);
	}

}
