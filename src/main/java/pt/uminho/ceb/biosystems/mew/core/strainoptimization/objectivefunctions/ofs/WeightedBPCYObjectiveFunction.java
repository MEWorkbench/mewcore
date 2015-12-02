package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.AbstractObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ObjectiveFunctionParameterType;
import pt.uminho.ceb.biosystems.mew.core.utils.Debugger;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;

public class WeightedBPCYObjectiveFunction extends AbstractObjectiveFunction {
	
	private static final long serialVersionUID = 1L;
	
	public static final String	ID					= "WBPCY";
	public static final String	WBPCY_PARAM_BIOMASS	= "Biomass";
	public static final String	WBPCY_PARAM_PRODUCT	= "Product";
	public static final String	WBPCY_PARAM_ALPHA	= "Alpha";
	public static final String	WBPCY_PARAM_SOLVER	= "Solver";
	
	protected final double		worstFitness		= 0.0;
	
	protected transient SimulationSteadyStateControlCenter center = null;
	
	public Map<String, ObjectiveFunctionParameterType> loadParameters(){
		HashMap<String, ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(WBPCY_PARAM_BIOMASS, ObjectiveFunctionParameterType.REACTION_BIOMASS);
		myparams.put(WBPCY_PARAM_PRODUCT, ObjectiveFunctionParameterType.REACTION_PRODUCT);
		myparams.put(WBPCY_PARAM_ALPHA, ObjectiveFunctionParameterType.DOUBLE);
		myparams.put(WBPCY_PARAM_SOLVER, ObjectiveFunctionParameterType.SOLVER);
		return Collections.unmodifiableMap(myparams);
	}
		
	public WeightedBPCYObjectiveFunction() {
		super();
	}
	
	public WeightedBPCYObjectiveFunction(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration {
		super(configuration);
	}
	
	public WeightedBPCYObjectiveFunction(String biomassId, String desiredFluxId, Double alpha, SolverType lpSolver) {
		super(biomassId, desiredFluxId, alpha, lpSolver);
	}
	
	@Override
	protected void processParams(Object... params) {
		setParameterValue(WBPCY_PARAM_BIOMASS, params[0]);
		setParameterValue(WBPCY_PARAM_PRODUCT, params[1]);
		setParameterValue(WBPCY_PARAM_ALPHA, params[2]);
		setParameterValue(WBPCY_PARAM_SOLVER, params[3]);
	}
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		// Formula:  alpha * FVA_Prod_Max + (1-alpha) * FVA_Prod_Min
		
		String biomassId = (String) getParameterValue(WBPCY_PARAM_BIOMASS);
		String desiredFluxId = (String) getParameterValue(WBPCY_PARAM_PRODUCT);
		double alpha = (double) getParameterValue(WBPCY_PARAM_ALPHA);
		SolverType lpSolver = (SolverType) getParameterValue(WBPCY_PARAM_SOLVER);
		
		double fvaMaxProd = 0;
		double fvaMinProd = 0;
		double biomassFluxValue = simResult.getFluxValues().getValue(biomassId) * 0.99999;
		ISteadyStateModel model = simResult.getModel();
		
		if (!Double.isNaN(biomassFluxValue) && biomassFluxValue > 0) {
			
			GeneticConditions gc = simResult.getGeneticConditions();
			
			EnvironmentalConditions ec = new EnvironmentalConditions();
			if (simResult.getEnvironmentalConditions() != null) ec.putAll(simResult.getEnvironmentalConditions());
			ec.addReactionConstraint(biomassId, new ReactionConstraint(biomassFluxValue, 100000.0));
			
			if (center == null) {
				center = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
				center.setSolver(lpSolver);
				center.setFBAObjSingleFlux(desiredFluxId, 1.0);
			}
			center.setGeneticConditions(gc);
			center.setEnvironmentalConditions(ec);
			center.setMaximization(true);
			SteadyStateSimulationResult fvaMaxResult = null;
			
			try {
				fvaMaxResult = (SteadyStateSimulationResult) center.simulate();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (fvaMaxResult != null && fvaMaxResult.getFluxValues() != null) fvaMaxProd = fvaMaxResult.getFluxValues().getValue(desiredFluxId);
			
			if (fvaMaxProd > 0.00000001) {
				center.setMaximization(false);
				SteadyStateSimulationResult fvaMinResult = null;
				
				try {
					fvaMinResult = (SteadyStateSimulationResult) center.simulate();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if (fvaMinResult != null && fvaMinResult.getFluxValues() != null) fvaMinProd = fvaMinResult.getFluxValues().getValue(desiredFluxId);
			}
			
			if (Double.isNaN(fvaMinProd)) fvaMinProd = 0;
			
			if (Double.isNaN(fvaMaxProd)) fvaMaxProd = 0;
			
			
			double ret = biomassFluxValue * (alpha * fvaMaxProd + (1 - alpha) * fvaMinProd);
			Debugger.debug("bx = " + biomassFluxValue + "\t fvaMax = " + fvaMaxProd + "\t fvaMin = " + fvaMinProd + "\t of = " + ret + "\talpha = " + alpha);
			return ret;
			
		}else{
			return getWorstFitness();
		}
	}
	
	@Override
	public double getWorstFitness() {
		return worstFitness;
	}
	
	@Override
	public boolean isMaximization() {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction#
	 * getUnnormalizedFitness(double)
	 */
	@Override
	public double getUnnormalizedFitness(double fit) {
		return fit;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction#
	 * getShortString()
	 */
	@Override
	public String getShortString() {
		return "WBPCY~" + getParameterValue(WBPCY_PARAM_PRODUCT) + "~" + getParameterValue(WBPCY_PARAM_ALPHA);
	}
	
	public String getDesiredId() {
		return (String) getParameterValue(WBPCY_PARAM_PRODUCT);
	}
	
	@Override
	public String getLatexString() {
		return "$" + getLatexFormula() + "$";
	}
	
	@Override
	public String getLatexFormula() {
		return "WeigthedBPCY = \\text{" + getParameterValue(WBPCY_PARAM_BIOMASS)+ "} \\times" + "(\\alpha \\times FVA_{max}\\;\\text{" + getParameterValue(WBPCY_PARAM_PRODUCT) + "} + " + "(1-\\alpha) \\times FVA_{min}\\;\\text{" + getParameterValue(WBPCY_PARAM_PRODUCT)+ "})";
	}
	
	@Override
	public String getBuilderString() {
		return "WBPCY (" + getParameterValue(WBPCY_PARAM_BIOMASS)+ "," + getParameterValue(WBPCY_PARAM_PRODUCT)+ "," +getParameterValue(WBPCY_PARAM_ALPHA)+ "," + getParameterValue(WBPCY_PARAM_SOLVER)+ ")";
	}
	
	@Override
	public String getID() {
		return ID;
	}
	
}
