package zmodem;


import zmodem.util.FileAdapter;
import zmodem.xfer.zm.util.ZModemReceive;
import zmodem.xfer.zm.util.ZModemSend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;


public class ZModem {
	
	private InputStream netIs;
	private OutputStream netOs;


	public ZModem(InputStream netin,OutputStream netout){
		netIs  = netin;
		netOs  = netout;
		
	}
	
	public void receive(FileAdapter destDir) throws IOException{
		ZModemReceive sender = new ZModemReceive(destDir, netIs, netOs);
		sender.receive();
		netOs.flush();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	public void send(Map<String,FileAdapter> lst) throws IOException{
		ZModemSend sender = new ZModemSend(lst, netIs, netOs);
		sender.send();
		netOs.flush();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
}
