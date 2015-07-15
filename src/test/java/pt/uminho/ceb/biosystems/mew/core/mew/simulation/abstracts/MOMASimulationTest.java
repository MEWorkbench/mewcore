package pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.MOMA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;

public abstract class MOMASimulationTest extends AbstractSimulationTest{
	
	boolean isMax = true;
	
	protected Map<String, Double> results = null;
	
	
	@Override
	protected void setParameters()
	{
		method = new MOMA(super.model);

		// Mandatory properties:
		// Solver
		super.setParameters();
	}

	@Override
	protected String getMethodString() {
		return SimulationProperties.MOMA;
	}
	
	@Override // Utilizado para o ControlCenter
	protected boolean isMaximization() {
		return isMax;
	}
	
	@Override
	protected Map<String, Double> getResults() {
		
		if(results == null){
			results= new HashMap<>();
			results.put(AbstractSimulationTest.WILDTYPE, 0.0);
			results.put(AbstractSimulationTest.KO_REACTIONS, 1.2682123222352963E-12);
			results.put(AbstractSimulationTest.KO_GENETICS, 1313.3072868804138);
			results.put(AbstractSimulationTest.UO_REACTIONS, 3.575866972729444E-12);
			results.put(AbstractSimulationTest.UO_GENETICS, 3384.9125429182277);
			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 930.6112736919978);
			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 1.6531434188480103E-13);
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
	//@Test
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
		
		method.simulate();
	}
	
//	@Test
	public void runMOMAWithReference() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException, IOException{
		
		System.out.println("runMOMAWithReference:");
		
		MapStringNum ref = new MapStringNum();
		ref.put("R_EX_succ_e", 5.0);
		ref.put("R_ADK1", 5.0);
		ref.put("R_GLUSy", 5.0);
		ref.put("R_GLUDy", 5.0);
		ref.put("R_FORt2", 5.0);
		ref.put("R_PDH", 5.0);
			
		
		method.setProperty(SimulationProperties.WT_REFERENCE, ref);
		
		System.out.println(method.simulate().getOFvalue());
		
	}

}
