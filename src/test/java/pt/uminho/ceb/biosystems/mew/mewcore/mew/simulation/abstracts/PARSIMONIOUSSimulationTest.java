package pt.uminho.ceb.biosystems.mew.mewcore.mew.simulation.abstracts;

import java.util.Map;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.PFBA;

public abstract class PARSIMONIOUSSimulationTest extends AbstractSimulationTest{
	
	boolean isMax = true;
	
	protected Map<String, Double> results = null;
	
	
	@Override
	protected void setParameters()
	{
		// Utiliza por defeito o FBA
		// method = new PARSIMONIOUS<FBA>(super.model);
		method = new PFBA(super.model);

		// Mandatory properties:
		// Solver
		// IS_MAXIMIZATION (Se utilizar FBA)
		super.setParameters();
		method.setProperty(SimulationProperties.IS_MAXIMIZATION, isMax);
	}

	@Override
	protected String getMethodString() {
		return SimulationProperties.PFBA;
	}
	
//	@Override
//	protected Map<String, Double> getResults() {
//		
//		if(results == null){
//			results= new HashMap<>();
//			results.put(AbstractSimulationTest.WILDTYPE, 518.41709);
//			results.put(AbstractSimulationTest.KO_REACTIONS, 420.48612);
//			results.put(AbstractSimulationTest.KO_GENETICS, 556.27316);
//			results.put(AbstractSimulationTest.UO_REACTIONS, 656.19532);
//			results.put(AbstractSimulationTest.UO_GENETICS, 576.99019);
//			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 496.29771);
//			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 68.003158);
//		}
//		
//		return results;
//	}

	@Override
	protected boolean isMaximization() {
		return isMax;
	}
	
	
	///////////////////////////////////////////////////////////
	////////////         Property Tests            ////////////
	///////////////////////////////////////////////////////////
	
	//================================================
	//				    RELAX_COEF				   				   
	//================================================
	// A propriedade RELAX_COEF é um double
	
	/** Teste para o caso da propriedade Relax_Coef ser registada com NULL*/
//	@Test
	public void runRelaxCoefNull() throws Exception
	{
		method.setProperty(SimulationProperties.RELAX_COEF, null);
		// Sendo registada sem objecto esta propriedade não dá exception
		// Ação controlada pelo método getRelaxCoef onde é utilizado por default o valor 0.99999
		
		method.simulate();
	}
	
	/** Teste para o caso da propriedade Relax_Coef ser  
	registada com um tipo de dados diferente do esperado*/
//	@Test
	public void runRelaxCoefTypeMismach() throws Exception
	{
		// Example: 
		//method.setProperty(SimulationProperties.RELAX_COEF, 0.99995);
		
		method.setProperty(SimulationProperties.RELAX_COEF, "Valor");
		// Não dá exception mas apresenta a seguinte mensagem: 
		// Property ignored reason: Type Mismach in Property RELAX_COEF
		// Expected Class: class java.lang.Double
		// Introduced Class: class java.lang.String
		
		// Exception tratada pelo método getRelaxCoef
		// Utilizado por default o valor 0.99999
		
		method.simulate();
	}	
	
	
	//================================================
	//				PARSIMONIUS_PROBLEM				   				   
	//================================================
	// A propriedade PARSIMONIUS_PROBLEM é um objeto AbstractSSBasicSimulation
	
	/** Teste para o caso da propriedade Parsimonius_Problem ser registada com NULL*/
//	@Test
	public void runParsimoniusProblemNull() throws Exception
	{
		method.setProperty(SimulationProperties.PARSIMONIOUS_PROBLEM, null);
		// Sendo registada sem objecto esta propriedade não dá exception
		// O registo é controlado pelo método getInitProblem que regista 
		// por default um problema FBA
		
		method.simulate();
	}
	
	/** Teste para o caso da propriedade Parsimonius_Problem ser  
	registada com um tipo de dados diferente do esperado*/
//	@Test
	public void runParsimoniusProblemTypeMismach() throws Exception
	{		
		// Example: 
		//FBA initProblem = new FBA(model);
		//initProblem.setProperty(SimulationProperties.SOLVER, SolverType.CLP);
		//initProblem.setProperty(SimulationProperties.IS_MAXIMIZATION, true);
		//method.setProperty(SimulationProperties.PARSIMONIUS_PROBLEM, initProblem);
		
		method.setProperty(SimulationProperties.PARSIMONIOUS_PROBLEM, "Valor");
		// Não dá exception mas apresenta a seguinte mensagem:
		// Property ignored reason: Type Mismach in Property PARSIMONIUS_PROBLEM
		// Expected Class: class metabolic.simulation.formulations.abstractions.AbstractSSBasicSimulation
		// Introduced Class: class java.lang.String
		
		// Exception tratada pelo método getInitProblem
		// Utiliza por default um problema FBA
		
		method.simulate();
	}
	
	
	//================================================
	//			 PARSIMONIUS_OBJECTIVE_VALUE				   				   
	//================================================
	// A propriedade PARSIMONIUS_OBJECTIVE_VALUE é um double
	
	/** Teste para o caso da propriedade Parsimonius_Objective Value ser registada com NULL*/
//	@Test
	public void runParsimoniusObjValNull() throws Exception
	{
		method.setProperty(SimulationProperties.PARSIMONIOUS_OBJECTIVE_VALUE, null);
		// Sendo registada sem objecto esta propriedade não dá exception
		// Registo controlado pelo método getObjectiveValue que regista por
		// default o valor da função objetivo do resultado da simulação
		// definida na propriedade PARSIMONIUS_PROBLEM ou FBA por default
		
		method.simulate();
	}
	
	/** Teste para o caso da propriedade Parsimonius_Objective Value ser  
	registada com um tipo de dados diferente do esperado*/
//	@Test
	public void runParsimoniusObjValTypeMismach() throws Exception
	{
		// Example: 
		//FBA initProblem = new FBA(model);
		//initProblem.setProperty(SimulationProperties.SOLVER, SolverType.CLP);
		//initProblem.setProperty(SimulationProperties.IS_MAXIMIZATION, true);
		//method.setProperty(SimulationProperties.PARSIMONIUS_OBJECTIVE_VALUE, initProblem.simulate().getOFvalue());
		
		method.setProperty(SimulationProperties.PARSIMONIOUS_OBJECTIVE_VALUE, "Valor");
		// Não dá exception mas apresenta a seguinte mensagem:
		// Property ignored reason: Type Mismach in Property PARSIMONIUS_OBJECTIVE_VALUE
		// Expected Class: class java.lang.Double
		// Introduced Class: class java.lang.String
		
		// Exception tratada pelo método getObjectiveValue
		// Utiliza por default o valor da função objetivo do resultado da simulação
		// definida na propriedade PARSIMONIUS_PROBLEM ou FBA por default
		
		method.simulate();
	}

}
