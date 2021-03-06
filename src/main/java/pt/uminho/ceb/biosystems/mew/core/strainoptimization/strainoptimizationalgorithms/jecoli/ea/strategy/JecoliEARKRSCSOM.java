package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea.strategy;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.controller.IAlgorithmController;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.controller.InitialStateController;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.dualset.DualSetMutationWrapper;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.dualset.DualSetUniformCrossoverWrapper;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetNewIndividualMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeGrowMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeRandomMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeShrinkMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.integer.IntegerSetRepresentationFactory;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.JecoliRKRSConverter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea.JecoliEACSOM;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliEARKRSCSOM extends JecoliEACSOM<JecoliRKRSConverter> {
    
	private static final long	serialVersionUID	= 1L;

	public JecoliEARKRSCSOM() {
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


    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
	public IAlgorithmController getAlgorithmController(ReproductionOperatorContainer reproductionOperatorContainer) throws Exception{
        List<Integer> genomeApplicationList = new ArrayList<>();
        genomeApplicationList.add(0);
        genomeApplicationList.add(1);

        List<IntegerSetRepresentationFactory> swapsFactoryList = optimizationStrategyConverter.getSwapsFactoryList();
    	ReproductionOperatorContainer controllerContainer = new ReproductionOperatorContainer<>();
        controllerContainer.addOperator(1.0,new DualSetMutationWrapper(new SetNewIndividualMutation<>(), genomeApplicationList, swapsFactoryList));

        InitialStateController controller = new InitialStateController(controllerContainer, reproductionOperatorContainer);
        return controller;
    }
}
