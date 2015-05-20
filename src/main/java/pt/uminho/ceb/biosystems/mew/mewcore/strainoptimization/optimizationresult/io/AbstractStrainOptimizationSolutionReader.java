package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.io;

import java.io.DataInputStream;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.IStrainOptimizationReader;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.IJecoliConfiguration;

/**
 * Created by ptiago on 18-03-2015.
 */
public abstract class AbstractStrainOptimizationSolutionReader implements IStrainOptimizationReader {
    
	private static final long	serialVersionUID	= 5429827712780750784L;

	protected abstract GeneticConditions computeGeneticConditions(IJecoliConfiguration configuration,String geneticConditionString) throws Exception;

    @Override
    public GeneticConditions readSolutionFromStream(DataInputStream inputStream,IJecoliConfiguration configuration) throws Exception {
        String geneticConditionString = inputStream.readLine();
        return computeGeneticConditions(configuration,geneticConditionString);
    }

}