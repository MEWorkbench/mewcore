package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.InvalidConfigurationException;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliEASCOMConfig;


/**
 * Created by ptiago on 26-03-2015.
 */
public class JecoliEASCOMConfigTest {

    @Test(expected = InvalidConfigurationException.class)
    public void eaDefaultConfigurationsTest() throws InvalidConfigurationException {
        JecoliEASCOMConfig config = new JecoliEASCOMConfig();
        config.validate();
    }

    @Test
    public void eaDefaultConfigurationsPopulationSizeTest() throws InvalidConfigurationException {
        JecoliEASCOMConfig config = new JecoliEASCOMConfig();
        assert(100 == config.getPopulationSize());
    }

    @Test
    public void eaDefaultConfigurationsNumberOfElitistIndividualsTest() throws InvalidConfigurationException {
        JecoliEASCOMConfig config = new JecoliEASCOMConfig();
        assert(1 == config.getNumberOfElitistIndividuals());
    }

    @Test
    public void eaDefaultConfigurationsNumberOfDefaultSurvirosTest() throws InvalidConfigurationException {
        JecoliEASCOMConfig config = new JecoliEASCOMConfig();
        assert(49 == config.getNumberOfSurvivors());
    }

    @Test
    public void eaDefaultConfigurationsOffSpringSizeTest() throws InvalidConfigurationException {
        JecoliEASCOMConfig config = new JecoliEASCOMConfig();
        assert(50 == config.getOffSpringSize());
    }

}
