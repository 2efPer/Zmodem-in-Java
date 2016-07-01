package zmodem.xfer.zm.packet;


import zmodem.xfer.util.Buffer;
import zmodem.xfer.util.ByteBuffer;
import zmodem.xfer.zm.util.ZMPacket;

public class Finish extends ZMPacket {

	@Override
	public Buffer marshall() {
		ByteBuffer buff = ByteBuffer.allocateDirect(16);
		
		for(int i=0;i<2;i++)
			buff.put((byte) 'O');
		
		buff.flip();
		
		return buff;
	}
	
	@Override
	public String toString() {
		return "Finish: OO";
	}

}
