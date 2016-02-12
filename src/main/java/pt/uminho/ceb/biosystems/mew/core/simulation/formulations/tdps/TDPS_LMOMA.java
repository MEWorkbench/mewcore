package pt.uminho.ceb.biosystems.mew.core.simulation.formulations.tdps;

import java.io.IOException;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSReferenceSimulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.L1VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;

// A method to calculate if a LMOMA solution is unique/robust
public class TDPS_LMOMA<T extends AbstractSSBasicSimulation<?>> extends AbstractSSReferenceSimulation<LPProblem> {
	
	//modificado para LMOMA
	public static double	OF_RELAX	= 2;
	//objectivo FV_LMOMA
	protected T				initProblem;
							
	public TDPS_LMOMA(ISteadyStateModel model) {
		super(model);
		initPFBAPros();
	}
	
	private void initPFBAPros() {
		optionalProperties.add(SimulationProperties.PARSIMONIOUS_PROBLEM);
		optionalProperties.add(SimulationProperties.PARSIMONIOUS_OBJECTIVE_VALUE);
		optionalProperties.add(SimulationProperties.RELAX_COEF);
	}
	
	public double getObjectiveValue() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, IOException, SolverException {
		Double value = null;
		try {
			value = (Double) ManagerExceptionUtils.testCast(properties, Double.class, SimulationProperties.PARSIMONIOUS_OBJECTIVE_VALUE, true);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored reason: " + e.getMessage());
			value = null;
		} catch (MandatoryPropertyException e) {
			value = null;
		}
		
		if (value == null) {
			SteadyStateSimulationResult result;
			result = initProblem.simulate();
			
			value = result.getOFvalue();
			setProperty(SimulationProperties.PARSIMONIOUS_OBJECTIVE_VALUE, value);
			
			System.out.println("MORA objective function:\t" + value);
		}
		
		return value;
	}
	
	public AbstractSSBasicSimulation getInitProblem() {
		
		if (initProblem == null) {
			try {
				initProblem = (T) ManagerExceptionUtils.testCast(properties, AbstractSSBasicSimulation.class, SimulationProperties.PARSIMONIOUS_PROBLEM, true);
			} catch (MandatoryPropertyException e) {
				initProblem = null;
			} catch (PropertyCastException e) {
				System.err.println("Property ignored reason: " + e.getMessage());
				initProblem = null;
			}
			
			if (initProblem == null) {
				
				//calcular um MORA de referencia
				initProblem = (T) new TDPS(model);
				initProblem.putAllProperties(properties);
				setProperty(SimulationProperties.PARSIMONIOUS_PROBLEM, initProblem);
			}
		}
		
		return initProblem;
	}
	
	@Override
	public LPProblem constructEmptyProblem() {
		getInitProblem();
		return initProblem.constructEmptyProblem();
	}
	
	@Override
	protected void createConstraints() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException {
		
		getInitProblem();
		List<LPConstraint> constraits = initProblem.getProblem().getConstraints();
		
		problem.setConstraints(constraits);
		
		try {
			//the additional 0.1 avoids error of wild-type is simulated
			
			problem.addConstraint(initProblem.getProblem().getObjectiveFunction().getRow(),
					LPConstraintType.LESS_THAN, getObjectiveValue() * getRelaxCoef() + 0.1);
			System.out.println("Relaxed MORA OF:\t" + (getObjectiveValue() * getRelaxCoef() + 0.1));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new WrongFormulationException(e);
		} catch (SolverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new WrongFormulationException(e);
		}
		
	}
	
	protected double getRelaxCoef() {
		
		Double coef = OF_RELAX;
		
		setProperty(SimulationProperties.RELAX_COEF, coef);
		
		return coef;
	}
	
	@Override
	protected void createVariables() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException {
		problem.setVariables(initProblem.getProblem().getVariables());
		setIdToIndexVarMapings(initProblem.getIdToIndexVarMapings());
		setIndexToIdVarMapings(initProblem.getIndexToIdVarMapings());
	}
	
	@Override
	protected void createObjectiveFunction() {
		//max vs min
		
		try {
			System.out.println("Minimization of flux changes");
			
			problem.setObjectiveFunction(new LPProblemRow(), false);
			getWTReference();
			boolean useDrains = getUseDrainsInRef();
			for (String id : wtReference.keySet()) {
				int idxVar = idToIndexVarMapings.get(id);
				double value = wtReference.get(id);
				
				if ((useDrains || !model.getReaction(id).getType().equals(ReactionType.DRAIN))) {
					objTerms.add(new L1VarTerm(idxVar, -value));
				}
			}
		} catch (Exception e) {
		}
	}
	
	public void setInitProblem(T problem) {
		this.initProblem = problem;
		setProperty(SimulationProperties.PARSIMONIOUS_PROBLEM, initProblem);
	}
	
	public void setObjectiveValue(Double value) {
		setProperty(SimulationProperties.PARSIMONIOUS_OBJECTIVE_VALUE, value);
	}
	
	@Override
	public String getObjectiveFunctionToString() {
		
		return "min Σ|V|";
	}
	
//	@Override
//	public SteadyStateSimulationResult convertLPSolutionToSimulationSolution(
//			LPSolution solution) throws PropertyCastException,
//			MandatoryPropertyException {
//		
//	
//		for(String id: idToIndexVarMapings.keySet()){
//			System.out.println(id + "\t" + solution.getValues().get(idToIndexVarMapings.get(id)));
//		}
//
//		return super.convertLPSolutionToSimulationSolution(solution);
//	}

}
