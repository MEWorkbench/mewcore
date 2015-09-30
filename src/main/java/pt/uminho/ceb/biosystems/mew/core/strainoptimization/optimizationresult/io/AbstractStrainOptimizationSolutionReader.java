package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationReader;

/**
 * Created by ptiago on 18-03-2015.
 */
public abstract class AbstractStrainOptimizationSolutionReader implements IStrainOptimizationReader {
    
	private static final long	serialVersionUID	= 1L;

	protected abstract GeneticConditions processGeneticConditions(String geneticConditionString) throws Exception;

    @Override
    public GeneticConditions readSolutionFromStream(InputStream inputStream) throws Exception {
    	BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String geneticConditionString = br.readLine();
        return processGeneticConditions(geneticConditionString);
    }

}