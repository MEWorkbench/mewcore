package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli;

/**
 * Created by ptiago on 09-03-2015.
 */
public class JecoliSPEAIISCOMConfig extends JecoliGenericConfiguration{
    private static final String POPULATION_SIZE = "jecoli.spea2.populationsize";
    private static final String ARCHIVE_SIZE = "jecoli.spea2.archivesize";
    private static final String ELITISM = "jecoli.spea2.elitism";
    private static final String NUMBER_OF_SURVIVORS = "jecoli.spea2.numberofsurvivors";
    private static final String OFFSPRING_SIZE = "jecoli.spea2.offspringsize";
    private int numberOfSurvivors;


    public JecoliSPEAIISCOMConfig() {
        super();
        this.setProperty(JecoliOptimizationProperties.OPTIMIZATION_ALGORITHM,"SPEA2");
    }

    public int getPopulationSize() {
        return getDefaultValue(POPULATION_SIZE, 100);
    }

    public int getArchiveSize() {
        return getDefaultValue(ARCHIVE_SIZE, 100);
    }

    public int getNumberOfElitistIndividuals() {
        return getDefaultValue(ELITISM, 1);
    }

    public int getNumberOfSurvivors() {
        return getDefaultValue(NUMBER_OF_SURVIVORS, 49);
    }

    public int getOffSpringSize() {
        return getDefaultValue(OFFSPRING_SIZE,50);
    }
}
