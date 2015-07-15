package pt.uminho.ceb.biosystems.mew.core.strainoptimization.controlcenter;

import java.util.Map;

/**
 * Created by ptiago on 12-03-2015.
 */
public class OptimizationMethodsFactory extends AbstractOptimizationMethodsFactory{
    
	private static final long	serialVersionUID	= 1L;

	public OptimizationMethodsFactory(Map<String, Class<?>> mapMethods) {
        super(mapMethods);
    }
}
