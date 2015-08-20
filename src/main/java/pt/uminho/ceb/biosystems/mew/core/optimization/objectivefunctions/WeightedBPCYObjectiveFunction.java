package pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions;

import java.io.Serializable;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.utils.Debugger;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class WeightedBPCYObjectiveFunction implements IObjectiveFunction, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected final double worstFitness = 0.0;
	
	protected String biomassId; 
	protected String desiredFluxId;
	protected double alpha;
	protected SolverType lpSolver = null;
	protected SimulationSteadyStateControlCenter center = null;
	
	
	public WeightedBPCYObjectiveFunction(String biomassId, String desiredFluxId, Double alpha, SolverType lpSolver) {
		
		this.biomassId = biomassId;
		this.desiredFluxId = desiredFluxId;
		this.alpha = alpha;
		this.lpSolver = lpSolver;
	}

	@Override
	public double evaluate(SteadyStateSimulationResult simResult){
		// Formula:  alpha * FVA_Prod_Max + (1-alpha) * FVA_Prod_Min
		
		double fvaMaxProd = 0;
		double fvaMinProd = 0;
		double biomassFluxValue = simResult.getFluxValues().getValue(biomassId) * 0.99999;
		ISteadyStateModel model = simResult.getModel();
		
		if(biomassFluxValue > 0){
			
			GeneticConditions gc = simResult.getGeneticConditions();

			
			EnvironmentalConditions ec = new EnvironmentalConditions();
			if(simResult.getEnvironmentalConditions() != null)
				ec.putAll(simResult.getEnvironmentalConditions());
			ec.addReactionConstraint(biomassId, new ReactionConstraint(biomassFluxValue,100000.0));
			
			if(center==null){
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
			
			if(fvaMaxResult!=null && fvaMaxResult.getFluxValues()!=null)
				fvaMaxProd = fvaMaxResult.getFluxValues().getValue(desiredFluxId);
			
			if(fvaMaxProd > 0.00000001){
				center.setMaximization(false);
				SteadyStateSimulationResult fvaMinResult = null;
				
				try {
					fvaMinResult = (SteadyStateSimulationResult) center.simulate();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if(fvaMinResult!=null && fvaMinResult.getFluxValues()!=null)
					fvaMinProd = fvaMinResult.getFluxValues().getValue(desiredFluxId);
			}
			
			if(Double.isNaN(fvaMinProd))
				fvaMinProd = 0;

			if(Double.isNaN(fvaMaxProd))
				fvaMaxProd = 0;
		}
		
		
		double ret = biomassFluxValue * (alpha * fvaMaxProd + (1-alpha) * fvaMinProd);
		Debugger.debug("bx = " + biomassFluxValue + "\t fvaMax = " + fvaMaxProd + "\t fvaMin = " + fvaMinProd + "\t of = " + ret + "\talpha = "+alpha);
		return ret;
	}

	@Override
	public ObjectiveFunctionType getType() {
		return ObjectiveFunctionType.WEIGHTED_BPCY;
	}

	@Override
	public double getWorstFitness() {
		return worstFitness;
	}

	@Override
	public boolean isMaximization() {
		return true;
	}

	/* (non-Javadoc)
	 * @see metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction#getUnnormalizedFitness(double)
	 */
	@Override
	public double getUnnormalizedFitness(double fit) {
		return fit;
	}

	/* (non-Javadoc)
	 * @see metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction#getShortString()
	 */
	@Override
	public String getShortString() {
		return "WBPCY~"+desiredFluxId+"~"+alpha;
	}

	public String getDesiredId() {
		return desiredFluxId;
	}

	@Override
	public String getLatexString() {
		return "$"+getLatexFormula()+"$";
	}
	
	@Override
	public String getLatexFormula() {
		return "WeigthedBPCY = \\text{"+biomassId+"} \\times"
				+ "(\\alpha \\times FVA_{max}\\;\\text{"+desiredFluxId+"} + "
				+ "(1-\\alpha) \\times FVA_{min}\\;\\text{"+desiredFluxId+"})";
	}

	@Override
	public String getBuilderString() {
		return getType() + "("+biomassId+","+desiredFluxId+","+alpha+","+lpSolver+")";
	}

}
