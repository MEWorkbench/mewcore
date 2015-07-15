package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.dualset.DualSetMutationWrapper;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.dualset.DualSetUniformCrossoverWrapper;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeGrowMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeRandomMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeShrinkMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.integer.IntegerSetRepresentationFactory;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliEASCOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.JecoliRKRSConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliEAReactionSwapReactionKnockoutCSOM extends JecoliEASCOM<JecoliRKRSConverter> {
    
	private static final long	serialVersionUID	= 1L;

	public JecoliEAReactionSwapReactionKnockoutCSOM() {
        super(new JecoliRKRSConverter());
    }

    @Override
    protected ReproductionOperatorContainer createAlgorithmReproductionOperatorContainer() throws Exception {
        ReproductionOperatorContainer reproductionOperatorContainer = new ReproductionOperatorContainer();
        boolean isVariableSizeGenome = algorithmConfiguration.getIsVariableSizeGenome();
        List<Integer> genomeApplicationList = new ArrayList<>();
        genomeApplicationList.add(0);
        genomeApplicationList.add(1);

        List<IntegerSetRepresentationFactory> swapsFactoryList = optimizationStrategyConverter.getSwapsFactoryList();

        if (isVariableSizeGenome) {
            reproductionOperatorContainer.addOperator(0.25, new DualSetUniformCrossoverWrapper(genomeApplicationList, swapsFactoryList));
            reproductionOperatorContainer.addOperator(0.50, new DualSetMutationWrapper(new SetRelativeRandomMutation(), genomeApplicationList, swapsFactoryList));
            reproductionOperatorContainer.addOperator(0.125, new DualSetMutationWrapper(new SetRelativeGrowMutation(), genomeApplicationList, swapsFactoryList));
            reproductionOperatorContainer.addOperator(0.125, new DualSetMutationWrapper(new SetRelativeShrinkMutation(), genomeApplicationList, swapsFactoryList));
        } else {
            reproductionOperatorContainer.addOperator(0.5, new DualSetUniformCrossoverWrapper(genomeApplicationList, swapsFactoryList));
            reproductionOperatorContainer.addOperator(0.5, new DualSetMutationWrapper(new SetRelativeRandomMutation(), genomeApplicationList, swapsFactoryList));
        }
        return reproductionOperatorContainer;
    }


}
