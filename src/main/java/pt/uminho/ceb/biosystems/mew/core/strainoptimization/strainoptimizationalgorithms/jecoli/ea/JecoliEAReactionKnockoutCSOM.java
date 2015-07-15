package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRandomMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeGrowMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeRandomMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeShrinkMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetUniformCrossover;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliEASCOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.IJecoliOptimizationStrategyConverter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.JecoliRKConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliEAReactionKnockoutCSOM extends JecoliEASCOM {


	private static final long	serialVersionUID	= 1L;


	public JecoliEAReactionKnockoutCSOM() {
        super(new JecoliRKConverter());
    }

    protected JecoliEAReactionKnockoutCSOM(IJecoliOptimizationStrategyConverter converter) {
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
