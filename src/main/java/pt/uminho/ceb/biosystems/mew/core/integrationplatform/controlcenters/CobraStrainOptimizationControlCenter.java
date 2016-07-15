package pt.uminho.ceb.biosystems.mew.core.integrationplatform.controlcenters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.integrationplatform.components.CobraStrainOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.components.ConstrainedReaction;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.configuration.CobraStrainOptimizationConfiguration;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.formulations.cobra.CobraMethods;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.formulations.cobra.optimization.CobraGDLSFormulation;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.formulations.cobra.optimization.CobraOptKnockFormulation;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.RegistMethodException;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.NoConstructorMethodException;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.controlcenter.AbstractStrainOptimizationControlCenter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.RKSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.RKSolutionSet;

public class CobraStrainOptimizationControlCenter extends AbstractStrainOptimizationControlCenter<IStrainOptimizationResultSet>{
	
	public CobraStrainOptimizationControlCenter() {
		super();
		//EA Based Methods
		factory.registerMethod(CobraStrainOptimizationProperties.COBRA_GDLS, CobraGDLSFormulation.class);
		factory.registerMethod(CobraStrainOptimizationProperties.COBRA_OPTKNOCK,  CobraOptKnockFormulation.class);
	}

	@Override
	public IStrainOptimizationResultSet execute(IGenericConfiguration genericConfiguration) throws Exception {
		
		CobraStrainOptimizationConfiguration config = (CobraStrainOptimizationConfiguration)genericConfiguration;
		config.getPropertyMap().putAll(genericConfiguration.getPropertyMap());
		
		EnvironmentalConditions environmentalConditions = config.getEnvironmentalConditions();
		ISteadyStateModel model = config.getSteadyStateModel();
		String method = config.getOptimizationAlgorithm();
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(environmentalConditions, null, model, method);
		cc.registerMethod(CobraMethods.COBRAOPTKNOCK, CobraOptKnockFormulation.class);
		for (String key : config.getPropertyMap().keySet()) {
			cc.addProperty(key, config.getProperty(key));
		}
		
		SteadyStateSimulationResult simResult = cc.simulate();
		
		Map<String, SteadyStateSimulationResult> simulationResultMap = new HashMap<>();
		simulationResultMap.put(simResult.getMethod(), simResult);
		
		List<Double> fitnesses = new ArrayList<>();
		fitnesses.add(simResult.getOFvalue());
		
		RKSolutionSet<CobraStrainOptimizationConfiguration> toRet = new RKSolutionSet<CobraStrainOptimizationConfiguration>(config);
		RKSolution sol = new RKSolution(simResult.getGeneticConditions(), simulationResultMap, fitnesses);
		
		toRet.addSolution(sol);
		
		return toRet;
	}
	
	static public void registMethod(String methodId, Class<?> klass) throws RegistMethodException, NoConstructorMethodException {
		factory.addStrainOptimizationMethod(methodId, klass);
	}
	
	public static void registerMethod(String id, Class<?> method) {
		factory.registerMethod(id, method);
	}

}
