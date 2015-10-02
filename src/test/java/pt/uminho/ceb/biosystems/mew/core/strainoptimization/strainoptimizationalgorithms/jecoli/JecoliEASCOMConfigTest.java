package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.InvalidConfigurationException;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea.JecoliEACSOMConfig;


/**
 * Created by ptiago on 26-03-2015.
 */
public class JecoliEASCOMConfigTest {

    @Test(expected = InvalidConfigurationException.class)
    public void eaDefaultConfigurationsTest() throws InvalidConfigurationException {
        JecoliEACSOMConfig config = new JecoliEACSOMConfig();
        config.validate();
    }

    @Test
    public void eaDefaultConfigurationsPopulationSizeTest() throws InvalidConfigurationException {
        JecoliEACSOMConfig config = new JecoliEACSOMConfig();
        assert(100 == config.getPopulationSize());
    }

    @Test
    public void eaDefaultConfigurationsNumberOfElitistIndividualsTest() throws InvalidConfigurationException {
        JecoliEACSOMConfig config = new JecoliEACSOMConfig();
        assert(1 == config.getNumberOfElitistIndividuals());
    }

    @Test
    public void eaDefaultConfigurationsNumberOfDefaultSurvirosTest() throws InvalidConfigurationException {
        JecoliEACSOMConfig config = new JecoliEACSOMConfig();
        assert(49 == config.getNumberOfSurvivors());
    }

    @Test
    public void eaDefaultConfigurationsOffSpringSizeTest() throws InvalidConfigurationException {
        JecoliEACSOMConfig config = new JecoliEACSOMConfig();
        assert(50 == config.getOffSpringSize());
    }

}
