package pt.uminho.ceb.biosystems.mew.core.mew.simulation.formulations;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.LMOMA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.LPVariabilityProblem;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractObjTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.builders.CPLEX3SolverBuilder;
import pt.uminho.ceb.biosystems.mew.solvers.lp.CplexParamConfiguration;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;

public class LPVariabilityProblemTest {
	
	public static ISteadyStateModel			_model			= null;
	public static EnvironmentalConditions	_envConditions	= null;
	public static GeneticConditions			_genConditions	= null;
	public static String					_targetID		= null;
	public static String					_biomassID		= null;
															
	@BeforeClass
	public static void init() throws Exception {
		
		String _modelConfFile = LPVariabilityProblemTest.class.getClassLoader().getResource("mfafiles/ecoli_core.conf").getPath();
		String _envConditionsFile1 = LPVariabilityProblemTest.class.getClassLoader().getResource("mfafiles/ecEnvConditions1.env").getPath();
		
		AbstractObjTerm.setMaxValue(Double.MAX_VALUE);
		AbstractObjTerm.setMinValue(-Double.MAX_VALUE);
		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);
		CplexParamConfiguration.setIntegerParam("MIPEmphasis", 2);
		CplexParamConfiguration.setBooleanParam("NumericalEmphasis", true);
		CplexParamConfiguration.setBooleanParam("PreInd", true);
		CplexParamConfiguration.setIntegerParam("HeurFreq", -1);
		
		/**
		 * MODEL
		 */
//		ModelConfiguration modelConf = new ModelConfiguration(_modelConfFile);
		JSBMLReader reader = new JSBMLReader("./src/test/resources/models/ecoli_core_model.xml", "1", false);
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		ISteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);
		_model = model;
		_biomassID = model.getBiomassFlux();
		
		/**
		 * ENVIRONMENTAL CONDITIONS
		 */
		_envConditions = EnvironmentalConditions.readFromFile(_envConditionsFile1, ",");
		
		/**
		 * GENETIC CONDITIONS
		 */
		_genConditions = new GeneticConditions(new ReactionChangesList(new String[] { "R_PTAr", "R_ALCD2x" }));
		
		/**
		 * TARGET
		 */
		_targetID = "R_EX_lac_D_e";
	}
	
//	@Test
	public void testLMOMARobustness() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException, IOException{
		
		LMOMA lmoma = new LMOMA(_model);
		lmoma.setEnvironmentalConditions(_envConditions);
		lmoma.setGeneticConditions(_genConditions);
		lmoma.setSolverType(CPLEX3SolverBuilder.ID);
				
		SteadyStateSimulationResult res = lmoma.simulate();
		double objectiveLMOMA = res.getOFvalue();
		System.out.println("biomass = "+res.getFluxValues().get(_biomassID));
		System.out.println("target= "+res.getFluxValues().get(_targetID));
		System.out.println(res.getOFString() +"="+res.getOFvalue());
		
		LPVariabilityProblem<LMOMA> lpVariability = new LPVariabilityProblem<>(_model);
		lpVariability.setInitProblem(lmoma);
		Map<String,Double> objFunction = new HashMap<String,Double>();
		objFunction.put(_targetID, 1.0);
		lpVariability.setObjectiveFunction(objFunction);
		lpVariability.setIsMaximization(false);
		lpVariability.setObjectiveValue(objectiveLMOMA);
		lpVariability.setEnvironmentalConditions(_envConditions);
		lpVariability.setGeneticConditions(_genConditions);
		lpVariability.setSolverType(CPLEX3SolverBuilder.ID);
		lpVariability.setProperty(SimulationProperties.RELAX_COEF, 0.9999);
		
		SteadyStateSimulationResult res2 = lpVariability.simulate();
		System.out.println("biomass = "+res2.getFluxValues().get(_biomassID));
		System.out.println("target= "+res2.getFluxValues().get(_targetID));
		System.out.println(res2.getOFString() +"="+res2.getOFvalue());
						
	}
	
//	@Test
	public void testRobustnessPoint() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException, IOException{
		
		LMOMA lmoma = new LMOMA(_model);
		lmoma.setEnvironmentalConditions(_envConditions);
		lmoma.setGeneticConditions(_genConditions);
		lmoma.setSolverType(CPLEX3SolverBuilder.ID);
				
		SteadyStateSimulationResult res = lmoma.simulate();
		double objectiveLMOMA = res.getOFvalue();
		System.out.println("biomass = "+res.getFluxValues().get(_biomassID));
		System.out.println("target= "+res.getFluxValues().get(_targetID));
		System.out.println(res.getOFString() +"="+res.getOFvalue());
		
		int numSteps = 200;
		int currStep = 0;
		
		double rangeMax= 1.0;
		double rangeMin = 0.0;
		double minPercent = 0.0000001;
		boolean isMaximization = false;
		
		LPVariabilityProblem<LMOMA> lpVariabilityOriginal = new LPVariabilityProblem<>(_model);
		lpVariabilityOriginal.setInitProblem(lmoma);
		Map<String,Double> objFunction = new HashMap<String,Double>();
		objFunction.put(_targetID, 1.0);
		lpVariabilityOriginal.setObjectiveFunction(objFunction);
		lpVariabilityOriginal.setIsMaximization(isMaximization);
		lpVariabilityOriginal.setObjectiveValue(objectiveLMOMA);
		lpVariabilityOriginal.setEnvironmentalConditions(_envConditions);
		lpVariabilityOriginal.setGeneticConditions(_genConditions);
		lpVariabilityOriginal.setSolverType(CPLEX3SolverBuilder.ID);
		lpVariabilityOriginal.setProperty(SimulationProperties.RELAX_COEF, 0.99999999);
		SteadyStateSimulationResult resOriginal = lpVariabilityOriginal.simulate();
		double original = resOriginal.getFluxValues().get(_targetID);
		Double current = null; 
		
		while(currStep < numSteps){					
			
			double nextTestPoint = ((rangeMax - rangeMin) / 2) + rangeMin;
			
			LPVariabilityProblem<LMOMA> lpVariabilityCurrent = new LPVariabilityProblem<>(_model);
			lpVariabilityCurrent.setInitProblem(lmoma);
			lpVariabilityCurrent.setObjectiveFunction(objFunction);
			lpVariabilityCurrent.setIsMaximization(false);
			lpVariabilityCurrent.setObjectiveValue(objectiveLMOMA);
			lpVariabilityCurrent.setEnvironmentalConditions(_envConditions);
			lpVariabilityCurrent.setGeneticConditions(_genConditions);
			lpVariabilityCurrent.setSolverType(CPLEX3SolverBuilder.ID);
			lpVariabilityCurrent.setProperty(SimulationProperties.RELAX_COEF, nextTestPoint);
			SteadyStateSimulationResult resCurrent = lpVariabilityCurrent.simulate();
			current = resCurrent.getFluxValues().get(_targetID);

			boolean robust = isRobust(original, current, minPercent, isMaximization);
			System.out.println("Step ["+currStep+"]: relax ="+nextTestPoint +" / target = "+current+" / robust = "+robust + " / range = ["+rangeMin+", "+rangeMax+"]");
					
			if(robust){
				rangeMax = nextTestPoint;
			}else{
				rangeMin = nextTestPoint;
			}
			
			currStep++;
		}
		
	}
	
	@Test
	public void testEnvelopeCriticalPoints() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException, IOException{
		
		LMOMA lmoma = new LMOMA(_model);
		lmoma.setEnvironmentalConditions(_envConditions);
		lmoma.setGeneticConditions(_genConditions);
		lmoma.setSolverType(CPLEX3SolverBuilder.ID);
				
		SteadyStateSimulationResult res = lmoma.simulate();
		double objectiveLMOMA = res.getOFvalue();
		System.out.println("biomass = "+res.getFluxValues().get(_biomassID));
		System.out.println("target= "+res.getFluxValues().get(_targetID));
		System.out.println(res.getOFString() +"="+res.getOFvalue());
		
		int numSteps = 15;
		int currStep = 0;
		
		double rangeMax= 1.0;
		double rangeMin = 0.0;
		double minPercent = 0.5;
		boolean isMaximization = false;
		
		LPVariabilityProblem<LMOMA> lpVariabilityOriginalMin = new LPVariabilityProblem<>(_model);
		lpVariabilityOriginalMin.setInitProblem(lmoma);
		Map<String,Double> objFunction = new HashMap<String,Double>();
		objFunction.put(_targetID, 1.0);
		lpVariabilityOriginalMin.setObjectiveFunction(objFunction);
		lpVariabilityOriginalMin.setIsMaximization(false);
		lpVariabilityOriginalMin.setObjectiveValue(objectiveLMOMA);
		lpVariabilityOriginalMin.setEnvironmentalConditions(_envConditions);
		lpVariabilityOriginalMin.setGeneticConditions(_genConditions);
		lpVariabilityOriginalMin.setSolverType(CPLEX3SolverBuilder.ID);
		lpVariabilityOriginalMin.setProperty(SimulationProperties.RELAX_COEF, 0.99999999);
		SteadyStateSimulationResult resOriginalMin = lpVariabilityOriginalMin.simulate();
		double originalFVAMin = resOriginalMin.getFluxValues().get(_targetID);
		
		
		LPVariabilityProblem<LMOMA> lpVariabilityOriginalMax = new LPVariabilityProblem<>(_model);
		lpVariabilityOriginalMax.setInitProblem(lmoma);
		lpVariabilityOriginalMax.setObjectiveFunction(objFunction);
		lpVariabilityOriginalMax.setIsMaximization(true);
		lpVariabilityOriginalMax.setObjectiveValue(objectiveLMOMA);
		lpVariabilityOriginalMax.setEnvironmentalConditions(_envConditions);
		lpVariabilityOriginalMax.setGeneticConditions(_genConditions);
		lpVariabilityOriginalMax.setSolverType(CPLEX3SolverBuilder.ID);
		lpVariabilityOriginalMax.setProperty(SimulationProperties.RELAX_COEF, 0.99999999);
		SteadyStateSimulationResult resOriginalMax = lpVariabilityOriginalMax.simulate();		
		double originalFVAMax = resOriginalMax.getFluxValues().get(_targetID);

		
		double[] originalArray = new double[4];
		originalArray[0] = resOriginalMin.getOFvalue();
		originalArray[1] = resOriginalMax.getOFvalue();
		originalArray[2] = originalFVAMin;
		originalArray[3] = originalFVAMax;
		
		System.out.println(Arrays.toString(originalArray));
		
		Double current = null; 
		
		double[] resArray = new double[4];
		resArray[0] = objectiveLMOMA;
		resArray[1] = originalFVAMax;
		while(currStep < numSteps){					
			
			double nextTestRelax = ((rangeMax - rangeMin) / 2) + rangeMin;
			
			LPVariabilityProblem<LMOMA> lpVariabilityCurrent = new LPVariabilityProblem<>(_model);
			lpVariabilityCurrent.setInitProblem(lmoma);
			lpVariabilityCurrent.setObjectiveFunction(objFunction);
			lpVariabilityCurrent.setIsMaximization(false);
			lpVariabilityCurrent.setObjectiveValue(objectiveLMOMA);
			lpVariabilityCurrent.setEnvironmentalConditions(_envConditions);
			lpVariabilityCurrent.setGeneticConditions(_genConditions);
			lpVariabilityCurrent.setSolverType(CPLEX3SolverBuilder.ID);
			lpVariabilityCurrent.setProperty(SimulationProperties.RELAX_COEF, nextTestRelax);
			SteadyStateSimulationResult resCurrent = lpVariabilityCurrent.simulate();
			current = resCurrent.getOFvalue();

			boolean robust = isRobust(originalFVAMin, current, minPercent, isMaximization);
			System.out.println("Step ["+currStep+"]: relax ="+nextTestRelax +" / target = "+current+" / robust = "+robust + " / range = ["+rangeMin+", "+rangeMax+"]");
			
			resArray[2] = objectiveLMOMA*nextTestRelax;
					
			if(robust){
				rangeMax = nextTestRelax;
			}else{
				rangeMin = nextTestRelax;
			}
			
			currStep++;
		}
		
		resArray[3] = (resArray[1]) / (resArray[0] - resArray[2]);
		
		System.out.println("max point = ["+resArray[0]+","+resArray[1]+"]");
		System.out.println("min point = ["+resArray[2]+",0.0]");
		System.out.println("slope = ["+resArray[3]+"]");
		System.out.println("slope angle = ["+Math.atan(resArray[3])/(Math.PI/2)+"]");
		
	}
	
	
	public static boolean isRobust(double original, double current, double minPercent, boolean isMaximization){
		
		return isMaximization ? (current <= original* minPercent) : (current >= original* minPercent);
	}
}
