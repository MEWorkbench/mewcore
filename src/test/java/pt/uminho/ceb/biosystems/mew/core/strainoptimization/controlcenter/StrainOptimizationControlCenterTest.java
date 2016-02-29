package pt.uminho.ceb.biosystems.mew.core.strainoptimization.controlcenter;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericOptimizationProperties;

/**
 * Created by ptiago on 26-03-2015.
 */
public class StrainOptimizationControlCenterTest {
    @Test
    public void propertySetControlCenterTest() throws Exception {
        StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();
        cc.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM,"SPEA2");
        String algorithmValue = (String) cc.getProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM);
        assert(algorithmValue.compareTo("SPEA2") == 0);
    }

    @Test (expected = Exception.class)
    public void noPropertySetControlCenterTest() throws Exception {
        StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();
        cc.execute();
    }

    @Test (expected = Exception.class)
    public void invalidAlgorithmControlCenterTest() throws Exception {
        StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();
        cc.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM,"SPEA1020");
        cc.setProperty(GenericOptimizationProperties.OPTIMIZATION_STRATEGY,"RK");
        cc.execute();
    }

    @Test (expected = Exception.class)
    public void invalidStrategyControlCenterTest() throws Exception {
        StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();
        cc.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM,"SA");
        cc.setProperty(GenericOptimizationProperties.OPTIMIZATION_STRATEGY,"RKA");
        cc.execute();
    }

    @Test (expected = Exception.class)
    public void strategyNotDefinedControlCenterTest() throws Exception {
        StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();
        cc.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM,"EA");
        cc.execute();
    }


}
