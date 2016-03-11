package pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.IConvexSteadyStateSimulationMethod;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.IOverrideReactionBounds;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.OverrideSteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.UnderOverSingleReference;
import pt.uminho.ceb.biosystems.mew.core.simulation.exceptions.AutomaticOverUnderSimulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.ILPSolver;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPMapVariableValues;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.solvers.qp.IQPSolver;
import pt.uminho.ceb.biosystems.mew.solvers.qp.QPProblem;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.KeyPropertyChangeEvent;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.ListenerHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.java.TimeUtils;

/**
 * Persistent implementation of an <code>AbstractSSBasicSimulation</code>.
 * 
 * 
 * @author pmaia
 * @date Jan 19, 2014
 * @version 1.0
 * @since metabolic3persistent
 * @param <T>
 */
public abstract class AbstractSSBasicSimulation<T extends LPProblem> implements IConvexSteadyStateSimulationMethod, PropertyChangeListener {
	
	/** debug flag */
	protected static boolean						debug				= false;
	
	/** times debug flag */
	protected static boolean						debug_times			= false;
	
	/** a map to keep execution times - for debugging purposes */
	protected Map<String, Long>						times				= null;
	
	/** a variable to keep the time */
	protected long									initTime			= System.currentTimeMillis();
	
	/** the <code>ISteadyStateModel</code> instance */
	protected ISteadyStateModel						model				= null;
	
	/** the current problem to be solved */
	protected T										problem				= null;
	
	/** A list of <code>AbstractObjTerm</code> to be included in the formulation prior to simulation */
	protected List<AbstractObjTerm>					objTerms			= null;
	
	/** mapping from id to index */
	protected Map<String, Integer>					idToIndexVarMapings	= null;
	
	/** mapping from index to id */
	protected Map<Integer, String>					indexToIdVarMapings	= null;
	
	/** map of current properties */
	protected ListenerHashMap<String, Object>		properties			= null;
	
	/** set of optional properties */
	protected Set<String>							optionalProperties	= null;
	
	/** set of mandatory properties */
	protected Set<String>							mandatoryProperties	= null;
	
	/** the solver in use */
	protected ILPSolver								_solver				= null;
	
	/** a boolean flag indicating if the problem should be forcibly recreated or not */
	protected boolean								_recreateProblem	= false;
	
	/** a boolean flag indicating whether to recreate the objective function or not */
	protected boolean								_recreateOF			= false;
	
	/** a <code>SimulationSteadyStateControlCenter</code> instance to compute the over/under references */
	protected SimulationSteadyStateControlCenter	_ouRefCenter		= null;
	
	/**
	 * Default constructor
	 * 
	 * @param model
	 */
	public AbstractSSBasicSimulation(ISteadyStateModel model) {
		this.model = model;
		setIdToIndexVarMapings(new HashMap<String, Integer>());
		setIndexToIdVarMapings(new HashMap<Integer, String>());
		objTerms = new ArrayList<AbstractObjTerm>();
		properties = new ListenerHashMap<String, Object>();
		addPropertyChangeListener(this);
		initPropsKeys();
		if (debug_times) times = new LinkedHashMap<String, Long>();
	}
	
	private void initPropsKeys() {
		mandatoryProperties = new HashSet<String>();
		mandatoryProperties.add(SimulationProperties.SOLVER);
		
		optionalProperties = new HashSet<String>();
		optionalProperties.add(SimulationProperties.IS_OVERUNDER_SIMULATION);
		optionalProperties.add(SimulationProperties.OVERUNDER_REFERENCE_FLUXES);
		optionalProperties.add(SimulationProperties.OVERUNDER_2STEP_APPROACH);
		optionalProperties.add(SimulationProperties.ENVIRONMENTAL_CONDITIONS);
		optionalProperties.add(SimulationProperties.GENETIC_CONDITIONS);
		optionalProperties.add(SimulationProperties.OF_ASSOCIATED_VARIABLES);
		optionalProperties.add(SimulationProperties.OF_ASSOCIATED_CONSTRAINTS);
		optionalProperties.add(SimulationProperties.DEBUG_SOLVER_MODEL);
	}
	
	public abstract T constructEmptyProblem();
	
	protected abstract void createObjectiveFunction() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException;
	
	public abstract String getObjectiveFunctionToString();
	
	public void putObjectiveFunctionIntoProblem() throws WrongFormulationException {
		
		Map<String, Integer> newVars = new HashMap<String, Integer>();
		List<LPVariable> variables = new ArrayList<LPVariable>();
		List<LPConstraint> constraints = new ArrayList<LPConstraint>();
		
		for (AbstractObjTerm term : objTerms) {
			Map<String, Integer> nv = term.addObjectiveTermToProblem(problem, variables, constraints);
			if (nv != null) newVars.putAll(nv);
		}
		
		putNewVariables(newVars);
		
		setProperty(SimulationProperties.OF_NEW_VARS, newVars);
		setProperty(SimulationProperties.OF_ASSOCIATED_VARIABLES, variables);
		setProperty(SimulationProperties.OF_ASSOCIATED_CONSTRAINTS, constraints);
		
	}
	
	public void putNewVariables(Map<String, Integer> vars) {
		
		idToIndexVarMapings.putAll(vars);
		for (String id : vars.keySet()) {
			int i = vars.get(id);
			indexToIdVarMapings.put(i, id);
		}
	}
	
	public void removeVariables(Map<String, Integer> vars) {
		for (String var : vars.keySet()) {
			int index = idToIndexVarMapings.remove(var);
			indexToIdVarMapings.remove(index);
		}
	}
	
	public Integer getCurrentNumOfVar() {
		return problem.getNumberVariables();
	}
	
	public void putVarMappings(String id, int idx) {
		getIdToIndexVarMapings().put(id, idx);
		getIndexToIdVarMapings().put(idx, id);
	}
	
	public Integer getIdxVar(String id) {
		return getIdToIndexVarMapings().get(id);
	}
	
	public String getIdVar(int idx) {
		return getIndexToIdVarMapings().get(idx);
	}
	
	protected void createProblemIfEmpty() throws MandatoryPropertyException, PropertyCastException, WrongFormulationException {
		if (problem == null || _recreateProblem) {
			if (debug) System.out.println("[" + getClass().getSimpleName() + "] got event [CONSTRUCT EMPTY PROBLEM]");
			
			if (debug_times) initTime = System.currentTimeMillis();
			problem = constructEmptyProblem();
			if (debug_times) times.put("constructEmptyProblemProblem", System.currentTimeMillis() - initTime);
			
			if (debug_times) initTime = System.currentTimeMillis();
			createVariables();
			if (debug_times) times.put("createVariables", System.currentTimeMillis() - initTime);
			
			if (debug_times) initTime = System.currentTimeMillis();
			createConstraints();
			if (debug_times) times.put("createConstrains", System.currentTimeMillis() - initTime);
			
			if (debug_times) initTime = System.currentTimeMillis();
			createObjectiveFunction();
			if (debug_times) times.put("createObjectiveFunction", System.currentTimeMillis() - initTime);
			
			if (debug_times) initTime = System.currentTimeMillis();
			putObjectiveFunctionIntoProblem();
			if (debug_times) times.put("putObjectiveFunctionIntoProblem", System.currentTimeMillis() - initTime);
			
		}
	}
	
	protected void createVariables() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException {
		int numberVariables = model.getNumberOfReactions();
		for (int i = 0; i < numberVariables; i++) {
			Reaction r = model.getReaction(i);
			putVarMappings(r.getId(), i);
			
			ReactionConstraint rc = model.getReactionConstraint(i);
			LPVariable var = new LPVariable(r.getId(), rc.getLowerLimit(), rc.getUpperLimit());
			problem.addVariable(var);
		}	
	}
	
	protected void createConstraints() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException {
		int numberVariables = model.getNumberOfReactions();
		int numberConstraints = model.getNumberOfMetabolites();
		
		for (int i = 0; i < numberConstraints; i++) {
			LPProblemRow row = new LPProblemRow();
			for (int j = 0; j < numberVariables; j++) {
				double value = model.getStoichiometricValue(i, j);
				if (value != 0) try {
					row.addTerm(j, value);
				} catch (LinearProgrammingTermAlreadyPresentException e) {
					throw new WrongFormulationException("Cannot add term " + j + "to row with value: " + value);
				}
			}
			
			LPConstraint constraint = new LPConstraint(LPConstraintType.EQUALITY, row, 0.0);
			problem.addConstraint(constraint);
		}
	}
	
	public IOverrideReactionBounds createModelOverride() throws MandatoryPropertyException, PropertyCastException, WrongFormulationException {
		
		boolean isOverUnder = false;
		IOverrideReactionBounds overrideRC;
		
		try {
			isOverUnder = (Boolean) ManagerExceptionUtils.testCast(properties, Boolean.class, SimulationProperties.IS_OVERUNDER_SIMULATION, false);
		} catch (PropertyCastException e) {
			System.err.println("The property " + SimulationProperties.IS_OVERUNDER_SIMULATION + " was ignored!!\n Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {
			isOverUnder = false;
		}
		
		EnvironmentalConditions environmentalConditions = getEnvironmentalConditions();
		GeneticConditions geneticConditions = getGeneticConditions();
		
//		System.out.println("["+getClass().getSimpleName()+"] twostep = "+properties.get(SimulationProperties.OVERUNDER_2STEP_APPROACH));
		if (isOverUnder) {
			Boolean is2stepApproach = (Boolean) ManagerExceptionUtils.testCast(properties, Boolean.class, SimulationProperties.OVERUNDER_2STEP_APPROACH, true);
			FluxValueMap reference = null;
			if (is2stepApproach != null && is2stepApproach) {
				try {
					reference = computeOverUnderReference(model, environmentalConditions, geneticConditions);
				} catch (Exception e) {
					throw new AutomaticOverUnderSimulationException(e); 
				}
			} else
				reference = (FluxValueMap) ManagerExceptionUtils.testCast(properties, FluxValueMap.class, SimulationProperties.OVERUNDER_REFERENCE_FLUXES, false);
			
			if (geneticConditions == null) throw new WrongFormulationException(new NullPointerException("GeneticConditions"));
			overrideRC = new UnderOverSingleReference(model, environmentalConditions, geneticConditions, reference);
		} else {
			overrideRC = new OverrideSteadyStateModel(model, environmentalConditions, geneticConditions);
		}
		
		return overrideRC;
	}
	
	public FluxValueMap computeOverUnderReference(ISteadyStateModel model, EnvironmentalConditions environmentalConditions, GeneticConditions geneticConditions) throws PropertyCastException, MandatoryPropertyException{
		
		if(_ouRefCenter==null){
			_ouRefCenter = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.PFBA);
			_ouRefCenter.setMaximization(true);
			_ouRefCenter.setSolver(getSolverType());
			_ouRefCenter.setFBAObjSingleFlux(model.getBiomassFlux(), 1.0);
		}

		GeneticConditions gc = null;
		if(geneticConditions.isGenes()){
			GeneChangesList gcl = new GeneChangesList();
			for(String g : geneticConditions.getGeneList().keySet())
				if(geneticConditions.getGeneList().get(g) == 0.0)
					gcl.addGeneKnockout(g);
			
			gc = new GeneticConditions(gcl, (ISteadyStateGeneReactionModel) model, false);
		}else{
			ReactionChangesList rcl = new ReactionChangesList();
			for(String r : geneticConditions.getReactionList().keySet())
				if(geneticConditions.getReactionList().get(r) == 0.0)
					rcl.addReactionKnockout(r);
			
			gc = new GeneticConditions(rcl, false);
		}
		
		_ouRefCenter.setEnvironmentalConditions(environmentalConditions);
		_ouRefCenter.setGeneticConditions(gc);
		
		SteadyStateSimulationResult res;
		try {
			res = _ouRefCenter.simulate();
		} catch (Exception e) {
			throw new AutomaticOverUnderSimulationException(e);
		}
//		System.out.println("["+getClass().getSimpleName()+"] overUnder2stepApproach ("+res.getOFString()+" = "+res.getOFvalue()+")");
//		System.out.println("\tGC FULL = "+geneticConditions.toStringOptions(",",false));
//		System.out.println("\tGC REF  = "+gc.toStringOptions(",",false));
		return res.getFluxValues();
	}
	
	protected LPSolution simulateProblem() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException {
		
		if (debug) System.out.println("\n[" + getClass().getSimpleName() + "]:\n" + MapUtils.prettyToString(properties, "=", "\n\t"));
		SolverType solverType = getSolverType();
		return (solverType.supportsPersistentModel()) ? simulatePersistent(solverType) : simulateVolatile(solverType);
	}
	
	private LPSolution simulatePersistent(SolverType solverType) throws WrongFormulationException, MandatoryPropertyException, PropertyCastException {
		LPProblem p = getProblem();
		
		if (_solver == null || _recreateProblem) {
			if (_solver != null) _solver.resetSolver();
			_solver = solverType.lpSolver(p);
		}
		
		if (isRecreateOF()) recreateObjectiveFunction();
		
		IOverrideReactionBounds override = createModelOverride();
		setVariables(override);
		
		LPSolution solution = null;
		try {
			solution = _solver.solve();
			String file = (String) properties.get(SimulationProperties.DEBUG_SOLVER_MODEL);
			if (file != null) _solver.saveModelToMPS(file, true);
		} finally {
			String file = (String) properties.get(SimulationProperties.DEBUG_SOLVER_MODEL);
			if (file != null) _solver.saveModelToMPS(file, true);
			unsetVariables(override);
			_recreateProblem = false;
			setRecreateOF(false);
		}
		
//		_solver.resetSolver();
		
		return solution;
	}
	
	public void forceSolverCleanup(){
		if(_solver!=null)
			_solver.resetSolver();
	}
	
	
	private LPSolution simulateVolatile(SolverType solverType) throws WrongFormulationException, MandatoryPropertyException, PropertyCastException  {
		
		problem = null;
		objTerms.clear();
		LPProblem p = getProblem();
		
		if (isRecreateOF()) recreateObjectiveFunction();
		
		IOverrideReactionBounds override = createModelOverride();
		setVariables(override);
		
		if (_solver != null) _solver.resetSolver();
		
		LPSolution solution = null;
		if (QPProblem.class.isAssignableFrom(problem.getClass())) {
			IQPSolver solver = solverType.qpSolver((QPProblem) p);
			try {
				solution = solver.solve();
			} finally {
				String file = (String) properties.get(SimulationProperties.DEBUG_SOLVER_MODEL);
				if (file != null && solver!=null) solver.saveModelToMPS(file, true);
				setRecreateOF(false);
			}
		} else {
			_solver = solverType.lpSolver(p);
			try {
				solution = _solver.solve();
			} finally{
				String file = (String) properties.get(SimulationProperties.DEBUG_SOLVER_MODEL);
				if (file != null && _solver!=null) _solver.saveModelToMPS(file, true);
				setRecreateOF(false);
			}
		}
		
		
		
		return solution;
	}
	
	@SuppressWarnings("unchecked")
	protected void recreateObjectiveFunction() {
			objTerms.clear();
			
			if (debug_times) initTime = System.currentTimeMillis();
			Map<String, Integer> oldVars = (Map<String, Integer>) getProperty(SimulationProperties.OF_NEW_VARS);
			removeVariables(oldVars);
			if (debug_times) times.put("recreateObjectiveFunction.removeVarMappings", System.currentTimeMillis() - initTime);
			
			if (debug_times) initTime = System.currentTimeMillis();
			List<LPVariable> oldLPvars = (List<LPVariable>) getProperty(SimulationProperties.OF_ASSOCIATED_VARIABLES);
			problem.removeVariableRange(oldLPvars);
			if (debug_times) times.put("recreateObjectiveFunction.removeVariableRange", System.currentTimeMillis() - initTime);
			
			if (debug_times) initTime = System.currentTimeMillis();
			List<LPConstraint> oldLPConstraints = (List<LPConstraint>) getProperty(SimulationProperties.OF_ASSOCIATED_CONSTRAINTS);
			problem.removeConstraintRange(oldLPConstraints);
			if (debug_times) times.put("recreateObjectiveFunction.removeConstraintRange", System.currentTimeMillis() - initTime);
			
			if (debug_times) initTime = System.currentTimeMillis();
			createObjectiveFunction();
			if (debug_times) times.put("recreateObjectiveFunction.createOF", System.currentTimeMillis() - initTime);
			
			if (debug_times) initTime = System.currentTimeMillis();
			putObjectiveFunctionIntoProblem();
			if (debug_times) times.put("recreateObjectiveFunction.putOFinProblem", System.currentTimeMillis() - initTime);
			
			if (debug_times) initTime = System.currentTimeMillis();
			problem.updateLPObjectiveFunction();
			if (debug_times) times.put("recreateObjectiveFunction.updateLPObjectiveFunction", System.currentTimeMillis() - initTime);
			
	}
	
	public SteadyStateSimulationResult simulate() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException {
		
		if (debug_times) initTime = System.currentTimeMillis();
		preSimulateActions();
		if (debug_times) times.put("pre-simulate", System.currentTimeMillis() - initTime);
		
		if (debug_times) initTime = System.currentTimeMillis();
		LPSolution solution = null;
		solution = simulateProblem();
		if (debug_times) times.put("solve", System.currentTimeMillis() - initTime);
		
		if (debug_times) initTime = System.currentTimeMillis();
		SteadyStateSimulationResult result = convertLPSolutionToSimulationSolution(solution);
		
		if (debug_times) times.put("convert", System.currentTimeMillis() - initTime);
		
		if (debug_times) initTime = System.currentTimeMillis();
		postSimulateActions();
		if (debug_times) times.put("post-simulate", System.currentTimeMillis() - initTime);
		
		if (debug_times) {
			TimeUtils.printTimeMap(times);
			times.clear();
		}
		
		return result;
	}
	
	public void preSimulateActions() {
	};
	
	public void postSimulateActions() {
	};
	
	public SteadyStateSimulationResult convertLPSolutionToSimulationSolution(LPSolution solution) throws PropertyCastException, MandatoryPropertyException {
		
		FluxValueMap fluxValues = getFluxValueListFromLPSolution(solution);
		SteadyStateSimulationResult res = new SteadyStateSimulationResult(model, getMethod(), fluxValues);
		
		if (solution == null) {
			res.setSolutionType(LPSolutionType.ERROR);
			String solverout = "The solver " + getSolverType() + " cannot generate an output...";
			res.setSolverOutput(solverout);
			res.setOFvalue(Double.NaN);
		} else {
			putMetaboliteExtraInfo(solution, res);
			putReactionExtraInfo(solution, res);
			res.setSolutionType(solution.getSolutionType());
			res.setSolverOutput(solution.getSolverOutput());
			res.setOFvalue(solution.getOfValue());
		}
		res.setEnvironmentalConditions(getEnvironmentalConditions());
		res.setGeneticConditions(getGeneticConditions());
		res.setOFString(getObjectiveFunctionToString());
		
		return res;
	}
	
	protected void putReactionExtraInfo(LPSolution solution, SteadyStateSimulationResult res) {
		Map<String, MapStringNum> complementary = new HashMap<String, MapStringNum>();
		
		for (String id : solution.getVariableMetricsIds()) {
			
			LPMapVariableValues cInfo = solution.getPerVariableMetric(id);
			MapStringNum values = convertLPMapToMapString(model, cInfo, true);
			complementary.put(id, values);
		}
		
		res.setComplementaryInfoReactions(complementary);
		
	}
	
	protected void putMetaboliteExtraInfo(LPSolution solution, SteadyStateSimulationResult res) {
		Map<String, MapStringNum> complementaryInfoMetabolites = new HashMap<String, MapStringNum>();
		
		for (String id : solution.getConstraintMetricsIds()) {
			
			LPMapVariableValues cInfo = solution.getPerConstraintMetric(id);
			MapStringNum values = convertLPMapToMapString(model, cInfo, false);
			complementaryInfoMetabolites.put(id, values);
		}
		
		res.setComplementaryInfoMetabolites(complementaryInfoMetabolites);
	}
	
	protected MapStringNum convertLPMapToMapString(ISteadyStateModel model, LPMapVariableValues cInfo, Boolean isReactions) {
		MapStringNum values = new MapStringNum();
		
		for (Integer idx : cInfo.keySet()) {
			Double value = cInfo.get(idx);
			String id = (isReactions) ? model.getReactionId(idx) : model.getMetaboliteId(idx);
			
			if (id != null) values.put(id, value);
		}
		
		return values;
	}
	
	public FluxValueMap getFluxValueListFromLPSolution(LPSolution solution) {
		
		LPMapVariableValues varValueList = null;
		if (solution != null) varValueList = solution.getValues();
		
		FluxValueMap fluxValues = new FluxValueMap();
		for (String rId : model.getReactions().keySet()) {
			
			int idx = getReactionVariableIndex(rId);
			double value = Double.NaN;
			if (varValueList != null) value = varValueList.get(idx);
			
			if (solution != null && !(solution.getSolutionType().equals(LPSolutionType.OPTIMAL) || solution.getSolutionType().equals(LPSolutionType.FEASIBLE) || solution.getSolutionType().equals(LPSolutionType.UNKNOWN))){
				value = Double.NaN;
			}
			
			fluxValues.put(rId, value);
		}
		
		if (debug && solution != null && !(solution.getSolutionType().equals(LPSolutionType.OPTIMAL) || solution.getSolutionType().equals(LPSolutionType.FEASIBLE) || solution.getSolutionType().equals(LPSolutionType.UNKNOWN))){
			System.out.println(">>>>>>>>>NAN = "+solution.getProblem().getNumberVariables()+"~"+solution.getProblem().getNumberConstraints()+" / "+this.getClass()+" / "+solution.getSolutionType().toString());
		}
		
		return fluxValues;
	}
	
	protected void setVariables(IOverrideReactionBounds override) throws PropertyCastException, MandatoryPropertyException {
		
		for (String r : override.getOverriddenReactions()) {
			Integer index = idToIndexVarMapings.get(r);
			if (index != null) {
				ReactionConstraint rc = override.getReactionConstraint(r);
				problem.changeVariableBounds(index, rc.getLowerLimit(), rc.getUpperLimit());
			} else if (debug) System.out.println(">> Trying to set variable [" + r + "] but variable is not available in this model.");
		}
	}
	
	protected void unsetVariables(IOverrideReactionBounds override) throws PropertyCastException, MandatoryPropertyException {
		
		for (String r : override.getOverriddenReactions()) {
			Integer index = idToIndexVarMapings.get(r);
			if (index != null) {
				ReactionConstraint rc = model.getReactionConstraint(r);
				problem.changeVariableBounds(index, rc.getLowerLimit(), rc.getUpperLimit());
			} else if (debug) System.out.println("<< Trying to unset variable [" + r + "] but variable is not available in this model.");
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		KeyPropertyChangeEvent event = (KeyPropertyChangeEvent) evt;
		
		switch (event.getPropertyName()) {
		
			case ListenerHashMap.PROP_REM: {
				
				if (debug) System.out.println("[" + getClass().getSimpleName() + "]: got event [REM]: " + event.getKey() + " = " + evt.getOldValue());
				
				break;
			}
			case ListenerHashMap.PROP_PUT: {
				
				if (debug) System.out.println("[" + getClass().getSimpleName() + "]: got event [PUT]: " + event.getKey() + " from " + evt.getOldValue() + " to " + evt.getNewValue());
				
				break;
			}
			case ListenerHashMap.PROP_UPDATE: {
				
				if (debug) System.out.println("[" + getClass().getSimpleName() + "]: got event [UPDATE]: " + event.getKey() + " from " + evt.getOldValue() + " to " + evt.getNewValue());
				
				if (event.getKey().equals(SimulationProperties.IS_MAXIMIZATION)) {
					SolverType solver = getSolverType();
					
					if(solver!=null && solver.supportsPersistentModel()){
						problem.changeObjectiveSense((boolean) evt.getNewValue());						
					} else{
						setRecreateOF(true);						
					}
				}
				
				if (event.getKey().equals(SimulationProperties.OBJECTIVE_FUNCTION)) {
					setRecreateOF(true);
				}
				
				break;
			}
			default:
				break;
		
		}
	}
	
	public void clearAllProperties() {
		if (debug) System.out.println("[" + getClass().getSimpleName() + "] got event [CLEAR ALL PROPERTIES]");
		properties.clear();
	}
	
	public LPProblem getProblem() throws MandatoryPropertyException, PropertyCastException, WrongFormulationException {
		createProblemIfEmpty();
		return problem;
	}
	
	public ISteadyStateModel getModel() {
		return model;
	}
	
	public Map<String, Integer> getIdToIndexVarMapings() {
		return idToIndexVarMapings;
	}
	
	public Map<Integer, String> getIndexToIdVarMapings() {
		return indexToIdVarMapings;
	}
	
	public void setIdToIndexVarMapings(Map<String, Integer> idToIndexVarMapings) {
		this.idToIndexVarMapings = idToIndexVarMapings;
	}
	
	public void setIndexToIdVarMapings(Map<Integer, String> indexToIdVarMapings) {
		this.indexToIdVarMapings = indexToIdVarMapings;
	}
	
	public Integer getReactionVariableIndex(int reactionIdx) {
		return getReactionVariableIndex(model.getReactionId(reactionIdx));
	}
	
	public Integer getReactionVariableIndex(String reactionId) {
		return idToIndexVarMapings.get(reactionId);
		
	}
	
	public GeneticConditions getGeneticConditions() throws PropertyCastException, MandatoryPropertyException {
		return (GeneticConditions) ManagerExceptionUtils.testCast(properties, GeneticConditions.class, SimulationProperties.GENETIC_CONDITIONS, true);
	}
	
	public EnvironmentalConditions getEnvironmentalConditions() throws PropertyCastException, MandatoryPropertyException {
		EnvironmentalConditions ec = ManagerExceptionUtils.testCast(properties, EnvironmentalConditions.class, SimulationProperties.ENVIRONMENTAL_CONDITIONS, true);
		return ec;
	}
	
	public void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions) {
		setProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS, environmentalConditions);
	}
	
	public void setGeneticConditions(GeneticConditions geneticConditions) {
		setProperty(SimulationProperties.GENETIC_CONDITIONS, geneticConditions);
	}
	
	public void setSolverType(SolverType solver) {
		setProperty(SimulationProperties.SOLVER, solver);
	}
	
	public SolverType getSolverType() throws PropertyCastException, MandatoryPropertyException {
		SolverType solverType = (SolverType) ManagerExceptionUtils.testCast(properties, SolverType.class, SimulationProperties.SOLVER, false);
		return solverType;
	}
	
	public void setProperty(String m, Object o) {
		properties.put(m, o);
	}
	
	public Set<String> getPossibleProperties() {
		return optionalProperties;
	}
	
	public Set<String> getMandatoryProperties() {
		return mandatoryProperties;
	}
	
	public void putAllProperties(Map<String, Object> p) {
		this.properties.putAll(p);
	}
	
	public String getMethod() {
		return (String) properties.get(SimulationProperties.METHOD_NAME);
	}
	
	public Class<?> getFormulationClass() {
		return problem.getClass();
	}
	

	public boolean isRecreateOF() {
		return _recreateOF;
	}
	
	public void setRecreateOF(boolean recreateOF) {
		this._recreateOF = recreateOF;
	}
	
	public List<AbstractObjTerm> getObjTerms() {
		return objTerms;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (properties != null) properties.addPropertyChangeListener(listener);
	}
	
	public void finalize() throws Throwable {
		if (properties != null) properties.removeAllPropertyChangeListeners();
		super.finalize();
	}
	
	public void saveModelToMPS(String file, boolean includeTime) {
		if (_solver != null) _solver.saveModelToMPS(file, includeTime);
	}
	

	public <T> T getProperty(String k) {
		return (T)properties.get(k);
	}

}
