package pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.ROOM;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;

public abstract class ROOMSimulationTest extends AbstractSimulationTest{

	boolean isMax = true;
	
	protected Map<String, Double> results = null;
	
	@Override
	protected void setParameters()
	{
		method = new ROOM(super.model);

		// Mandatory properties:
		// Solver
		super.setParameters();
	}

	@Override
	protected String getMethodString() {
		return SimulationProperties.ROOM;
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
			results.put(AbstractSimulationTest.KO_REACTIONS, -8.8079797E-14);
			results.put(AbstractSimulationTest.KO_GENETICS, 2.353547);
			results.put(AbstractSimulationTest.UO_REACTIONS, -4.3742698E-5);
			results.put(AbstractSimulationTest.UO_GENETICS, 1.5511579);
			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 1.1384144);
			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 15.96104);
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
		
		// Duvida: na classe AbstractSSReferenceSimulation existe um método setReference que não é utilizado
		
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
	
	
	//================================================
	//			 		ROOM_DELTA				   				   
	//================================================
	// A propriedade ROOM_DELTA é um double
	
	/** Teste para o caso da propriedade ROOM_DELTA ser registada com NULL*/
//	@Test
	public void runRoomDeltaNull() throws Exception
	{
		method.setProperty(SimulationProperties.ROOM_DELTA, null);
		// Sendo registada sem objecto esta propriedade é ignorada e a exceção 
		// controlada pelo método getDelta onde é utilizado por default o valor 0.03
		
		method.simulate();
	}
	
	/** Teste para o caso da propriedade ROOM_DELTA ser     
	registada com um tipo de dados diferente do esperado*/
//	@Test
	public void runRoomDeltaTypeMismach() throws Exception
	{
		// Example: 
		//method.setProperty(SimulationProperties.ROOM_DELTA, 0.03);
		
		method.setProperty(SimulationProperties.ROOM_DELTA, "true");
		
		// Não dá exception mas apresenta a seguinte mensagem:
		// The property Type Mismach in Property ROOM_DELTA
		// Expected Class: class java.lang.Double
		// Introduced Class: class java.lang.String was ignored!!
		// Reason: Type Mismach in Property ROOM_DELTA
		// Expected Class: class java.lang.Double
		// Introduced Class: class java.lang.String
		
		// Exceção é tratada pelo método getDelta e atribui 0.03 por default
		
		method.simulate();
	}	
	
	
	//================================================
	//			 		ROOM_EPSILON				   				   
	//================================================
	// A propriedade ROOM_EPSILON é um double
	
	/** Teste para o caso da propriedade ROOM_EPSILON ser registada com NULL*/
//	@Test
	public void runRoomEpsilonNull() throws Exception
	{
		method.setProperty(SimulationProperties.ROOM_EPSILON, null);
		// Sendo registada sem objecto esta propriedade é ignorada e a exceção 
		// controlada pelo método getEpsilon onde é utilizado por default o valor 0.001
		
		method.simulate();
	}
	
	/** Teste para o caso da propriedade ROOM_EPSILON ser     
	registada com um tipo de dados diferente do esperado*/
	//@Test
	public void runRoomEpsilonTypeMismach() throws Exception
	{
		// Example: 
		//method.setProperty(SimulationProperties.ROOM_EPSILON, 0.001);
		
		method.setProperty(SimulationProperties.ROOM_EPSILON, "true");
		
		// Não dá exception mas apresenta a seguinte mensagem:
		// The property Type Mismach in Property ROOM_EPSILON
		// Expected Class: class java.lang.Double
		// Introduced Class: class java.lang.String was ignored!!
		// Reason: Type Mismach in Property ROOM_EPSILON
		// Expected Class: class java.lang.Double
		// Introduced Class: class java.lang.String
		
		// Exceção é tratada pelo método getEpsilon onde valor por default é 0.001
		
		method.simulate();
	}	
	
//	@Test
	public void runRoomWithReference() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException, IOException
	{
		System.out.println("runRoomWithReference:");
		
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
