package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea.strategy;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeGrowMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeRandomMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeShrinkMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetUniformCrossover;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.IJecoliOptimizationStrategyConverter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.JecoliRKConverter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea.JecoliEACSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea.JecoliEACSOMConfig;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliEARKCSOM extends JecoliEACSOM<IJecoliOptimizationStrategyConverter<JecoliEACSOMConfig, ?>> {
	
	private static final long serialVersionUID = 1L;
	
	public JecoliEARKCSOM() {
		super(new JecoliRKConverter());
	}
	
	protected JecoliEARKCSOM(IJecoliOptimizationStrategyConverter converter) {
		super(converter);
	}
	
	@Override
	protected ReproductionOperatorContainer createAlgorithmReproductionOperatorContainer() throws Exception {
		ReproductionOperatorContainer reproductionOperatorContainer = new ReproductionOperatorContainer();
		boolean isVariableSizeGenome = (boolean) algorithmConfiguration.getProperty(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME);
		
		double crossoverProbability = algorithmConfiguration.getCrossoverProbability();
		double mutationProbability = algorithmConfiguration.getMutationProbability();
		double growProbability = algorithmConfiguration.getGrowProbability();
		double shrinkProbability = algorithmConfiguration.getShrinkProbability();
		double mutationRadiusPercentage = algorithmConfiguration.getMutationRadiusPercentage();
		
		if (isVariableSizeGenome) {
//        	System.out.println("IS VARSIZE");
			reproductionOperatorContainer.addOperator(crossoverProbability, new SetUniformCrossover());
			reproductionOperatorContainer.addOperator(mutationProbability, new SetRelativeRandomMutation<>(mutationRadiusPercentage));
			reproductionOperatorContainer.addOperator(growProbability, new SetRelativeGrowMutation<>());
			reproductionOperatorContainer.addOperator(shrinkProbability, new SetRelativeShrinkMutation<>());
		} else {
//        	System.out.println("IS NOT VARSIZE");
			reproductionOperatorContainer.addOperator(crossoverProbability, new SetUniformCrossover());
			reproductionOperatorContainer.addOperator(mutationProbability, new SetRelativeRandomMutation(mutationRadiusPercentage));
		}
		
		return reproductionOperatorContainer;
	}
	
}
