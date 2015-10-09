package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry.BalanceValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry.MetaboliteFormula;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.AbstractObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ObjectiveFunctionParameterType;
import pt.uminho.ceb.biosystems.mew.core.utils.Debugger;

public class CYIELDObjectiveFunction extends AbstractObjectiveFunction {
	
	private static final long		serialVersionUID	= -7344676414875267981L;
	public static final String		ID					= "CYIELD";
	protected static final String	CARBON				= "C";
	
	public static final String	CYIELD_PARAM_SUBSTRATE	= "Substrate";
	public static final String	CYIELD_PARAM_PRODUCT	= "Product";
	public static final String	CYIELD_PARAM_CONTAINER	= "Container";
	
	protected int	_carbonContentSubstrate	= -1;
	protected int	_carbonContentTarget	= -1;
	
	public Map<String, ObjectiveFunctionParameterType> loadParameters(){
		HashMap<String, ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(CYIELD_PARAM_SUBSTRATE, ObjectiveFunctionParameterType.REACTION_SUBSTRATE);
		myparams.put(CYIELD_PARAM_PRODUCT, ObjectiveFunctionParameterType.REACTION_PRODUCT);
		myparams.put(CYIELD_PARAM_CONTAINER, ObjectiveFunctionParameterType.CONTAINER);
		return Collections.unmodifiableMap(myparams);
	}
	
	public CYIELDObjectiveFunction(){super();}
	
	public CYIELDObjectiveFunction(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration {
		super(configuration);
	}
	
	public CYIELDObjectiveFunction(String substrateID, String productID, Container container) throws Exception {
		super(substrateID, productID, container);
	}
	
	@Override
	protected void processParams(Object... params) {
		setParameterValue(CYIELD_PARAM_SUBSTRATE, params[0]);
		setParameterValue(CYIELD_PARAM_PRODUCT, params[1]);
		setParameterValue(CYIELD_PARAM_CONTAINER, params[2]);	
	}
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		
		if (_carbonContentSubstrate <= 0 || _carbonContentTarget <= 0) try {
			init(simResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double fit = computeCYield(simResult);
		
		return fit;
	}
	
	protected double computeCYield(SteadyStateSimulationResult simResult) {
		String productID = (String) getParameterValue(CYIELD_PARAM_PRODUCT);
		String substrateID = (String) getParameterValue(CYIELD_PARAM_SUBSTRATE);
		double targ = Math.abs(simResult.getFluxValues().getValue(productID) * ((double) _carbonContentTarget));
		double subs = Math.abs(simResult.getFluxValues().getValue(substrateID) * ((double) _carbonContentSubstrate));
		double fit = (subs <= 0.0) ? getWorstFitness() : (targ / subs);
		
		Debugger.debug("Sub [" + substrateID + "] C=" + _carbonContentSubstrate + " / Targ [" + productID + "] C=" + _carbonContentTarget + " / Fit = " + fit);
		return fit;
	}
	
	protected void init(SteadyStateSimulationResult simResult) throws Exception {
		String productID = (String) getParameterValue(CYIELD_PARAM_PRODUCT);
		String substrateID = (String) getParameterValue(CYIELD_PARAM_SUBSTRATE);
		Container container = (Container) getParameterValue(CYIELD_PARAM_CONTAINER);
		
		if (!container.metabolitesHasFormula())
			throw new Exception(getClass().getCanonicalName() + ": the provided container ["+container.getModelName()+"] is unable to provide metabolite formulas. Metabolite formulas are required to calculate the carbon content of the metabolites.");
			
		ISteadyStateModel model = simResult.getModel();
		int substrateDrainIndex = model.getReactionIndex(substrateID);
		int targetDrainIndex = model.getReactionIndex(productID);
		int substrateIndex = model.getMetaboliteFromDrainIndex(substrateDrainIndex);
		int targetIndex = model.getMetaboliteFromDrainIndex(targetDrainIndex);
		BalanceValidator validator = new BalanceValidator(container);
		validator.setFormulasFromContainer();
		String metSubstrate = model.getMetaboliteId(substrateIndex);
		String metTarget = model.getMetaboliteId(targetIndex);
		System.out.println("Sub [" + substrateID + "/" + substrateDrainIndex + "/" + substrateIndex + "/" + metSubstrate + "]");
		System.out.println("Targ [" + productID + "/" + targetDrainIndex + "/" + targetIndex + "/" + metTarget + "]");
		MetaboliteFormula metFormulaSubstrate = validator.getformula(metSubstrate);
		MetaboliteFormula metFormulaTarget = validator.getformula(metTarget);
		System.out.println(metFormulaSubstrate.getOriginalFormula());
		System.out.println(metFormulaTarget.getOriginalFormula());
		_carbonContentSubstrate = metFormulaSubstrate.getValue(CARBON);
		_carbonContentTarget = metFormulaTarget.getValue(CARBON);
		if (_carbonContentSubstrate <= 0) throw new Exception("[" + substrateID + "] has carbon content of zero (0). Please verify formula in the model.");
		if (_carbonContentTarget <= 0) throw new Exception("[" + productID + "] has carbon content of zero (0). Please verify formula in the model.");
		
	}
	
	@Override
	public double getWorstFitness() {
		return 0d;
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
		return getID();
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
		return getID() + "(" + getParameterValue(CYIELD_PARAM_SUBSTRATE)+ "," + getParameterValue(CYIELD_PARAM_PRODUCT)+ "," + ((Container)getParameterValue(CYIELD_PARAM_CONTAINER)).getModelName() + ")";
	}
	
	@Override
	public String getID() {
		return ID;
	}
	
	
	
}
