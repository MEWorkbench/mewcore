package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.spea2.strategy;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.dualset.DualSetMutationWrapper;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.dualset.DualSetUniformCrossoverWrapper;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetGrowthMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRandomMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeRandomMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetShrinkMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.integer.IntegerSetRepresentationFactory;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.JecoliRKRSConverter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.spea2.JecoliSPEAIICSOM;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliSPEA2RKRSCSOM extends JecoliSPEAIICSOM<JecoliRKRSConverter> {

    /**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	public JecoliSPEA2RKRSCSOM() {
        super(new JecoliRKRSConverter());
    }

    @Override
    protected ReproductionOperatorContainer createAlgorithmReproductionOperatorContainer() throws Exception {
        ReproductionOperatorContainer reproductionOperatorContainer = new ReproductionOperatorContainer();
        boolean isVariableSizeGenome = algorithmConfiguration.getIsVariableSizeGenome();
        List<IntegerSetRepresentationFactory> swapsFactoryList = optimizationStrategyConverter.getSwapsFactoryList();

        List<Integer> genomeApplicationList = new ArrayList<>();
        genomeApplicationList.add(0);
        genomeApplicationList.add(1);

        if (isVariableSizeGenome) {
            reproductionOperatorContainer.addOperator(0.25, new DualSetUniformCrossoverWrapper(genomeApplicationList, swapsFactoryList));
            reproductionOperatorContainer.addOperator(0.5, new DualSetMutationWrapper(new SetRelativeRandomMutation(), genomeApplicationList, swapsFactoryList));
            reproductionOperatorContainer.addOperator(0.125, new DualSetMutationWrapper(new SetGrowthMutation(), genomeApplicationList, swapsFactoryList));
            reproductionOperatorContainer.addOperator(0.125, new DualSetMutationWrapper(new SetShrinkMutation(), genomeApplicationList, swapsFactoryList));
        } else {
            reproductionOperatorContainer.addOperator(0.5, new DualSetUniformCrossoverWrapper(genomeApplicationList, swapsFactoryList));
            reproductionOperatorContainer.addOperator(0.5, new DualSetMutationWrapper(new SetRandomMutation(), genomeApplicationList, swapsFactoryList));
        }

        return null;
    }
}
