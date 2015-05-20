package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.controlcenter;

import java.util.Map;

/**
 * Created by ptiago on 12-03-2015.
 */
public class OptimizationMethodsFactory extends AbstractOptimizationMethodsFactory{
    
	private static final long	serialVersionUID	= -5962836290165533414L;

	public OptimizationMethodsFactory(Map<String, Class<?>> mapMethods) {
        super(mapMethods);
    }
}
