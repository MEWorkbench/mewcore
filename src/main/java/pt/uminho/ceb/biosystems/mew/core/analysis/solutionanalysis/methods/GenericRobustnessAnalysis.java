package pt.uminho.ceb.biosystems.mew.core.analysis.solutionanalysis.methods;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.analysis.solutionanalysis.ISolutionAnalysis;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.LMOMA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.LPVariabilityProblem;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;

public class GenericRobustnessAnalysis extends AbstractSolutionAnalysisMethod implements ISolutionAnalysis {
	
	public static int		NUM_OUTPUTS	= 3;
	private String			_targetID;
	protected static DecimalFormat	df			= new DecimalFormat("#.00000");
										
	public GenericRobustnessAnalysis(String targetID) {
		super(NUM_OUTPUTS);
		_targetID = targetID;
	}
	
	@Override
	public void initDescriptions() {
		descriptions[0] = "MAX OF T100%";
		descriptions[1] = "MIN OF T0%";
		descriptions[2] = "MAX ALLOWED RELAX %";
	}
	
	@Override
	public double[] analyse(SteadyStateSimulationResult result) {
		double[] toret = new double[3];
		
		ISteadyStateModel model = result.getModel();
		EnvironmentalConditions envConditions = result.getEnvironmentalConditions();
		GeneticConditions genConditions = result.getGeneticConditions();
//		String biomassID = model.getBiomassFlux();
		
		LMOMA lmoma = new LMOMA(model);
		lmoma.setEnvironmentalConditions(envConditions);
		lmoma.setGeneticConditions(genConditions);
		lmoma.setSolverType(SolverType.CPLEX3);
		
		SteadyStateSimulationResult res = null;
		try {
			res = lmoma.simulate();
		} catch (PropertyCastException | MandatoryPropertyException | WrongFormulationException | SolverException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		double objectiveLMOMA = res.getOFvalue();
//		System.out.println("biomass = " + res.getFluxValues().get(biomassID));
//		System.out.println("target= " + res.getFluxValues().get(_targetID));
//		System.out.println(res.getOFString() + "=" + res.getOFvalue());
		
		int numSteps = 20;
		int currStep = 0;
		
		double rangeMax = 1.0;
		double rangeMin = 0.0;
		double minPercent = 0.00001;
		boolean isMaximization = false;
		
		LPVariabilityProblem<LMOMA> lpVariabilityOriginalMin = new LPVariabilityProblem<>(model);
		lpVariabilityOriginalMin.setInitProblem(lmoma);
		Map<String, Double> objFunction = new HashMap<String, Double>();
		objFunction.put(_targetID, 1.0);
		lpVariabilityOriginalMin.setObjectiveFunction(objFunction);
		lpVariabilityOriginalMin.setIsMaximization(false);
		lpVariabilityOriginalMin.setObjectiveValue(objectiveLMOMA);
		lpVariabilityOriginalMin.setEnvironmentalConditions(envConditions);
		lpVariabilityOriginalMin.setGeneticConditions(genConditions);
		lpVariabilityOriginalMin.setSolverType(SolverType.CPLEX3);
		lpVariabilityOriginalMin.setProperty(SimulationProperties.RELAX_COEF, 0.99999999);
		
		SteadyStateSimulationResult resOriginalMin = null;
		try {
			resOriginalMin = lpVariabilityOriginalMin.simulate();
		} catch (PropertyCastException | MandatoryPropertyException | WrongFormulationException | SolverException e) {
			e.printStackTrace();
		}
		
		Double originalFVAMin = null;
		
		if (resOriginalMin != null) {
			originalFVAMin = resOriginalMin.getFluxValues().get(_targetID);
		}
		
		Double current = null;
		
		toret[0] = objectiveLMOMA;
		while (currStep < numSteps) {
			
			double nextTestRelax = ((rangeMax - rangeMin) / 2) + rangeMin;
			
			LPVariabilityProblem<LMOMA> lpVariabilityCurrent = new LPVariabilityProblem<>(model);
			lpVariabilityCurrent.setInitProblem(lmoma);
			lpVariabilityCurrent.setObjectiveFunction(objFunction);
			lpVariabilityCurrent.setIsMaximization(false);
			lpVariabilityCurrent.setObjectiveValue(objectiveLMOMA);
			lpVariabilityCurrent.setEnvironmentalConditions(envConditions);
			lpVariabilityCurrent.setGeneticConditions(genConditions);
			lpVariabilityCurrent.setSolverType(SolverType.CPLEX3);
			lpVariabilityCurrent.setProperty(SimulationProperties.RELAX_COEF, nextTestRelax);
			SteadyStateSimulationResult resCurrent = null;
			try {
				resCurrent = lpVariabilityCurrent.simulate();
			} catch (PropertyCastException | MandatoryPropertyException | WrongFormulationException | SolverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			current = (resCurrent != null) ? resCurrent.getOFvalue() : null;
			
			boolean robust = (current != null) ? isRobust(originalFVAMin, current, minPercent, isMaximization) : false;
//			System.out.println("Step [" + currStep + "]: relax =" + nextTestRelax + " / target = " + current + " / robust = " + robust + " / range = [" + rangeMin + ", " + rangeMax + "]");
			
			toret[1] = objectiveLMOMA * nextTestRelax;
			
			if (robust) {
				rangeMax = nextTestRelax;
			} else {
				rangeMin = nextTestRelax;
			}
			
			currStep++;
		}
		
//		double pivot = Math.abs(toret[1]) > 1e-5 ? Math.abs(toret[1]) : 0;
//		toret[2] = toret[0] - pivot;
		if (toret[0] > 0) {
			toret[2] = Double.parseDouble(df.format((toret[1] * 100) / toret[0]));
		} else {
			toret[2] = 0.0;
		}
		
		return toret;
	}
	
	public static boolean isRobust(double original, double current, double minPercent, boolean isMaximization) {
		
		return isMaximization ? (current <= original * minPercent) : (current >= original * minPercent);
	}
	
}
