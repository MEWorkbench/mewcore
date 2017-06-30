package pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.LMOMA;

public class LMOMASimulationTest extends AbstractSimulationTest{
	
	boolean isMax = true;
	
	protected Map<String, Double> results = null;
	
	@Override
	protected void setParameters()
	{
		method = new LMOMA(super.model);

		// Mandatory properties:
		// Solver
		super.setParameters();
	}
	
	@Override
	protected String getMethodString() {
		return SimulationProperties.LMOMA;
	}
	
	@Override // Utilizado para o ControlCenter
	protected boolean isMaximization() {
		return isMax;
	}
		
	@Override
	protected Map<String, Double> getResults() 
	{	
		if(results == null){
			results= new HashMap<>();
			results.put(AbstractSimulationTest.WILDTYPE, 0.0);
			results.put(AbstractSimulationTest.KO_REACTIONS, 0.0);
			results.put(AbstractSimulationTest.KO_GENETICS, 195.10192);
			results.put(AbstractSimulationTest.UO_REACTIONS, -8.7712632E-7);
			results.put(AbstractSimulationTest.UO_GENETICS, 299.09969);
			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 109.97111);
			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 301.00911);
		}
		
		return results;
	}
	
	
	///////////////////////////////////////////////////////////
	////////////         Property Tests            ////////////
	///////////////////////////////////////////////////////////

	//================================================
	//				    WT_REFERENCE				   				   
	//================================================
	// A propriedade WT_REFERENCE é um FluxValueMap
	
	/** Teste para o caso da propriedade wtReference ser registada com NULL*/
//	@Test
	public void runWtReferenceNull() throws Exception
	{
		method.setProperty(SimulationProperties.WT_REFERENCE, null);
		// Sendo registada sem objecto esta propriedade não retorna exceção
		// No método getWTReference é executado o método SimulationProperties.simulateWT 
		// que devolve um FluxValueMap que é utilizado no registo da propriedade
		
		method.simulate();
	}
	
	/** Teste para o caso da propriedade wtReference ser  
	registada com um tipo de dados diferente do esperado*/
//	@Test
	public void runWtReferenceTypeMismach() throws Exception
	{
		// Example: 
		//Map<String, Double> wtRef = 
		// 		SimulationProperties.simulateWT(model, envCond, solverType);
		//method.setProperty(SimulationProperties.WT_REFERENCE, wtRef);
		
		method.setProperty(SimulationProperties.WT_REFERENCE, "Valor");
		// Não dá exception mas apresenta seguinte mensagem:
		// The property WT_REFERENCE was ignored!!
		// Reason: Type Mismach in Property WT_REFERENCE
		// Expected Class: class utilities.datastructures.map.MapStringNum
		// Introduced Class: class java.lang.String
		
		// Quando isto acontece o método getWTReference faz o registo da  
		// propriedade com os valores da SimulationProperties.simulateWT
		
		method.simulate();
	}


	//================================================
	//			 USE_DRAINS_IN_WT_REFERENCE				   				   
	//================================================
	// A propriedade USE_DRAINS_IN_WT_REFERENCE é um boolean
	
	/** Teste para o caso da propriedade USE_DRAINS_IN_WT_REFERENCE ser registada com NULL*/
//	@Test
	public void runUseDrainsInWtRefNull() throws Exception
	{
		method.setProperty(SimulationProperties.USE_DRAINS_IN_WT_REFERENCE, null);
		// Sendo registada sem objecto esta propriedade é ignorada 
		// sendo a exception controlada pelo método getUseDrainsInRef()
		
		method.simulate();
	}
	
	/** Teste para o caso da propriedade USE_DRAINS_IN_WT_REFERENCE   
	ser registada com um tipo de dados diferente do esperado*/
//	@Test
	public void runUseDrainsInWtRefTypeMismach() throws Exception
	{
		// Example: 
		//method.setProperty(SimulationProperties.USE_DRAINS_IN_WT_REFERENCE, true);

		method.setProperty(SimulationProperties.USE_DRAINS_IN_WT_REFERENCE, "boolean");

		// Não dá exception mas apresenta a seguinte mensagem:
		// The property Type Mismach in Property USE_DRAINS_IN_WT_REFERENCE
		// Expected Class: class java.lang.Boolean
		// Introduced Class: class java.lang.String was ignored!!
		// Reason: Type Mismach in Property USE_DRAINS_IN_WT_REFERENCE
		// Expected Class: class java.lang.Boolean
		// Introduced Class: class java.lang.String
		
		// Exception tratada pelo método getUseDrainsInRef e coloca false por default
		// Existe na classe AbstractSSReferenceSimulation um método chamado setUseDrainsInRef que não é utilizado?
		
		method.simulate();
	}

	@Override
	public String getSolver() {
		// TODO Auto-generated method stub
		return null;
	}

}
