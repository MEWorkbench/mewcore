package pt.uminho.ceb.biosystems.mew.core.simulation.formulations;

import java.beans.PropertyChangeEvent;
import java.io.IOException;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSReferenceSimulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.L1VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.KeyPropertyChangeEvent;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.ListenerHashMap;

public class DSPP_LMOMA extends AbstractSSReferenceSimulation<LPProblem>{
	
	protected PFBA<FBA> internalPFBA = null;
	
	public DSPP_LMOMA(ISteadyStateModel model) {
		super(model);
	}

	protected void initRefProperties() {
		super.initRefProperties();
		mandatoryProperties.add(SimulationProperties.DSPP_FIRST_STAGE_ENV_COND);
	}
	
	public MapStringNum getWTReference() throws PropertyCastException, MandatoryPropertyException{
		
		if(internalPFBA==null){
			internalPFBA = new PFBA<>(model);
			internalPFBA.setSolverType(getSolverType());
			internalPFBA.setProperty(SimulationProperties.IS_MAXIMIZATION, true);
		}
		
		internalPFBA.setEnvironmentalConditions(getFirstStageEnvironmentalConditions());
		internalPFBA.setGeneticConditions(getGeneticConditions());
		
		try {
			wtReference = internalPFBA.simulate().getFluxValues();
//			MapUtils.prettyPrint(wtReference);
			setReference(wtReference);
		} catch (WrongFormulationException | SolverException | IOException e) {
			setReference(null);
			e.printStackTrace();
		}
		
		return wtReference;
	}

	private EnvironmentalConditions getFirstStageEnvironmentalConditions() throws PropertyCastException, MandatoryPropertyException {
		EnvironmentalConditions ec = ManagerExceptionUtils.testCast(properties, EnvironmentalConditions.class, SimulationProperties.DSPP_FIRST_STAGE_ENV_COND, true);
		MapUtils.prettyPrint(ec);
		return ec;
	}	

	@Override
	public LPProblem constructEmptyProblem() {
		return new LPProblem();
	}

	@Override
	protected void createObjectiveFunction() throws PropertyCastException, MandatoryPropertyException {
		problem.setObjectiveFunction(new LPProblemRow(), false);

		getWTReference();
		boolean useDrains = getUseDrainsInRef();
		for(String id: wtReference.keySet()){
			int idxVar = idToIndexVarMapings.get(id);
			double value = wtReference.get(id);
			
			if((useDrains || !model.getReaction(id).getType().equals(ReactionType.DRAIN))){
				objTerms.add(new L1VarTerm(idxVar,-value));
			}
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
//		
		KeyPropertyChangeEvent event = (KeyPropertyChangeEvent) evt;
		
		switch (event.getPropertyName()) {		
			case ListenerHashMap.PROP_UPDATE: {
								
				if (event.getKey().equals(SimulationProperties.GENETIC_CONDITIONS)) {
					resetReference = true;
					setRecreateOF(true);
				}
				
				if (event.getKey().equals(SimulationProperties.ENVIRONMENTAL_CONDITIONS)) {
					resetReference = true;
					setRecreateOF(true);
				}
				
				if (event.getKey().equals(SimulationProperties.DSPP_FIRST_STAGE_ENV_COND)) {
					resetReference = true;
					setRecreateOF(true);
				}
				
				break;
			}
			default:
				break;
		
		}
	}

	@Override
	public String getObjectiveFunctionToString() {
		return "Σ |v-wt|";
	}
}
