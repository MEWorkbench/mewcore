package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli;


/**
 * Created by ptiago on 10-03-2015.
 */
public class JecoliPBILSCOM{}/* extends JecoliSCOM<JecoliPBILSCOMConfig> {
    @Override
    public IAlgorithm<IRepresentation> createAlgorithm(JecoliPBILSCOMConfig algorithmConfiguration, boolean isOverUnder, boolean isVariableSizeGenome, boolean isReactionSwap, ISolutionFactory solutionFactory, AbstractMultiobjectiveEvaluationFunction evaluationFunction, List<IntegerSetRepresentationFactory> swapsFactoryList) throws Exception {
        PopulationBasedIncrementalLearningConfiguration configuration = new PopulationBasedIncrementalLearningConfiguration();

        configuration.setLearningRate(algorithmConfiguration.getLearningRate());
        configuration.setNumberOfSolutionsToSelect(algorithmConfiguration.getNumberOfSolutionsToSelect());
        configuration.setProbabilityVector(algorithmConfiguration.getInitialProbabilityVector());

        configuration.setSolutionFactory(solutionFactory);

        configuration.setPopulationSize(algorithmConfiguration.getPopulationSize());
        ITerminationCriteria terminationCriteria = new NumberOfFunctionEvaluationsTerminationCriteria(algorithmConfiguration.getMaximumNumberOfFunctionEvaluations());
        configuration.setTerminationCriteria(terminationCriteria);

        configuration.setDoPopulationInitialization(true);
        configuration.setNumberOfObjectives(algorithmConfiguration.getNumberOfObjectives());

        configuration.setRandomNumberGenerator(new DefaultRandomNumberGenerator());
        configuration.setProblemBaseDirectory("nullDirectory");
        configuration.setAlgorithmStateFile("nullFile");
        configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
        configuration.setStatisticsConfiguration(algorithmConfiguration.getStatisticsConfiguration());
        configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(15);
        return (IAlgorithm)new PopulationBasedIncrementalLearning(configuration);
    }


}*/
