package pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry.BalanceValidator;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.configuration.ModelConfiguration;
import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.utils.Debugger;

public class CarbonYieldObjectiveFunction implements IObjectiveFunction {
	
	private static final long		serialVersionUID		= -7344676414875267981L;
	protected static final String	CARBON					= "C";
	
	protected String				_substrateID			= null;
	protected String				_targetID				= null;
	protected String				_configurationFile		= null;
	protected int					_carbonContentSubstrate	= -1;
	protected int					_carbonContentTarget	= -1;
	protected Container				_container				= null;
	
	public CarbonYieldObjectiveFunction(String substrateID, String targetID, String modelConfigFile) throws Exception {
		
		_substrateID = substrateID;
		_targetID = targetID;
		_configurationFile = modelConfigFile;
		ModelConfiguration modelConfiguration = new ModelConfiguration(modelConfigFile);
		_container = modelConfiguration.getContainer();
		if (!_container.metabolitesHasFormula())
			throw new Exception(getClass().getCanonicalName() + ": model [" + modelConfiguration.getModelFile() + "] do not contain metabolite formulas. Formulas are required to calculate the carbon content of the metabolites.");
	}
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		if (_carbonContentSubstrate <= 0 || _carbonContentTarget <= 0) try {
			init(simResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double fit = computeCYield(simResult, _targetID);
		
		return fit;
	}
	
	protected double computeCYield(SteadyStateSimulationResult simResult, String target) {
		double targ = Math.abs(simResult.getFluxValues().getValue(_targetID) * ((double) getCarbonContentTarget(target)));
		double subs = Math.abs(simResult.getFluxValues().getValue(_substrateID) * ((double) _carbonContentSubstrate));
		double fit = (subs <= 0.0) ? getWorstFitness() : (targ / subs);
		
		Debugger.debug("Sub [" + _substrateID + "] C=" + _carbonContentSubstrate + " / Targ [" + _targetID + "] C=" + _carbonContentTarget + " / Fit = " + fit);
		return fit;
	}
	
	protected int getCarbonContentTarget(String target) {
		return _carbonContentTarget;
	}
	
	protected void init(SteadyStateSimulationResult simResult) throws Exception {
		ISteadyStateModel model = simResult.getModel();
		int substrateDrainIndex = model.getReactionIndex(_substrateID);
		int targetDrainIndex = model.getReactionIndex(_targetID);
		int substrateIndex = model.getMetaboliteFromDrainIndex(substrateDrainIndex);
		int targetIndex = model.getMetaboliteFromDrainIndex(targetDrainIndex);
		BalanceValidator validator = new BalanceValidator(_container);
		validator.setFormulasFromContainer();
		_carbonContentSubstrate = validator.getformula(model.getMetaboliteId(substrateIndex)).getValue(CARBON);
		_carbonContentTarget = validator.getformula(model.getMetaboliteId(targetIndex)).getValue(CARBON);
		if (_carbonContentSubstrate <= 0) throw new Exception("[" + _substrateID + "] has carbon content of zero (0). Please verify formula in the model.");
		if (_carbonContentTarget <= 0) throw new Exception("[" + _targetID + "] has carbon content of zero (0). Please verify formula in the model.");
		
		_container = null;
		//		System.out.println("Sub ["+_substrateID+"] C="+_carbonContentSubstrate+" / Targ ["+_targetID+"] C="+_carbonContentTarget);
	}
	
	@Override
	public double getWorstFitness() {
		return 0d;
	}
	
	@Override
	public ObjectiveFunctionType getType() {
		return ObjectiveFunctionType.CYIELD;
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
		return "CYIELD";
	}
	
	@Override
	public String getLatexString() {
		return getShortString();
	}
	
	@Override
	public String getLatexFormula() {
		return null;
	}
	
	@Override
	public String getBuilderString() {
		return getType() + "(" + _substrateID + "," + _targetID + "," + _configurationFile + ")";
	}
	
}