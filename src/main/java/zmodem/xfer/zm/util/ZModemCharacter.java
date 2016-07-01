package zmodem.xfer.zm.util;

public enum ZModemCharacter {
	ZPAD('*'),
	ZDLE(0x18), 
	ZDLEE(ZDLE.value()^0x40),      
	ZBIN('A'),                   
	ZHEX('B'),                 
	ZBIN32('C'),               
	ZCRCE('h'), 
	ZCRCG('i'), 
	ZCRCQ('j'), 
	ZCRCW('k'),
	ZRUB0('l'),
	ZRUB1('m'),
	ZRQINIT(0),     
	ZRINIT(1),      
	ZSINIT(2),      
	ZACK(3),        
	ZFILE(4),       
	ZSKIP(5),       
	ZNAK(6),        
	ZABORT(7),      
	ZFIN(8),        
	ZRPOS(9),       
	ZDATA(10),      
	ZEOF(11),       
	ZFERR(12),      
	ZCRC(13),       
	ZCHALLENGE(14), 
	ZCOMPL(15),     
	ZCAN(16),       
	ZFREECNT(17),   
	ZCOMMAND(18),   
	ZSTDERR(19);    

	private byte value;
	private ZModemCharacter(char b){
		value = (byte)b;
	}
	private ZModemCharacter(int b){
		value =(byte) b;
	}
	private ZModemCharacter(byte b){
		value = b;
	}
	
	
	public byte value() {
		return value;
	}
	
	public static ZModemCharacter forbyte(byte b) {
		for(ZModemCharacter zb : values()){
			if(zb.value()==b)
				return zb;
		}
		return null;
	}

}
