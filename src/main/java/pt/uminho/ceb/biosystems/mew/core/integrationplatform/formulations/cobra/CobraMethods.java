package pt.uminho.ceb.biosystems.mew.core.integrationplatform.formulations.cobra;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.matlab.MatlabConnection;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.exceptions.MatlabNotFoundException;

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
			
		} catch (MatlabConnectionException | MatlabInvocationException | MatlabNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
