package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli;

import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.simulatedannealing.AnnealingSchedule;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.simulatedannealing.IAnnealingSchedule;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.configuration.IGenericConfiguration;

/**
 * Created by ptiago on 05-03-2015.
 */
public class JecoliSASCOMConfig extends JecoliGenericConfiguration implements IGenericConfiguration {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	public JecoliSASCOMConfig() {
		super();
		this.setProperty(JecoliOptimizationProperties.OPTIMIZATION_ALGORITHM, "SA");
	}
	
	public IAnnealingSchedule getAnnealingSchedule() {
		return getDefaultValue(JecoliOptimizationProperties.ANNEALING_SCHEDULE, new AnnealingSchedule(0.007, 0.000006, 50, 50000));
	}
	
	public void setAnnealingSchedule(IAnnealingSchedule annealingSchedule) {
		propertyMap.put(JecoliOptimizationProperties.ANNEALING_SCHEDULE, annealingSchedule);
	}
}
