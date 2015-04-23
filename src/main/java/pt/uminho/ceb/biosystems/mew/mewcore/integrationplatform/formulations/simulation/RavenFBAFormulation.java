package pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.formulations.simulation;

import java.util.Arrays;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.connection.converter.ConnectionFormulation;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.connection.matlab.MatlabConnection;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.ManagerExceptionUtils;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;

public class RavenFBAFormulation extends ConnectionFormulation{

	public RavenFBAFormulation(ISteadyStateModel model) throws MatlabConnectionException, MatlabInvocationException {
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
		
		
//			double d;
//		
//			d = ManagerExceptionUtils.testCast(properties, Double.class, "geometric teta", false);
//		
//			getConverter().sendDouble("teta", d);
		
		
			
			//getConverter().sendModel(getModel(), modelName);
			
			getConverter().runFunction("solveLP", Arrays.asList(new String[]{modelName}), Arrays.asList(new String[]{"ravenFBASolution"}));
			
//			if(isMaximized)
//				getConverter().runFunction("optimizeCbModel", Arrays.asList(new String[]{modelName}), Arrays.asList(new String[]{"FBAsolution"}));
//			else
//				getConverter().runFunction("optimizeCbModel", Arrays.asList(new String[]{modelName, "'min'"}) , Arrays.asList(new String[]{"FBAsolution"}));
				//getConverter().runCommand("FBAsolution = optimizeCbModel("+getModifiedModelId()+", 'min', teta);");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public SteadyStateSimulationResult parseMatlabOutput() {
		SteadyStateSimulationResult result = new SteadyStateSimulationResult(model, "", null);
		try{
			 
			//double[] reducedCosts = getConverter().getVariableDoubleList("FBAsolution.w");
			//double[] shadowPrices = getConverter().getVariableDoubleList("FBAsolution.y");
			double[] primal = getConverter().getVariableDoubleList("ravenFBASolution.x");
			
			String[] rxns = getConverter().getVariableStringList(modelName+".rxns");
			String[] mets = getConverter().getVariableStringList(modelName+".mets");
			
			FluxValueMap fluxValues = new FluxValueMap();
			if(primal.length > 0)
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
			result.setOFvalue(-1*((double[]) getConverter().getVariableDoubleList("ravenFBASolution.f"))[0]);
			//////////////////////////////////////////////////////////////////////////////////////////
			
			result.setSolverOutput(getConverter().getVariableString("ravenFBASolution.msg"));
			result.setFluxValues(fluxValues);
			
		}catch(Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public void prepareMatlabEnvironment() {
		// TODO Auto-generated method stub
//		try {
//			getConverter().sendModel(getModel(), modelName);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
	}


	@Override
	protected Map<String, Object> createConverterParameteres() {
		// TODO Auto-generated method stub
		return null;
	}

}
