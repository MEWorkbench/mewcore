package pt.uminho.ceb.biosystems.mew.core.integrationplatform.formulations.simulation;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Map;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.converter.ConnectionFormulation;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.matlab.MatlabConnection;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;

public class TigerFBAFormulation extends ConnectionFormulation {
	
	public TigerFBAFormulation(ISteadyStateModel model) throws MatlabConnectionException, MatlabInvocationException {
		super(model, new MatlabConnection());
		// TODO Auto-generated constructor stub
		modelName = model.getId();
	}
	
	@Override
	protected void initPropsKeys() {
		super.initPropsKeys();
		mandatoryProps.add(SimulationProperties.IS_MAXIMIZATION);
		possibleProperties.add(SimulationProperties.OBJECTIVE_FUNCTION);
	}
	
//		

//
//	@Override
//	public Set<String> getMandatoryProperties() {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
	String modelName;// = /*getModel().getId()+"wt";*/"modelWT";
	
	@Override
	public void executeSimulationCommand() {
		Boolean isMaximized;
		try {
			isMaximized = ManagerExceptionUtils.testCast(properties, Boolean.class, SimulationProperties.IS_MAXIMIZATION, false);
			
			//tigerFBASolution = fba(tigerModel);
			getConverter().runFunction("fba", Arrays.asList(new String[] { "tigerModel" }), Arrays.asList(new String[] { "tigerFBASolution" }));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public SteadyStateSimulationResult parseMatlabOutput() {
		SteadyStateSimulationResult result = new SteadyStateSimulationResult(model, "", null);
		try {
		
//			double[] reducedCosts = getConverter().getVariableDoubleList("FBAsolution.w");
//			double[] shadowPrices = getConverter().getVariableDoubleList("FBAsolution.y");
			double[] primal = getConverter().getVariableDoubleList("tigerFBASolution.x(1:length(tigerModel.rxns))");
			
			String[] rxns = getConverter().getVariableStringList(modelName + ".rxns");
			String[] mets = getConverter().getVariableStringList(modelName + ".mets");
			
			FluxValueMap fluxValues = new FluxValueMap();
			if (primal.length > 0)
				for (int i = 0; i < rxns.length; i++)
					fluxValues.put(rxns[i], primal[i]);
					
//			MapStringNum compRxnRedCostValues = new MapStringNum();
//			if(reducedCosts.length > 0)
//				for (int i = 0; i < rxns.length; i++)
//					compRxnRedCostValues.put(rxns[i], reducedCosts[i]);			
//			result.addComplementaryInfoMetabolites("ReducedCosts", compRxnRedCostValues);
//			
//			MapStringNum compMetShadPricesValues = new MapStringNum();
//			if(shadowPrices.length > 0)
//				for (int i = 0; i < mets.length; i++)
//					compMetShadPricesValues.put(mets[i], shadowPrices[i]);			
//			result.addComplementaryInfoReactions("ShadowPrices", compMetShadPricesValues);

//			getConverter().runCommand("objectiveAbbr = checkObjective("+modelName+");");
//			getConverter().runFunction("checkObjective", Arrays.asList(new String[]{modelName}), Arrays.asList(new String[]{"objectiveAbbr"}));
//			
//			String oFString = getConverter().getVariableString("objectiveAbbr");
//			result.setOFString(possibleProperties..get);
			
			/////////////////////////////////////////////////////////////////////////////////////////
			result.setOFvalue(((double[]) getConverter().getVariableDoubleList("tigerFBASolution.val"))[0]);
			//////////////////////////////////////////////////////////////////////////////////////////
			
			//result.setSolverOutput(getConverter().getVariableString("FBAsolution.solver"));
			result.setFluxValues(fluxValues);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public void prepareMatlabEnvironment() {
		
		//start_tiger('glpk')
		getConverter().runFunction("start_tiger",
				Arrays.asList(new String[] { "'glpk'" }),
				Arrays.asList(new String[] {}));
				
		//tigerModel = cobra_to_tiger(cobraModel);
		getConverter().runFunction("cobra_to_tiger",
				Arrays.asList(new String[] { modelName }),
				Arrays.asList(new String[] { "tigerModel" }));
				
	}
	
	@Override
	protected Map<String, Object> createConverterParameteres() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void clearAllProperties() {
	}
	
}
