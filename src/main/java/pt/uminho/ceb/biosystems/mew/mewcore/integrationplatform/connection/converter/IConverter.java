package pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.connection.converter;

import java.util.List;

import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;

import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.OverrideSteadyStateModel;

public interface IConverter {
	
	void init() throws Exception;
	
	void sendModel(ISteadyStateModel model, String modelName)  throws Exception;
	
	void sendObjectiveFunction(MapStringNum objectiveFunctionsList) throws Exception;
	
	void sendModifiedModel(OverrideSteadyStateModel overrideModel) throws Exception;
	
	//SteadyStateSimulationResult simulate() throws Exception;
	
	
	
	
	void sendInteger(String variableName, int variableValue) throws Exception;
	
	void sendIntegerList(String variableName, List variableValue) throws Exception;
	
	void sendDouble(String variableName, double variableValue) throws Exception;
	
	void sendDoubleList(String variableName, List variableValue) throws Exception;
	
	void sendString(String variableName, String variableValue) throws Exception;
	
	void sendStringList(String variableName, String[] variableValue) throws Exception;
	
	
	
	//void runFunction(String fName, String fInput, String fOutput);
	
	//void runFunction(String fName, String fOutput, String... fInput );
	
	//void runFunction(String fName, List<String> fInput, String fOutput);
	
	//void runFunction(String fName, String fInput, List<String> fOutput);
	
	void runFunction(String fName, List<String> fInput, List<String> fOutput);
	
	
	String getVariableString(String command) throws Exception;
	
	String[] getVariableStringList(String command) throws Exception;
	
	int getVariableInteger(String command) throws Exception;
	
	int[] getVariableIntegerList(String command) throws Exception;
	
	double getVariableDouble(String command) throws Exception;
	
	double[] getVariableDoubleList(String command) throws Exception;
	
	
	
	// Generic Methods
	
	void sendVariable(String variableName, String variableValue) throws Exception;
	
	Object getVariable(String command) throws Exception;
	
	void runCommand(String command) throws Exception;
	
//	void setParameters(Map<String, Object> param);

//	MatlabProxy getProxy();

//	void setProxy(MatlabProxy proxy);
}
