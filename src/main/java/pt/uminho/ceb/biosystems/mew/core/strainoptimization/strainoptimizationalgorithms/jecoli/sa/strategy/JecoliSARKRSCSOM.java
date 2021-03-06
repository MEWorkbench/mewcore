package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.sa.strategy;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.dualset.DualSetMutationWrapper;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRandomMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeGrowMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeRandomMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeShrinkMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.integer.IntegerSetRepresentationFactory;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.JecoliRKRSConverter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.sa.JecoliSACSOM;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliSARKRSCSOM extends JecoliSACSOM<JecoliRKRSConverter> {

	private static final long	serialVersionUID	= 1L;

	public JecoliSARKRSCSOM() {
        super(new JecoliRKRSConverter());
    }

    @Override
    protected ReproductionOperatorContainer createAlgorithmReproductionOperatorContainer() throws Exception {
        List<Integer> genomeApplicationList = new ArrayList<>();
        genomeApplicationList.add(0);
        genomeApplicationList.add(1);

        List<IntegerSetRepresentationFactory> swapsFactoryList = optimizationStrategyConverter.getSwapsFactoryList();

        ReproductionOperatorContainer reproductionOperatorContainer = new ReproductionOperatorContainer();
        boolean isVariableSizeGenome = algorithmConfiguration.getIsVariableSizeGenome();

        if (isVariableSizeGenome) {
            reproductionOperatorContainer.addOperator(0.50, new DualSetMutationWrapper(new SetRelativeRandomMutation(), genomeApplicationList, swapsFactoryList));
            reproductionOperatorContainer.addOperator(0.25, new DualSetMutationWrapper(new SetRelativeGrowMutation(), genomeApplicationList, swapsFactoryList));
            reproductionOperatorContainer.addOperator(0.25, new DualSetMutationWrapper(new SetRelativeShrinkMutation(), genomeApplicationList, swapsFactoryList));
        } else {
            reproductionOperatorContainer.addOperator(1.0, new DualSetMutationWrapper(new SetRandomMutation(), genomeApplicationList, swapsFactoryList));
        }
        return reproductionOperatorContainer;
    }
}
