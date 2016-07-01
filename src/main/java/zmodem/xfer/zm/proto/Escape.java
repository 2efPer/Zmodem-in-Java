package zmodem.xfer.zm.proto;

import zmodem.xfer.zm.util.ZModemCharacter;

import java.util.HashMap;
import java.util.Map;



public class Escape {
	
	private int  len = 0;
	private Action action = Action.ESCAPE;
	
	
	public Escape(Action a){
		this(a,0);
	}
	
	
	public Escape(Action a,int l){
		len = l;
		action = a;
	}
	
	public Action action() {
		return action;
	}
	
	
	public int len() {
		return len;
	}
	
	

	private static Map<Byte, Escape> _specials = new HashMap<Byte, Escape>();
	
	static{
		_specials.put(ZModemCharacter.ZBIN.value()  ,new Escape(Action.HEADER,7 ));               
		_specials.put(ZModemCharacter.ZHEX.value()  ,new Escape(Action.HEADER,16));                 
		_specials.put(ZModemCharacter.ZBIN32.value(),new Escape(Action.HEADER,9 ));
		_specials.put(ZModemCharacter.ZCRCE.value() ,new Escape(Action.DATA  ,2 )); 
		_specials.put(ZModemCharacter.ZCRCG.value() ,new Escape(Action.DATA  ,2 )); 
		_specials.put(ZModemCharacter.ZCRCQ.value() ,new Escape(Action.DATA  ,2 )); 
		_specials.put(ZModemCharacter.ZCRCW.value() ,new Escape(Action.DATA  ,2 ));
	}
	
	
	
	public static Escape detect(byte b, boolean acceptsHeader){
		Escape r = _specials.get(b);
		
		
		if(r==null || ((!acceptsHeader) && r.action()==Action.HEADER))
			return new Escape(Action.ESCAPE);
		
		return r;
	}
	
	public static boolean mustEscape(byte b,byte previous, boolean escapeCtl){
		switch(b){
		case 0xd:
		case (byte)0x8d:
			if (escapeCtl &&  previous=='@')
				return true;
			break;
		case 0x18:
		case 0x10:
		case 0x11:
		case 0x13:
		case (byte) 0x7f:
		case (byte) 0x90:
		case (byte) 0x91:
		case (byte) 0x93:
		case (byte) 0xff:
			return true;
		default:
			if (escapeCtl && ((b & 0x60)==0) )
				return true;
		}
		return false;
	}
	
	public static byte escapeIt(byte b){
		if(b==(byte)0x7f)
			return ZModemCharacter.ZRUB0.value();
		if(b==(byte)0xff)
			return ZModemCharacter.ZRUB1.value();
		if(b==(byte)ZModemCharacter.ZRUB0.value())
			return 0x7f;
		if(b==(byte)ZModemCharacter.ZRUB1.value())
			return (byte)0xff;		
		
		return (byte)(b^0x40);
	}
	
	@Override
	public String toString() {
		return "Action="+action+", len="+len;
	}
}
