package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.InfeasibleProblemException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.SolverConstructionException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.SolverDefinitionException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.IOverrideReactionBounds;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.ISteadyStateSimulationMethod;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.OverrideSteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.UnderOverSingleReference;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;

public abstract class AbstractSSBasicSimulation<T extends LPProblem> implements ISteadyStateSimulationMethod{

	public static boolean debug = false;
	
	//Debug
	protected Map<String,Long> times;
	protected long initType = System.currentTimeMillis();
	
	protected ISteadyStateModel model;

	protected T problem = null;
	protected List<AbstractObjTerm> objTerms;
	
	protected Map<String, Integer> idToIndexVarMapings;
	protected Map<Integer, String> indexToIdVarMapings;
	
	protected Map<String, Object> propreties;
	protected IOverrideReactionBounds overrideRC;
	
	protected boolean isProblemCreated = false;
	
	protected Set<String> possibleProperties;
	protected Set<String> mandatoryProps;
//	protected LPSolution solution;
	
	public abstract T constructEmptyProblem();
	
	protected abstract void createObjectiveFunction() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException, SolverException;
	
	public abstract String getObjectiveFunctionToString();
	
	public AbstractSSBasicSimulation(ISteadyStateModel model){
		this.model = model;
		setIdToIndexVarMapings(new HashMap<String, Integer>());
		setIndexToIdVarMapings(new HashMap<Integer, String>());
		objTerms = new ArrayList<AbstractObjTerm>();
		propreties = new HashMap<String, Object>();
		initPropsKeys();
	}

	private void initPropsKeys() {
		mandatoryProps = new HashSet<String>();
		mandatoryProps.add(SimulationProperties.SOLVER);
		
		possibleProperties = new HashSet<String>();
		possibleProperties.add(SimulationProperties.IS_OVERUNDER_SIMULATION);
		possibleProperties.add(SimulationProperties.OVERUNDER_REFERENCE_FLUXES);
		possibleProperties.add(SimulationProperties.ENVIRONMENTAL_CONDITIONS);
		possibleProperties.add(SimulationProperties.GENETIC_CONDITIONS);		
	}

	public void putObjectiveFunctionIntoProblem() throws WrongFormulationException {
		
		Map<String, Integer> newVars = new HashMap<String, Integer>();
		for(AbstractObjTerm term : objTerms){
			Map<String, Integer> nv = term.addObjectiveTermToProblem(problem);
			if(nv!=null)
				newVars.putAll(nv);
		}
		putNewVariables(newVars);
		
	}
	
	public void putNewVariables(Map<String, Integer> vars){
		
		idToIndexVarMapings.putAll(vars);
		for(String id : vars.keySet()){
			int i = vars.get(id);
			indexToIdVarMapings.put(i, id);
		}
	}
	
	public Integer getCorrentNumOfVar(){
		return problem.getNumberVariables();
	}
	
	public void putVarMappings(String id, int idx){
		getIdToIndexVarMapings().put(id, idx);
		getIndexToIdVarMapings().put(idx, id);
	}
	
	public Integer getIdxVar(String id){
		return getIdToIndexVarMapings().get(id);
	}
	
	public String getIdVar(int idx){
		return getIndexToIdVarMapings().get(idx);
	}
	
	protected void createVariables() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException {
		int numberVariables = model.getNumberOfReactions();
		for(int i=0; i < numberVariables; i++)
		{
				
			
			Reaction r = model.getReaction(i);
			putVarMappings(r.getId(), i);
			
			ReactionConstraint rc = overrideRC.getReactionConstraint(i);
			
//			System.out.println(r.getId() + "\t" + r.getConstraints().getLowerLimit() + "\t" + r.getConstraints().getUpperLimit()+"\t" +
//					rc.getLowerLimit() + "\t" + rc.getUpperLimit());
			LPVariable var = new LPVariable(r.getId(),rc.getLowerLimit(), rc.getUpperLimit());
			problem.addVariable(var);
			
		}
		
	}
	
	protected void createConstrains() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException, SolverException{
		int numberVariables = model.getNumberOfReactions();
		int numberConstraints = model.getNumberOfMetabolites();
		
		for(int i=0; i < numberConstraints; i++)
		{
			LPProblemRow row = new LPProblemRow();
			for(int j=0; j < numberVariables; j++)
			{
				double value = model.getStoichiometricValue(i, j);
				if (value != 0) try {
                    row.addTerm(j, value);
                } catch (LinearProgrammingTermAlreadyPresentException e) {
                    throw new WrongFormulationException("Cannot add term " + j + "to row with value: " + value);
                }
            }
			
			LPConstraint constraint = 
				new LPConstraint(LPConstraintType.EQUALITY, row, 0.0);
			problem.addConstraint(constraint);
			
			
		}
	}
	
	protected void createProblemIfEmpty() throws MandatoryPropertyException, PropertyCastException, WrongFormulationException, SolverException{
		if(problem == null){
			problem = constructEmptyProblem();
			if(debug){
				times = new LinkedHashMap<String, Long>();
				long timeNow = System.currentTimeMillis();
				times.put("constructEmptyProblemProblem", timeNow-initType);
			}
			createModelOverride();
			if(debug){
				long timeNow = System.currentTimeMillis();
				times.put("createModelOverride", timeNow-initType);
			}
			createVariables();
			if(debug){
				long timeNow = System.currentTimeMillis();
				times.put("createVariables", timeNow-initType);
			}
			createConstrains();
			if(debug){
				long timeNow = System.currentTimeMillis();
				times.put("createConstrains", timeNow-initType);
			}
			createObjectiveFunction();
			if(debug){
				long timeNow = System.currentTimeMillis();
				times.put("createObjectiveFunction", timeNow-initType);
			}
			putObjectiveFunctionIntoProblem();
			if(debug){
				long timeNow = System.currentTimeMillis();
				times.put("putObjectiveFunctionIntoProblem", timeNow-initType);
			}
			
		}
	}
	
	protected void createModelOverride() throws MandatoryPropertyException, PropertyCastException, WrongFormulationException {
		
		boolean isOverUnder = false;
		
		try {
			isOverUnder = (Boolean) ManagerExceptionUtils.testCast(propreties, Boolean.class, SimulationProperties.IS_OVERUNDER_SIMULATION,
					false);
		} catch (PropertyCastException e) {
			System.err.println("The property " + SimulationProperties.IS_OVERUNDER_SIMULATION + " was ignored!!\n Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {
			isOverUnder = false;
		}
		
		EnvironmentalConditions environmentalConditions = getEnvironmentalConditions();
		GeneticConditions geneticConditions = getGeneticConditions();
	
		if (isOverUnder){
			FluxValueMap reference = (FluxValueMap) ManagerExceptionUtils.testCast(propreties
					, FluxValueMap.class, SimulationProperties.OVERUNDER_REFERENCE_FLUXES, false);
			
			if(geneticConditions == null) throw new WrongFormulationException(new NullPointerException("GeneticConditions"));
			overrideRC = new UnderOverSingleReference(model, environmentalConditions, geneticConditions, reference);
		}
		else{
			overrideRC = new OverrideSteadyStateModel(model,environmentalConditions, geneticConditions);
		}
				
	}
	
	public LPProblem getProblem() throws MandatoryPropertyException, PropertyCastException, WrongFormulationException, SolverException {
		createProblemIfEmpty();
		
		return problem;
	}
	
	public ISteadyStateModel getModel(){
		return model;
	}

	public Map<String, Integer> getIdToIndexVarMapings() {
		return idToIndexVarMapings;
	}

	public Map<Integer, String> getIndexToIdVarMapings() {
		return indexToIdVarMapings;
	}

	public boolean isProblemCreated() {
		return isProblemCreated;
	}

	public void setIdToIndexVarMapings(Map<String, Integer> idToIndexVarMapings) {
		this.idToIndexVarMapings = idToIndexVarMapings;
	}

	public void setIndexToIdVarMapings(Map<Integer, String> indexToIdVarMapings) {
		this.indexToIdVarMapings = indexToIdVarMapings;
	}
	
	public Integer getReactionVariableIndex(int reactionIdx){
		return getReactionVariableIndex(model.getReactionId(reactionIdx));
	}
	
	public Integer getReactionVariableIndex(String reactionId){
		return idToIndexVarMapings.get(reactionId);
		
	}
	
	public GeneticConditions getGeneticConditions() throws PropertyCastException, MandatoryPropertyException{
		return (GeneticConditions) ManagerExceptionUtils.testCast(propreties, GeneticConditions.class, SimulationProperties.GENETIC_CONDITIONS, true);
	}
	
	public EnvironmentalConditions getEnvironmentalConditions() throws PropertyCastException, MandatoryPropertyException{
		EnvironmentalConditions ec = ManagerExceptionUtils.testCast(propreties, EnvironmentalConditions.class, SimulationProperties.ENVIRONMENTAL_CONDITIONS, true); 
//		System.out.println("ENV COND: " + ec);
		return ec;
	}
	
	public void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions){
		setProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS, environmentalConditions);
	}
	
	public void setGeneticConditions(GeneticConditions geneticConditions){
		setProperty(SimulationProperties.GENETIC_CONDITIONS, geneticConditions);
	}
	
	public void setSolverType(SolverType solver){
		setProperty(SimulationProperties.SOLVER, solver);
	}
	
	public SolverType getSolverType() throws PropertyCastException, MandatoryPropertyException{
		SolverType solverType = (SolverType) ManagerExceptionUtils.testCast(propreties, SolverType.class,
				SimulationProperties.SOLVER, false);
		
		return solverType;
	}
	
	public void setProperty(String m, Object o){
		propreties.put(m, o);
	}
	
	protected LPSolution simulateProblem() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException {
		SolverType solverType = getSolverType();
		LPProblem p = getProblem();
		
		ILPSolver solver = solverType.lpSolver(p);
		
		
		LPSolution solution = solver.solve();
	

		return solution;
		
	}
	

	public SteadyStateSimulationResult simulate() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException{


		
		LPSolution solution = simulateProblem();
		
		if(debug){
			long timeNow = System.currentTimeMillis();
			times.put("Simulate", timeNow-initType);
		}

		
		SteadyStateSimulationResult result = convertLPSolutionToSimulationSolution(solution);
		
		if(debug){
			long timeNow = System.currentTimeMillis();
			times.put("solve", timeNow-initType);
			MapUtils.prettyPrint(times);
		}
		
		
		return result;
	}
	
	public SteadyStateSimulationResult convertLPSolutionToSimulationSolution(LPSolution solution) throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException {
		
		FluxValueMap fluxValues = getFluxValueListFromLPSolution(solution);	
		SteadyStateSimulationResult res = new SteadyStateSimulationResult(model, getMethod(), fluxValues);
		
		if(solution ==null){
			res.setSolutionType(LPSolutionType.ERROR);
			String solverout = "The solver " + getSolverType() + " cannot generate an output..." ;
			res.setSolverOutput(solverout);
			res.setOFvalue(Double.NaN);
		}else{
			putMetaboliteExtraInfo(solution,res);
			putReactionExtraInfo(solution,res);
			res.setSolutionType(solution.getSolutionType());
			res.setSolverOutput(solution.getSolverOutput());
			res.setOFvalue(solution.getOfValue());
		}
		res.setEnvironmentalConditions(getEnvironmentalConditions());
		res.setGeneticConditions(getGeneticConditions());
		res.setOFString(getObjectiveFunctionToString());
		
		
		return res;
	}
	
	protected void putReactionExtraInfo(LPSolution solution,
			SteadyStateSimulationResult res) {
		Map<String, MapStringNum> complementary = new HashMap<String, MapStringNum>();
		
		for(String id: solution.getVariableMetricsIds()){
			
			LPMapVariableValues cInfo =  solution.getPerVariableMetric(id);
			MapStringNum values = convertLPMapToMapString(model, cInfo, true);
			complementary.put(id, values);
		}
		
		res.setComplementaryInfoReactions(complementary);
		
	}

	protected void putMetaboliteExtraInfo(LPSolution solution,
			SteadyStateSimulationResult res) {
		Map<String, MapStringNum> complementaryInfoMetabolites = new HashMap<String, MapStringNum>();
		
		
		for(String id: solution.getConstraintMetricsIds()){
			
			LPMapVariableValues cInfo =  solution.getPerConstraintMetric(id);
			MapStringNum values = convertLPMapToMapString(model, cInfo, false);
			complementaryInfoMetabolites.put(id, values);
		}
		
		res.setComplementaryInfoMetabolites(complementaryInfoMetabolites);
	}
	
	protected MapStringNum convertLPMapToMapString(ISteadyStateModel model, LPMapVariableValues cInfo, Boolean isReactions){
		MapStringNum values = new MapStringNum();
		
//		Set<String> it = (isReactions)?model.getReactions().keySet(): model.getMetabolites().keySet();
		for(Integer idx : cInfo.keySet()){
			Double value = cInfo.get(idx);
			String id =(isReactions)?model.getReactionId(idx):model.getMetaboliteId(idx);
			
//			FIXME: resolve this problem, check if the information is consistent
			if(id != null)
			values.put(id, value);
		}
		
		return values;
		
	}

	public FluxValueMap getFluxValueListFromLPSolution(LPSolution solution){
		
		
		LPMapVariableValues varValueList = null;
		if(solution != null)
			varValueList = solution.getValues();
		
		FluxValueMap fluxValues = new FluxValueMap();
		for(String rId : model.getReactions().keySet()){
//			System.out.println(rId + idToIndexVarMapings);
			int idx = idToIndexVarMapings.get(rId);
//			System.out.println(rId + "\t " + idx);
			
			double value = Double.NaN;
			if(varValueList!=null)
				value = varValueList.get(idx);
			
			if( solution!=null && !(solution.getSolutionType().equals(LPSolutionType.OPTIMAL) 
					|| solution.getSolutionType().equals(LPSolutionType.FEASIBLE) 
					|| solution.getSolutionType().equals(LPSolutionType.UNKNOWN)))
				value = Double.NaN;
				
			fluxValues.put(rId, value);
		}
		return fluxValues;
	}
	
	public Set<String> getPossibleProperties(){
		return possibleProperties;		
	}
	
	public Set<String> getMandatoryProperties(){
		return mandatoryProps;
	}
	
	public void putAllProperties(Map<String, Object> p){
		this.propreties.putAll(p);		
	}

	public String getMethod(){
		return (String) propreties.get(SimulationProperties.METHOD_NAME);
	}
	
	public Class<?> getFormulationClass(){
		return problem.getClass();
	}
	
	public <T> T getProperty(String k) {
		return (T)propreties.get(k);
	}
}
