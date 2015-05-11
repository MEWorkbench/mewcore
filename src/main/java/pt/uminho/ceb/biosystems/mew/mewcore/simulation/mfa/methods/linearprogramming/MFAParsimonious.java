package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.methods.linearprogramming;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.L1VarTerm;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.methods.MFAWithSolvers;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.properties.MFAProperties;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.KeyPropertyChangeEvent;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.ListenerHashMap;

public class MFAParsimonious<T extends MFAWithSolvers<?>> extends MFAWithSolvers<LPProblem> {
	
	public static final double	DEFAULT_RELAX					= 0.99999;
	
	protected T					internalProblem					= null;
	protected LPConstraint		parsimoniousConstraint			= null;
	
	protected boolean			_updateParsimoniousConstraint	= false;
	protected boolean			_replaceParsimoniousConstraint	= false;
	
	public MFAParsimonious(ISteadyStateModel model) {
		super(model);
		initParsimoniousProperties();
	}
	
	private void initParsimoniousProperties() {
		optionalProperties.add(SimulationProperties.RELAX_COEF);
	}
	
	@SuppressWarnings("unchecked")
	public MFAWithSolvers<?> getInternalProblem() {
		
		if (internalProblem == null) {
			try {
				internalProblem = (T) ManagerExceptionUtils.testCast(properties, MFAWithSolvers.class, SimulationProperties.PARSIMONIOUS_PROBLEM, true);
			} catch (MandatoryPropertyException e) {
				internalProblem = null;
			} catch (PropertyCastException e) {
				System.err.println("Property ignored reason: " + e.getMessage());
				internalProblem = null;
			}
			
			if (internalProblem == null) {
				internalProblem = (T) new MFALP(model);
				internalProblem.putAllProperties(properties);
				setProperty(SimulationProperties.PARSIMONIOUS_PROBLEM, internalProblem);
			}
		}
		
		return internalProblem;
	}
	
	@Override
	public LPProblem constructEmptyProblem() {
		getInternalProblem().constructEmptyProblem();
		return new LPProblem();
	}
	
	@Override
	protected void createVariables() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException {
		List<LPVariable> newVars = new ArrayList<LPVariable>();
		for (LPVariable var : getInternalProblem().getProblem().getVariables()) {
			newVars.add(var.clone());
			problem.addVariable(var);
		}
		
		Map<String, Integer> id2index = new HashMap<String, Integer>();
		Map<Integer, String> index2id = new HashMap<Integer, String>();
		id2index.putAll(getInternalProblem().getIdToIndexVarMapings());
		index2id.putAll(getIndexToIdVarMapings());
		setIdToIndexVarMapings(id2index);
		setIndexToIdVarMapings(index2id);
	}
	
	@Override
	protected void createConstraints() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException {
		
		List<LPConstraint> newConsts = new ArrayList<LPConstraint>();
		for (LPConstraint cons : getInternalProblem().getProblem().getConstraints()) {
			newConsts.add(cons.clone());
			problem.addConstraint(cons);
			setProperty(MFAProperties.FLUX_RATIO_LP_CONSTRAINTS, getInternalProblem().getProperty(MFAProperties.FLUX_RATIO_LP_CONSTRAINTS));
		}
		
		try {
			double objectiveValue = getObjectiveValue();
			parsimoniousConstraint = createParsimoniousConstraint(objectiveValue);
			problem.addConstraint(parsimoniousConstraint);
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new WrongFormulationException(e);
		} catch (SolverException e) {
			e.printStackTrace();
			throw new WrongFormulationException(e);
		}
	}
	
	protected LPConstraint createParsimoniousConstraint(double objectiveValue) throws WrongFormulationException, MandatoryPropertyException, PropertyCastException {
		LPProblemRow fbaRow = getInternalProblem().getProblem().getObjectiveFunction().getRow();
		return new LPConstraint(LPConstraintType.EQUALITY, fbaRow, objectiveValue);
	}
	
	@SuppressWarnings("unchecked")
	protected void replaceParsimoniousOFConstraint() {
		problem.removeConstraint(parsimoniousConstraint);
		
		Map<String, Integer> vars = (Map<String, Integer>) getInternalProblem().getProperty(SimulationProperties.OF_NEW_VARS);
		List<LPVariable> variables = (List<LPVariable>) getInternalProblem().getProperty(SimulationProperties.OF_ASSOCIATED_VARIABLES);
		List<LPConstraint> constraints = (List<LPConstraint>) getInternalProblem().getProperty(SimulationProperties.OF_ASSOCIATED_CONSTRAINTS);
		
		for (LPVariable v : variables) {
			try {
				problem.removeVariable(v);
			} catch (WrongFormulationException e) {
				e.printStackTrace();
			}
		}
		for (LPConstraint c : constraints) {
			problem.removeConstraint(c);
		}
		
		removeVariables(vars);
		
		try {
			
			double objectiveValue = computeObjectiveValue();
			
			vars = (Map<String, Integer>) getInternalProblem().getProperty(SimulationProperties.OF_NEW_VARS);
			variables = (List<LPVariable>) getInternalProblem().getProperty(SimulationProperties.OF_ASSOCIATED_VARIABLES);
			constraints = (List<LPConstraint>) getInternalProblem().getProperty(SimulationProperties.OF_ASSOCIATED_CONSTRAINTS);
			putNewVariables(vars);
			
			for (LPVariable v : variables) {
				problem.addVariableAt(vars.get(v.getVariableName()), v);
			}
			for (LPConstraint c : constraints) {
				problem.addConstraint(c);
			}
			
			parsimoniousConstraint = createParsimoniousConstraint(objectiveValue);
			problem.addConstraint(parsimoniousConstraint);
		} catch (WrongFormulationException | SolverException | PropertyCastException | MandatoryPropertyException | IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void updateParsimoniousOFConstraint() {
		
		double objectiveValue;
		try {
			objectiveValue = computeObjectiveValue();
			int index = problem.getConstraints().indexOf(parsimoniousConstraint);
			problem.changeConstraintBound(index, objectiveValue);
		} catch (WrongFormulationException | SolverException | PropertyCastException | MandatoryPropertyException | IOException e) {
			e.printStackTrace();
		}
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
			value = computeObjectiveValue();
		}
		
		return value;
	}
	
	public double computeObjectiveValue() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, IOException, SolverException {
		
		SteadyStateSimulationResult result;
		result = getInternalProblem().simulate();
		double value = result.getOFvalue() * getRelaxCoef();
		return value;
	}
	
	protected double getRelaxCoef() {
		
		Double coef = null;
		try {
			coef = (Double) ManagerExceptionUtils.testCast(properties, Double.class, SimulationProperties.RELAX_COEF, false);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored reason: " + e.getMessage());
			coef = DEFAULT_RELAX;
		} catch (MandatoryPropertyException e) {
			coef = DEFAULT_RELAX;
		}
		
		setProperty(SimulationProperties.RELAX_COEF, coef);
		
		return coef;
	}
	
	@Override
	protected void createObjectiveFunction() {
		
		problem.setObjectiveFunction(new LPProblemRow(), false);
		
		Set<String> reactionIds = model.getReactions().keySet();
		for (String rId : reactionIds) {
			int varIdx = idToIndexVarMapings.get(rId);
			objTerms.add(new L1VarTerm(varIdx));
		}
	}
	
	public void setInitProblem(T problem) {
		this.internalProblem = problem;
		setProperty(SimulationProperties.PARSIMONIOUS_PROBLEM, internalProblem);
	}
	
	public void setObjectiveValue(Double value) {
		setProperty(SimulationProperties.PARSIMONIOUS_OBJECTIVE_VALUE, value);
	}
	
	@Override
	public String getObjectiveFunctionToString() {
		
		return "min Σ|V|";
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		KeyPropertyChangeEvent event = (KeyPropertyChangeEvent) evt;
		
		String key = (String) event.getKey();
		
		switch (event.getPropertyName()) {	
			case ListenerHashMap.PROP_PUT: {
				
				if (debug) System.out.println("[" + getClass().getSimpleName() + "]: got event [PUT]: " + event.getKey() + " from " + evt.getOldValue() + " to " + evt.getNewValue());
				
				if (key.equals(SimulationProperties.SOLVER)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(SimulationProperties.IS_MAXIMIZATION)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(SimulationProperties.OBJECTIVE_FUNCTION)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(SimulationProperties.ENVIRONMENTAL_CONDITIONS)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(SimulationProperties.GENETIC_CONDITIONS)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(SimulationProperties.IS_OVERUNDER_SIMULATION)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(SimulationProperties.OVERUNDER_REFERENCE_FLUXES)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(SimulationProperties.OVERUNDER_2STEP_APPROACH)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(MFAProperties.MEASURED_FLUXES)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(MFAProperties.FLUX_RATIO_CONSTRAINTS)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					if(problem!=null){
						setUpdateFRConstraints(true);
						setUpdateParsimoniousConstraint(true);
					}
				}
				
				break;
			}
			case ListenerHashMap.PROP_UPDATE: {
				
				if (debug) System.out.println("[" + getClass().getSimpleName() + "]: got event [UPDATE]: " + event.getKey() + " from " + evt.getOldValue() + " to " + evt.getNewValue());
				
				if (key.equals(SimulationProperties.SOLVER)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(SimulationProperties.IS_MAXIMIZATION)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					setUpdateParsimoniousConstraint(true);
				}
				
				if (key.equals(SimulationProperties.OBJECTIVE_FUNCTION)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					_replaceParsimoniousConstraint = true;
				}
				
				if (key.equals(SimulationProperties.ENVIRONMENTAL_CONDITIONS)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					setUpdateParsimoniousConstraint(true);
				}
				
				if (key.equals(SimulationProperties.GENETIC_CONDITIONS)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					setUpdateParsimoniousConstraint(true);
				}
				
				if (key.equals(SimulationProperties.IS_OVERUNDER_SIMULATION)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(SimulationProperties.OVERUNDER_REFERENCE_FLUXES)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(SimulationProperties.OVERUNDER_2STEP_APPROACH)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(MFAProperties.MEASURED_FLUXES)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
//					_updateParsimoniousConstraint = true;
				}
				
				if (key.equals(MFAProperties.FLUX_RATIO_CONSTRAINTS)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					setUpdateParsimoniousConstraint(true);
					setUpdateFRConstraints(true);										
				}
				
				break;
			}
			default:
				break;
		
		}
	}
	
	@Override
	public void clearAllProperties() {
		super.clearAllProperties();
		getInternalProblem().clearAllProperties();
	}

	@Override
	public void preSimulateActions() {
		super.preSimulateActions();
		if(_replaceParsimoniousConstraint){
			replaceParsimoniousOFConstraint();
		}else if(_updateParsimoniousConstraint){
			updateParsimoniousOFConstraint();
		}
	}

	@Override
	public void postSimulateActions() {		
		_replaceParsimoniousConstraint = false;
		_updateParsimoniousConstraint = false;
	}
	
	public void setUpdateParsimoniousConstraint(boolean update){
		_updateParsimoniousConstraint = update;
	}
}
