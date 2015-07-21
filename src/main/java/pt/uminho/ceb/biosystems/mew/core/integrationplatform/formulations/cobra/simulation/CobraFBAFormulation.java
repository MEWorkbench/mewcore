package pt.uminho.ceb.biosystems.mew.core.integrationplatform.formulations.cobra.simulation;

import java.beans.PropertyChangeListener;
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
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;

public class CobraFBAFormulation extends ConnectionFormulation{
	
	Boolean isMaximized = true;
	Map<String, Double> obj_coef = null;
	String modelName;

	public CobraFBAFormulation(ISteadyStateModel model) throws MatlabConnectionException, MatlabInvocationException {
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
			if(isMaximized)
				getConverter().runFunction("optimizeCbModel", Arrays.asList(new String[]{modelName}), Arrays.asList(new String[]{"cobraFBAsolution"}));
			else
				getConverter().runFunction("optimizeCbModel", Arrays.asList(new String[]{modelName, "'min'"}) , Arrays.asList(new String[]{"cobraFBAsolution"}));
		} catch (Exception e) {
			throw new CobraMatlabFormulationException(e);
		}
	}

	@Override
	public SteadyStateSimulationResult parseMatlabOutput() {
		SteadyStateSimulationResult result = new SteadyStateSimulationResult(model, "", null);
		try{
			 
			double[] reducedCosts = getConverter().getVariableDoubleList("cobraFBAsolution.w");
			double[] shadowPrices = getConverter().getVariableDoubleList("cobraFBAsolution.y");
			double[] primal = getConverter().getVariableDoubleList("cobraFBAsolution.x");
			
			String[] rxns = getConverter().getVariableStringList(modelName+".rxns");
			String[] mets = getConverter().getVariableStringList(modelName+".mets");
			
			FluxValueMap fluxValues = new FluxValueMap();
			if(primal.length > 0)
				for (int i = 0; i < rxns.length; i++)
					fluxValues.put(rxns[i], primal[i]);
			
			MapStringNum compRxnRedCostValues = new MapStringNum();
			if(reducedCosts.length > 0)
				for (int i = 0; i < rxns.length; i++)
					compRxnRedCostValues.put(rxns[i], reducedCosts[i]);			
			result.addComplementaryInfoMetabolites("ReducedCosts", compRxnRedCostValues);
			
			MapStringNum compMetShadPricesValues = new MapStringNum();
			if(shadowPrices.length > 0)
				for (int i = 0; i < mets.length; i++)
					compMetShadPricesValues.put(mets[i], shadowPrices[i]);			
			result.addComplementaryInfoReactions("ShadowPrices", compMetShadPricesValues);
			
			
			/////////////////////////////////////////////////////////////////////////////////////////
			result.setOFvalue(((double[]) getConverter().getVariableDoubleList("cobraFBAsolution.f"))[0]);
			//////////////////////////////////////////////////////////////////////////////////////////
			
			result.setSolverOutput(getConverter().getVariableString("cobraFBAsolution.solver"));
			result.setFluxValues(fluxValues);
			result.setMethod("Cobra FBA");
			
			String ofString = "Max: ";
			if(!isMaximized)
				ofString = "Min: ";
			
//			for (String objective : obj_coef.keySet()) {
//				ofString += objective;
//			}
				
			result.setOFString(ofString + obj_coef.entrySet().iterator().next().getKey());
			
		}catch(Exception e){
			throw new CobraMatlabFormulationException(e);
		}
		return result;
	}

	@Override
	public void prepareMatlabEnvironment() {
		try{
			isMaximized = ManagerExceptionUtils.testCast(properties, Boolean.class, SimulationProperties.IS_MAXIMIZATION, false);
		} catch(Exception e){
			e.printStackTrace();
			isMaximized = true;
		}
		
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
	public void preSimulateActions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postSimulateActions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearAllProperties() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRecreateOF(boolean recreateOF) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRecreateOF() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveModelToMPS(String file, boolean includeTime) {
		// TODO Auto-generated method stub
		
	}	

}
