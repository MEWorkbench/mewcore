package pt.uminho.ceb.biosystems.mew.mewcore.utils;

public class Debugger {

	public static boolean debug = false;
	
	public static void debug(Object message) {
		if(debug)
			System.out.println("[DEBUG][metabolic] - " + message.toString());
	}
	
}
