package pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.formulations.cobra;

import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.connection.matlab.MatlabConnection;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;

public class CobraMethods {

	public static String COBRAFBA = "Cobra FBA";
	public static String COBRAGEOMETRICFBA = "Cobra GeometricFBA";
	public static String COBRALMOMA = "Cobra LMOMA";
	public static String COBRAMOMA = "Cobra MOMA";
	public static String COBRAOPTKNOCK = "Cobra OptKnock";
	public static String COBRAGDLS = "Cobra GDLS";
	
	public static void test()
	{
		MatlabConnection conn;
		try {
			conn = new MatlabConnection();
			conn.init();
			
		} catch (MatlabConnectionException | MatlabInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
