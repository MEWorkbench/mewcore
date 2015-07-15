package pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts;

import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.FBA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;

public abstract class FBASimulationTest extends AbstractSimulationTest{
		
	boolean isMax = true;
	String objFunc = "R_Biomass_Ecoli_core_w_GAM";
	
	protected Map<String, Double> results = null;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	
	@Override
	protected void setParameters()
	{			
		HashMap<String, Double> obj_coef = new HashMap<String, Double>();
		obj_coef.put(objFunc, 1.0);
		
		method = new FBA(super.model);

		// Mandatory properties:
		// Solver
		// IsMaximization
		method.setProperty(SimulationProperties.IS_MAXIMIZATION, isMax);
		//method.setProperty(SimulationProperties.OBJECTIVE_FUNCTION, obj_coef);
		
		super.setParameters();
	}
	
	@Override
	protected String getMethodString() {
		return SimulationProperties.FBA;
	}
	
	@Override // Utilizado para o ControlCenter
	protected boolean isMaximization() {
		return isMax;
	}
	
//	@Override
//	protected Map<String, Double> getResults() {
//		
//		//CLP
//		if(results == null){
//			results= new HashMap<>();
//			results.put(AbstractSimulationTest.WILDTYPE, 0.87392151);
//			results.put(AbstractSimulationTest.KO_REACTIONS, 0.33887138);
//			results.put(AbstractSimulationTest.KO_GENETICS, 0.78235105);
//			results.put(AbstractSimulationTest.UO_REACTIONS, 0.5162538);
//			results.put(AbstractSimulationTest.UO_GENETICS, 0.40940924);
//			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 0.36439464);
//			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 0.0);
//		}
//		
//		//GLPK
//		if(results == null){
//			results= new HashMap<>();
//			results.put(AbstractSimulationTest.WILDTYPE, 0.87392151);
//			results.put(AbstractSimulationTest.KO_REACTIONS, 0.33887138);
//			results.put(AbstractSimulationTest.KO_GENETICS, 0.78235105);
//			results.put(AbstractSimulationTest.UO_REACTIONS, 0.5162538);
//			results.put(AbstractSimulationTest.UO_GENETICS, 0.40940924);
//			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 0.36439464);
//			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 0.0);
//		}
//		
//		return results;
//	}
	
	
	///////////////////////////////////////////////////////////
	////////////         Property Tests            ////////////
	///////////////////////////////////////////////////////////
	
	//================================================
	//				  IS_MAXIMIZATION
	//================================================
	// IS_MAXIMIZATION Boolean Type
	
	/** Teste para o caso da propriedade isMaximization ser registada com NULL*/
//	@Test
	public void runIsMaximizationMandatoryPropertyException() throws Exception
	{		
		thrown.expect(MandatoryPropertyException.class);
		
		method.setProperty(SimulationProperties.IS_MAXIMIZATION, null);
		// É dada exception e apresentada a seguinte mensagem:
		// isMaximization class java.lang.Boolean
		
		method.simulate();
	}
	
	/** Teste para o caso da propriedade isMaximization ser 
	registada com um tipo diferente do esperado*/
//	@Test
	public void runIsMaximizationPropertyCastException() throws Exception
	{
		thrown.expect(PropertyCastException.class);
		
		// Example: 
		//method.setProperty(SimulationProperties.IS_MAXIMIZATION, true);
				
		method.setProperty(SimulationProperties.IS_MAXIMIZATION, "Valor");
		// É dada exception e apresentada a seguinte mensagem:
		// Type Mismach in Property isMaximization
		// Expected Class: class java.lang.Boolean
		// Introduced Class: class java.lang.String
		
		method.simulate();
	}


	//================================================
	//				 OBJECTIVE_FUNCTION
	//================================================
	// A propriedade OBJECTIVE_FUNCTION é um HashMap de String e Double
	
	/** Teste para o caso da propriedade objectiveFunction ser registada com NULL*/
//	@Test
	public void runObjectiveFunctionNull() throws Exception
	{
		System.setProperty("PATH", "../dependencies/unix64/bin/");
		
		method.setProperty(SimulationProperties.OBJECTIVE_FUNCTION, null);
		// Não dá exception e no método getObjectiveFunction é utilizada a Biomassa por default
		
		method.simulate();
	}
	
	/** Teste para o caso da propriedade objectiveFunction  
	ser registada com um tipo diferente do esperado*/
//	@Test
	public void runObjectiveFunctionTypeMismach() throws Exception
	{
		// Example: 
		//HashMap<String, Double> obj_coef = new HashMap<String, Double>();
		//obj_coef.put(model.getBiomassFlux(), 1.0);
		//method.setProperty(SimulationProperties.OBJECTIVE_FUNCTION, obj_coef);
		
		method.setProperty(SimulationProperties.OBJECTIVE_FUNCTION, "Valor");		
		// Não dá exception e no método getObjectiveFunction é utilizada a Biomassa por default
		
		method.simulate();
	}


}
