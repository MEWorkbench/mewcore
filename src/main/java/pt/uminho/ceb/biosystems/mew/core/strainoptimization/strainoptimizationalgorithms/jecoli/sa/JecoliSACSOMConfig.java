package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.sa;

import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.simulatedannealing.AnnealingSchedule;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.simulatedannealing.IAnnealingSchedule;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;

/**
 * Created by ptiago on 05-03-2015.
 */
public class JecoliSACSOMConfig extends JecoliGenericConfiguration implements IGenericConfiguration {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	public JecoliSACSOMConfig() {
		super();
		this.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM, "SA");
	}
	
	public IAnnealingSchedule getAnnealingSchedule() {
		return getDefaultValue(JecoliOptimizationProperties.ANNEALING_SCHEDULE, new AnnealingSchedule(0.007, 0.000006, 50, 50000));
	}
	
	public void setAnnealingSchedule(IAnnealingSchedule annealingSchedule) {
		propertyMap.put(JecoliOptimizationProperties.ANNEALING_SCHEDULE, annealingSchedule);
	}
}
