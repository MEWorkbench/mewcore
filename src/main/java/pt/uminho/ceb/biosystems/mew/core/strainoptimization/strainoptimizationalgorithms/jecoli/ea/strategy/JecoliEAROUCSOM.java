package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea.strategy;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.controller.IAlgorithmController;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.controller.InitialStateController;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.crossover.HybridSetUniformCrossover;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetGaussianPertubationMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetGrowthMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetRandomIntegerMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetRandomSetMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetShrinkMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetNewIndividualMutation;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.IJecoliOptimizationStrategyConverter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.JecoliROUConverter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea.JecoliEACSOM;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliEAROUCSOM extends JecoliEACSOM {

	private static final long	serialVersionUID	= 1L;

	public JecoliEAROUCSOM() {
        super(new JecoliROUConverter());
    }

    protected JecoliEAROUCSOM(IJecoliOptimizationStrategyConverter converter) {
        super(converter);
    }

    @Override
    protected ReproductionOperatorContainer createAlgorithmReproductionOperatorContainer() throws Exception {
        ReproductionOperatorContainer reproductionOperatorContainer = new ReproductionOperatorContainer();
        boolean isVariableSizeGenome = (boolean) algorithmConfiguration.getProperty(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME);

        if (isVariableSizeGenome) {
            reproductionOperatorContainer.addOperator(0.15, new HybridSetUniformCrossover());
            reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomSetMutation(2));
            reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomIntegerMutation(2));
            reproductionOperatorContainer.addOperator(0.15, new HybridSetGaussianPertubationMutation(2));
            reproductionOperatorContainer.addOperator(0.15, new HybridSetGrowthMutation());
            reproductionOperatorContainer.addOperator(0.15, new HybridSetShrinkMutation());
        } else {
            reproductionOperatorContainer.addOperator(0.15, new HybridSetUniformCrossover());
            reproductionOperatorContainer.addOperator(0.3, new HybridSetRandomSetMutation(2));
            reproductionOperatorContainer.addOperator(0.3, new HybridSetRandomIntegerMutation(2));
            reproductionOperatorContainer.addOperator(0.25, new HybridSetGaussianPertubationMutation(2));
        }

        return reproductionOperatorContainer;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public IAlgorithmController getAlgorithmController(ReproductionOperatorContainer reproductionOperatorContainer) throws Exception{
    	ReproductionOperatorContainer controllerContainer = new ReproductionOperatorContainer<>();
        controllerContainer.addOperator(1.0, new HybridSetRandomSetMutation());

        InitialStateController controller = new InitialStateController(controllerContainer, reproductionOperatorContainer);
        return controller;
    }
}
