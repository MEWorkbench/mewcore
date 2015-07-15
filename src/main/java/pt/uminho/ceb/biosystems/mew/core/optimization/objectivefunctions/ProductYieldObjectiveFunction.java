package pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions;

import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.utils.Debugger;

public class ProductYieldObjectiveFunction implements IObjectiveFunction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -718681127792059768L;
	protected double minUptakeFlux;
	protected double minBiomassFlux;
	protected String biomassId;
	protected String productId;
	protected String substrateId;
		
	public ProductYieldObjectiveFunction(
			String biomassId,
			String productId,
			String substrateId,
			Double minUptakeFlux,
			Double minBiomassFlux ) {
		this.minUptakeFlux = minUptakeFlux;
		this.minBiomassFlux = minBiomassFlux;
		this.biomassId = biomassId;
		this.productId = productId;
		this.substrateId = substrateId;
	}

	@Override
	public double getWorstFitness() {
		return 0;
	}

	@Override
	public ObjectiveFunctionType getType() {
		return ObjectiveFunctionType.PYIELD;
	}

	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {

		FluxValueMap fvm = simResult.getFluxValues();
		double biomassFlux = fvm.get(biomassId);
		double productFlux = fvm.get(productId);
		double substrateFlux = Math.abs(fvm.get(substrateId));
		
		double fitness;
		if(biomassFlux < minBiomassFlux)
			fitness = getWorstFitness();
		else if(substrateFlux < minUptakeFlux)
			fitness = getWorstFitness();
		else
			fitness = productFlux/substrateFlux;
		
		Debugger.debug("B:" + biomassFlux + "\tP:" + productFlux + "\tS:" + substrateFlux + "\tYIELD:" + fitness);
		return fitness;
	}

	@Override
	public boolean isMaximization() {
		return true;
	}

	@Override
	public double getUnnormalizedFitness(double fit) {
		return fit;
	}

	@Override
	public String getShortString() {
		return "PYIELD~target=" + productId +
				";minUptake="+minUptakeFlux + 
				";minBiomass="+minBiomassFlux;
	}

	public String getDesiredId() {
		return productId;
	}
	
	public String getSubstrateId() {
		return substrateId;
	}

	@Override
	public String getLatexString() {
		return "$"+getLatexFormula()+"$";
	}
	
	@Override
	public String getLatexFormula() {
		return "PYIELD = \\frac{\\text{"+productId+"}}{\\text{"+substrateId+"}}";
	}

	@Override
	public String getBuilderString() {
		return getType() + "("+biomassId+","+productId+","
				+substrateId+","+minUptakeFlux+","+minBiomassFlux+")"; 
	}

	
}
