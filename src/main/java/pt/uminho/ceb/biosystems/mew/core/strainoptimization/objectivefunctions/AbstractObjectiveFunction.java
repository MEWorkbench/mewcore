package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractObjectiveFunction implements IObjectiveFunction, Serializable {
	
	private static final long	serialVersionUID		= 1L;
	public static final String	OBJECTIVE_FUNCTION_ID	= "objectiveFunctionID";
	
	protected Map<String, ObjectiveFunctionParameterType>	parameters;
	protected Map<String, Object>									values;
	
	public AbstractObjectiveFunction(Map<String,Object> configuration) throws InvalidObjectiveFunctionConfiguration{
		this();
		validate(configuration);
		for(String param : configuration.keySet()){
			setParameterValue(param, configuration.get(param));
		}
	}
	
	public AbstractObjectiveFunction(Object... params){
		this();
		if(params.length>0){
			processParams(params);
		}
	}
	
	public AbstractObjectiveFunction(){
		Map<String,ObjectiveFunctionParameterType> params = loadParameters();
		setParameters(params);
	}
	
	abstract protected void processParams(Object... params);
	
	public Map<String, ObjectiveFunctionParameterType> mandatoryParameters(){
		return parameters;
	}
	
	public abstract Map<String,ObjectiveFunctionParameterType> loadParameters();
	
	protected void setParameters(Map<String,ObjectiveFunctionParameterType> mandatoryParams){
		parameters = mandatoryParams;
	}
	
	public Map<String, Object> getValues() {
		if (values == null) {
			values = new HashMap<>();
		}
		return values;
	}
	
	public Object getParameterValue(String parameterID) {
		return values.get(parameterID);
	}
	
	public void setParameterValue(String parameterID, Object value) {
		getValues().put(parameterID, value);
	}
	
	public Map<String, Object> copyConfiguration() {
		Map<String, Object> newConf = new HashMap<>();
		
		for (String id : mandatoryParameters().keySet()) {
			newConf.put(id, values.get(id));
		}
		
		return newConf;
	}
	
	public ObjectiveFunctionParameterType getParameterType(String param) {
		return mandatoryParameters().get(param);
	}
	
	public void validate(Map<String, Object> values) throws InvalidObjectiveFunctionConfiguration {
		for (String param : mandatoryParameters().keySet()) {
			if (!values.containsKey(param)) {
				throw new InvalidObjectiveFunctionConfiguration("Missing parameter [" + param + "]");
			}
		}
	}
	
}
