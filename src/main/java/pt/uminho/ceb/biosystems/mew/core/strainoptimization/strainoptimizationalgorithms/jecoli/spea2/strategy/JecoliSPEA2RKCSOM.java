package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.spea2.strategy;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRandomMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeGrowMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeRandomMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeShrinkMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetUniformCrossover;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.IJecoliOptimizationStrategyConverter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.JecoliRKConverter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.spea2.JecoliSPEAIICSOM;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliSPEA2RKCSOM extends JecoliSPEAIICSOM {

	private static final long	serialVersionUID	= 1L;

	public JecoliSPEA2RKCSOM() {
        super(new JecoliRKConverter());
    }

    protected JecoliSPEA2RKCSOM(IJecoliOptimizationStrategyConverter converter) {
        super(converter);
    }

    @Override
    protected ReproductionOperatorContainer createAlgorithmReproductionOperatorContainer() throws Exception {
        ReproductionOperatorContainer reproductionOperatorContainer = new ReproductionOperatorContainer();
        boolean isVariableSizeGenome = (boolean) algorithmConfiguration.getProperty(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME);

        if (isVariableSizeGenome) {
            reproductionOperatorContainer.addOperator(0.25, new SetUniformCrossover());
            reproductionOperatorContainer.addOperator(0.5, new SetRelativeRandomMutation<>());
            reproductionOperatorContainer.addOperator(0.125, new SetRelativeGrowMutation<>());
            reproductionOperatorContainer.addOperator(0.125, new SetRelativeShrinkMutation<>());
        } else {
            reproductionOperatorContainer.addOperator(0.5, new SetUniformCrossover());
            reproductionOperatorContainer.addOperator(0.5, new SetRandomMutation());
        }

        return reproductionOperatorContainer;
    }
}
