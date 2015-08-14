package pt.uminho.ceb.biosystems.mew.core.simulation.formulations;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.IOverrideReactionBounds;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;

public class MinMaxRacio extends FBA{

	public MinMaxRacio(ISteadyStateModel model) {
		super(model);
		mandatoryProperties.add(SimulationProperties.MIN_MAX_RACIO_DIVISOR);
		mandatoryProperties.add(SimulationProperties.MIN_MAX_RACIO_DIVISOR_SENSE);
		mandatoryProperties.add(SimulationProperties.MIN_MAX_RACIO_DIVIDEND);
		mandatoryProperties.add(SimulationProperties.MIN_MAX_RACIO_DIVIDEND_SENSE);
		
		optionalProperties.add("DEFAULT_UB_LB");
	}

	@Override
	public Map<String, Double> getObjectiveFunction() {
		Map<String, Double> map = new HashMap<String, Double>();
		try {
			map.put(getDividendVariable(),(double) getDividendSense());
		} catch (Exception e) {
			// TODO: handle exception
		}
		return map ;
	}
	
	@Override
	protected void createVariables() throws PropertyCastException,
			MandatoryPropertyException, WrongFormulationException,
			SolverException {
		super.createVariables();
		
		int problemVar = model.getNumberOfReactions();
		
		for(int i =0; i < problemVar ; i++){
			problem.getVariable(i).setLowerBound(-getDefaultBounds());
			problem.getVariable(i).setUpperBound(getDefaultBounds());
		}
		
		
		int varcIdx = model.getReactionIndex(getDivisorVariable());
		problem.getVariable(varcIdx).setLowerBound(1*getDivisorSense());
		problem.getVariable(varcIdx).setUpperBound(1*getDivisorSense());
		
		int varDividendIdx = model.getReactionIndex(getDividendVariable());
		
		LPVariable v = problem.getVariable(varDividendIdx);
//		ReactionConstraint rc = model.getReactionConstraint(varDividendIdx); //was overrideRC
		IOverrideReactionBounds overrideRC = createModelOverride();
        ReactionConstraint rc = overrideRC.getReactionConstraint(varDividendIdx);
//        
//        System.out.println(getDividendVariable()+"\t"+ rc);
//         ReactionConstraint rc = model.getReactionConstraint(varDividendIdx); //was overrideRC
		if(getDividendSense()>0){
			
			double upperLimit = rc.getUpperLimit();
			if(upperLimit< 0 ) throw new WrongFormulationException("Problem in Dividend Sense!");
			
			problem.getVariable(varDividendIdx).setLowerBound(0.0);
			problem.getVariable(varDividendIdx).setUpperBound(upperLimit);
		}else{
			
			double loweLimit = rc.getLowerLimit();
			if(loweLimit > 0 ) throw new WrongFormulationException("Problem in Dividend Sense!");
			
			problem.getVariable(varDividendIdx).setLowerBound(loweLimit);
			problem.getVariable(varDividendIdx).setUpperBound(0.0);
		}
		
		putVarMappings("t", problemVar);
		problem.addVariable(new LPVariable("t", 0, getDefaultBounds()));
//		problem.addVariable(new LPVariable("t", 1,1));
	}
	
	private int getDivisorSense() throws PropertyCastException, MandatoryPropertyException {
		
		Number i = ManagerExceptionUtils.testCast(properties, Number.class, SimulationProperties.MIN_MAX_RACIO_DIVISOR_SENSE, false);
		double d = i.doubleValue();
		
		if( d <0)
			return  -1;
		else if (d > 0) return 1;
		
		throw new WrongFormulationException("");
		
	}
	
	private int getDividendSense() throws PropertyCastException, MandatoryPropertyException{
		Number i = ManagerExceptionUtils.testCast(properties, Number.class, SimulationProperties.MIN_MAX_RACIO_DIVIDEND_SENSE, false);
		double d = i.doubleValue();
		
		if( d <0)
			return  -1;
		else if (d > 0) return 1;
		
		throw new WrongFormulationException("");
	}


	@Override
	protected void createConstraints() throws WrongFormulationException,
			PropertyCastException, MandatoryPropertyException, SolverException {
		super.createConstraints();
		
		int tIdx = getIdxVar("t");
		int numberReactions = model.getNumberOfReactions();
		
		int varcIdx = model.getReactionIndex(getDivisorVariable());
		for(int i =0; i < numberReactions; i++){
			if(i==varcIdx) continue;
			IOverrideReactionBounds overrideRC = createModelOverride();
	        ReactionConstraint rc = overrideRC.getReactionConstraint(i);
//			ReactionConstraint rc = model.getReactionConstraint(i); //was overrideRC
			
			try {
				LPProblemRow rowLow = new LPProblemRow();
				rowLow.addTerm(i, -1d);
				rowLow.addTerm(tIdx, rc.getLowerLimit());
				problem.addConstraint( 
						new LPConstraint(LPConstraintType.LESS_THAN, rowLow, 0.0));
				
				LPProblemRow rowUpp = new LPProblemRow();
				rowUpp.addTerm(i, 1d);
				rowUpp.addTerm(tIdx, -rc.getUpperLimit());
				problem.addConstraint(
						new LPConstraint(LPConstraintType.LESS_THAN, rowUpp, 0.0));
			} catch (LinearProgrammingTermAlreadyPresentException e) {
				 throw new WrongFormulationException("Cannot add term " + i + "to Bounds var");
			}

		}
		
	}
	
	@Override
	public String getObjectiveFunctionToString() {
		try {
			return ((getIsMaximization())?"MAX":"MIN")+": ("+getDividendSense() + " "+getDividendVariable() + ")/"+getDivisorSense() + " " + getDivisorVariable();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
		
		

	public String getDivisorVariable() throws PropertyCastException, MandatoryPropertyException{
		
		return (String) ManagerExceptionUtils.testCast(properties, String.class, SimulationProperties.MIN_MAX_RACIO_DIVISOR, false);
	}
	
	public String getDividendVariable() throws PropertyCastException, MandatoryPropertyException{
		
		return (String) ManagerExceptionUtils.testCast(properties, String.class, SimulationProperties.MIN_MAX_RACIO_DIVIDEND, false);
	}
	
	public double getDefaultBounds(){
		Double d = getProperty("DEFAULT_UB_LB");
		if(d == null) d = 100000000d;
		return d;
	}


	public void setDivisorId(String id) {
		properties.put(SimulationProperties.MIN_MAX_RACIO_DIVISOR, id);
	}
	
	public void setDivisorSense(Number d){
		setProperty(SimulationProperties.MIN_MAX_RACIO_DIVISOR_SENSE, d);
	}
	
	public void setDividendId(String id) {
		properties.put(SimulationProperties.MIN_MAX_RACIO_DIVIDEND, id);
	}
	
	public void setDividendSense(Number d){
		setProperty(SimulationProperties.MIN_MAX_RACIO_DIVIDEND_SENSE, d);
	}
	
	@Override
	public SteadyStateSimulationResult convertLPSolutionToSimulationSolution(
			LPSolution solution) throws PropertyCastException,
			MandatoryPropertyException, WrongFormulationException,
		SolverException {
		SteadyStateSimulationResult res = super.convertLPSolutionToSimulationSolution(solution);
		MapStringNum transform = res.getFluxValues();
		FluxValueMap map = new FluxValueMap();
		
		Double t = solution.getValues().get(getIdxVar("t"));
		
		System.out.println("t = " + t);
		for(String id : transform.keySet()){
			map.put(id, transform.get(id)/t);
		}
		if(solution.getSolutionType().equals(LPSolutionType.INFEASIBLE))
			res.setOFvalue(Double.NaN);
		
		res.setFluxValues(map);
		res.addComplementaryInfoReactions("min_max_transformV", transform);
		return res;
		
		
	}
}
