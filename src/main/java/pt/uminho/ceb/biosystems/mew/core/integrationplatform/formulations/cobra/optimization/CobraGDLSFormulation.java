package pt.uminho.ceb.biosystems.mew.core.integrationplatform.formulations.cobra.optimization;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.components.OptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.converter.ConnectionFormulation;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.matlab.MatlabConnection;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.exceptions.CobraMatlabFormulationException;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;

public class CobraGDLSFormulation extends ConnectionFormulation{

	String modelNameFromOptFlux;
	
	
	public CobraGDLSFormulation(ISteadyStateModel model) throws MatlabConnectionException, MatlabInvocationException {
		super(model, new MatlabConnection());
		initFormulation(model);
	}
	
	private void initFormulation(ISteadyStateModel model) {
		modelNameFromOptFlux = model.getId();
		if(modelNameFromOptFlux == null || modelNameFromOptFlux.equals(""))
			modelNameFromOptFlux = "model";
		if(modelNameFromOptFlux.startsWith("."))
			modelNameFromOptFlux = "DOT" + modelNameFromOptFlux;
	}

	@Override
	protected Map<String, Object> createConverterParameteres() {
		
		Map<String, Object> prop = new HashMap<String, Object>();
		prop.put(SimulationProperties.ENVIRONMENTAL_CONDITIONS, properties.get(SimulationProperties.ENVIRONMENTAL_CONDITIONS));
		
		prop.put(OptimizationProperties.SELECTED_RXNS, properties.get(OptimizationProperties.SELECTED_RXNS));
		prop.put(OptimizationProperties.PRODUCT_FLUX, properties.get(OptimizationProperties.PRODUCT_FLUX));
		prop.put(OptimizationProperties.MAX_KOS, properties.get(OptimizationProperties.MAX_KOS));
		prop.put(OptimizationProperties.MIN_GROWTH, properties.get(OptimizationProperties.MIN_GROWTH));
		prop.put(OptimizationProperties.TIME_LIMIT, properties.get(OptimizationProperties.TIME_LIMIT));
		prop.put(OptimizationProperties.NEIGHBORHOOD_SIZE, properties.get(OptimizationProperties.NEIGHBORHOOD_SIZE));
		prop.put(OptimizationProperties.NUM_SEARCH_PATHS, properties.get(OptimizationProperties.NUM_SEARCH_PATHS));
		prop.put(OptimizationProperties.ITERATION_LIMIT, properties.get(OptimizationProperties.ITERATION_LIMIT));
		
		return prop;
	}

	@Override
	protected void initPropsKeys() {
		super.initPropsKeys();
		possibleProperties.add(SimulationProperties.ENVIRONMENTAL_CONDITIONS);
	}
	
	@Override
	public void prepareMatlabEnvironment() {		
		try {
			
			int neighborhoodSize = 1;
			if(properties.containsKey(OptimizationProperties.NEIGHBORHOOD_SIZE))
				neighborhoodSize = (int) properties.get(OptimizationProperties.NEIGHBORHOOD_SIZE);
			converter.sendInteger("neighborhoodSize", neighborhoodSize);
			
			
			int numSearchPaths = 1;
			if(properties.containsKey(OptimizationProperties.NUM_SEARCH_PATHS))
				numSearchPaths = (int) properties.get(OptimizationProperties.NUM_SEARCH_PATHS);
			converter.sendInteger("numSearchPaths", numSearchPaths);
			
			
			int maxKOs = 3; 
			if(properties.containsKey(OptimizationProperties.MAX_KOS))
				maxKOs = (int) properties.get(OptimizationProperties.MAX_KOS);
			converter.sendInteger("maxKO", maxKOs);

			
			Set<String> selectedRxns = new TreeSet<>();
			if(properties.containsKey(OptimizationProperties.SELECTED_RXNS)){
				selectedRxns = (Set<String>)properties.get(OptimizationProperties.SELECTED_RXNS);
				converter.sendStringList("selectedRxns", selectedRxns.toArray(new String[selectedRxns.size()]));
				converter.runCommand("selectedRxns = selectedRxns.';");
			}
			
			
			int timeLimit = 3600;
			if(properties.containsKey(OptimizationProperties.TIME_LIMIT))
				timeLimit = (int) properties.get(OptimizationProperties.TIME_LIMIT);
			converter.sendInteger("timeLimit", timeLimit);

			
			double minGrowth = 0.005;
			if(properties.containsKey(OptimizationProperties.MIN_GROWTH))
				minGrowth = (double)properties.get(OptimizationProperties.MIN_GROWTH);
			converter.sendDouble("minGrowth", minGrowth);
			
			
			String targetRxn = model.getBiomassFlux();
			if(properties.containsKey(OptimizationProperties.PRODUCT_FLUX))
				targetRxn = (String) properties.get(OptimizationProperties.PRODUCT_FLUX);
			converter.sendString("targetRxn", targetRxn);
			

		} catch (Exception e) {
			throw new CobraMatlabFormulationException(e, "Problem in prepareMatlabEnvironment of CobraGDLSFormulation");
		}
	}
	
	@Override
	public void executeSimulationCommand()
	{
		
		try {
			getConverter().runFunction("GDLS", //"listOfSelectedRxns"
					Arrays.asList(new String[]{modelNameFromOptFlux, "targetRxn", "'selectedRxns'", "selectedRxns", "'timeLimit'", "timeLimit", "'nbhdsz'", "neighborhoodSize", "'M'", "numSearchPaths", "'minGrowth'", "minGrowth", "'maxKO'", "maxKO"}), 
					Arrays.asList(new String[]{"gdlsSolution", "bilevelMILPProblem", "gdlsSolutionStructs"}));
		
		
//		getConverter().runFunction("GDLS", //"listOfSelectedRxns"
//				Arrays.asList(new String[]{modelNameFromOptFlux, "targetRxn", 
//						"'nbhdsz'", "neighborhoodSize", "'M'", "numSearchPaths", "'maxKO'", "maxKO",
//						"'selectedRxns'", "selectedRxns", "'timeLimit'", "timeLimit", "'minGrowth'", "minGrowth"}), 
//				Arrays.asList(new String[]{"gdlsSolution", "bilevelMILPProblem", "gdlsSolutionStructs"}));
		} catch (Exception e) {
			throw new CobraMatlabFormulationException(e, "Problem in executeSimulationCommand of CobraGDLSFormulation");
		}
	}
	
	@Override
	public SteadyStateSimulationResult parseMatlabOutput()
	{
		SteadyStateSimulationResult result = new SteadyStateSimulationResult(model, "", null);
		
		try{
			
			String[] koRxnsList = getConverter().getVariableStringList("gdlsSolution"+".KOs");
			
			double biomassValue = getConverter().getVariableDoubleList("gdlsSolution"+".biomass")[0];
			
			double minProdValue = getConverter().getVariableDoubleList("gdlsSolution"+".minTargetProd")[0];
			double maxProdValue = getConverter().getVariableDoubleList("gdlsSolution"+".maxTargetProd")[0];
			
//			SimulationSteadyStateControlCenter fbaSimulation = new SimulationSteadyStateControlCenter(
//					(EnvironmentalConditions)properties.get(SimulationProperties.ENVIRONMENTAL_CONDITIONS), 
//					new GeneticConditions(new ReactionChangesList(Arrays.asList(koRxnsList))), 
//					model, 
//					SimulationProperties.FBA);
//			
//			fbaSimulation.setMaximization(true);
//			fbaSimulation.setFBAObjSingleFlux(converter.getVariableString("targetRxn"), 1.0);
//			fbaSimulation.setSolver(SolverType.CLP);
//			SteadyStateSimulationResult fbaResult = fbaSimulation.simulate();
//			
//			System.out.println(fbaResult.getGeneticConditions().getReactionList().size());
			
			getConverter().runCommand(modelNameFromOptFlux+"_FBA = " +modelNameFromOptFlux+";");
			for (int i = 0; i < koRxnsList.length; i++) {
				getConverter().runFunction("changeRxnBounds", 
						Arrays.asList(new String[]{modelNameFromOptFlux+"_FBA", "'"+koRxnsList[i]+"'", "0", "'b'"}), 
						Arrays.asList(new String[]{modelNameFromOptFlux+"_FBA"}));
			}
			
			getConverter().runFunction("optimizeCbModel", 
					Arrays.asList(new String[]{modelNameFromOptFlux+"_FBA"}), 
					Arrays.asList(new String[]{"cobraFBAsolution"}));
			
			
			result = getResultFromFBA();
			
			result.setOFvalue(maxProdValue);
			
			result.setEnvironmentalConditions((EnvironmentalConditions)properties.get(SimulationProperties.ENVIRONMENTAL_CONDITIONS));
			result.setGeneticConditions(new GeneticConditions(new ReactionChangesList(Arrays.asList(koRxnsList))));
			
			//result.setMethod("Cobra GDLS");
//			
			
			return result;
		
		} catch(Exception e) {
			throw new CobraMatlabFormulationException(e, "Problem in parseMatlabOutput of CobraGDLSFormulation");
		}
	}
	
	private SteadyStateSimulationResult getResultFromFBA(){
		SteadyStateSimulationResult result = new SteadyStateSimulationResult(model, "", null);
		try{
			double[] reducedCosts = getConverter().getVariableDoubleList("cobraFBAsolution.w");
			double[] shadowPrices = getConverter().getVariableDoubleList("cobraFBAsolution.y");
			double[] primal = getConverter().getVariableDoubleList("cobraFBAsolution.x");
			
			String[] rxns = getConverter().getVariableStringList(modelNameFromOptFlux+"_FBA"+".rxns");
			String[] mets = getConverter().getVariableStringList(modelNameFromOptFlux+"_FBA"+".mets");
			
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
			result.setOFString("Max: " +getConverter().getVariableString("targetRxn"));
			result.setFluxValues(fluxValues);
			result.setMethod("Cobra FBA");
			//result.setOFString("Max: R_BiomassEcoli");
			
		}catch(Exception e){
			throw new CobraMatlabFormulationException(e, "Problem in getResultFromFBA of CobraGDLSFormulation");
		}
		
		return result;
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
