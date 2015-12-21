package pt.uminho.ceb.biosystems.mew.core.integrationplatform.formulations.cobra.simulation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.converter.ConnectionFormulation;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.matlab.MatlabConnection;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.exceptions.CobraMatlabFormulationException;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;

public class CobraGeometricFBAFormulation extends ConnectionFormulation{

	String modelName;
	Map<String, Double> obj_coef = null;
	
	public CobraGeometricFBAFormulation(ISteadyStateModel model) throws MatlabConnectionException, MatlabInvocationException {
		super(model, new MatlabConnection());
		initFormulation(model);
	}
	
	private void initFormulation(ISteadyStateModel model) {
		modelName = model.getId();
		if(modelName == null || modelName.equals(""))
			modelName = "model";
		if(modelName.startsWith("."))
			modelName = "DOT" + modelName;
	}

	@Override
	protected void initPropsKeys() {
		super.initPropsKeys();
		mandatoryProps.add(SimulationProperties.IS_MAXIMIZATION);
		possibleProperties.add(SimulationProperties.OBJECTIVE_FUNCTION);
	}
	
	
	@Override
	public void executeSimulationCommand() {
		try {
			getConverter().runFunction("geometricFBA", Arrays.asList(new String[]{modelName}), Arrays.asList(new String[]{"flux"}));
		} catch (Exception e) {
			throw new CobraMatlabFormulationException(e);
		}
	}

	@Override
	public SteadyStateSimulationResult parseMatlabOutput() {
		SteadyStateSimulationResult result = new SteadyStateSimulationResult(model, "", null);
		try{
			double[] primal = getConverter().getVariableDoubleList("flux");
			
			String[] rxns = getConverter().getVariableStringList(modelName+".rxns");
			
			FluxValueMap fluxValues = new FluxValueMap();
			for (int i = 0; i < rxns.length; i++)
				fluxValues.put(rxns[i], primal[i]);

			result.setMethod("COBRA GeometricFBA");
			result.setFluxValues(fluxValues);
			
			result.setOFString(obj_coef.entrySet().iterator().next().getKey());
			result.setOFvalue(fluxValues.getValue(obj_coef.entrySet().iterator().next().getKey()));
		}catch(Exception e){
			throw new CobraMatlabFormulationException(e);
		}
		return result;
	}

	@Override
	public void prepareMatlabEnvironment() {		
		try {
			obj_coef = ManagerExceptionUtils.testCast(properties, Map.class, SimulationProperties.OBJECTIVE_FUNCTION, false);
		} catch (Exception e) {
			e.printStackTrace();
			obj_coef = new HashMap<String, Double>();
			obj_coef.put(model.getBiomassFlux(), 1.0);
		}
		
		if(obj_coef.size() == 1 && !obj_coef.containsKey(model.getBiomassFlux()))
			getConverter().runFunction("changeObjective", Arrays.asList(new String[]{modelName, "'"+obj_coef.entrySet().iterator().next().getKey()+"'"}), 
					Arrays.asList(new String[]{modelName}));
	}


	@Override
	protected Map<String, Object> createConverterParameteres() {
		return null;
	}

	@Override
	public void clearAllProperties() {
		// TODO Auto-generated method stub
		
	}
}
