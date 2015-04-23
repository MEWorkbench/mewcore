/**
 * 
 */
package pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.archivetrimming;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolution;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionSet;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.SolutionSet;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.comparator.SolutionPureFitnessComparator;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.AMFunctionType;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.trimming.ITrimmingFunction;

/**
 * @author pmaia
 *
 * Dec 20, 2012
 */
public class SelectionValueTrimmer implements ITrimmingFunction<IRepresentation>{
	
	private int maximumSize = 100; //default
	private double fitThreshold = 0.0000001;
	private boolean useScalarFitness = true;
	private boolean isMaximization = true;
	
	public SelectionValueTrimmer(int maximumSize){				
		this.maximumSize = maximumSize;
	}

	public SelectionValueTrimmer(int maximumSize,double fitThreshold){				
		this(maximumSize);
		this.fitThreshold  = fitThreshold;
	}
	
	public SelectionValueTrimmer(int maximumSize,double fitThreshold, boolean useScalarFitness, boolean isMaximization){
		this(maximumSize,fitThreshold);
		this.useScalarFitness  = useScalarFitness;
		this.isMaximization = isMaximization;
	}
	
	@Override
	public ISolutionSet<IRepresentation> trimm(ISolutionSet<IRepresentation> original) {
	
		int size = original.getNumberOfSolutions();
		
//		int uplim = (size>5) ? 5 : size;
//		int lowlim = (size>uplim+5) ? size-5 : size;
//		System.out.println("Trimmer: original");
//		for(int i=0; i<uplim; i++){
//			System.out.println("\t["+i+"]"+original.getSolution(i).getScalarFitnessValue());
//		}
//		for(int i=lowlim; i<size; i++){
//			System.out.println("\t["+i+"]"+original.getSolution(i).getScalarFitnessValue());
//		}
		
		SolutionPureFitnessComparator<ISolution<IRepresentation>> comparator = new SolutionPureFitnessComparator<ISolution<IRepresentation>>(!isUseScalarFitness());
		original.sort(comparator, true, isMaximization);
		
//		System.out.println("Trimmer: sorted");
//		for(int i=0; i<uplim; i++){
//			System.out.println("\t["+i+"]"+original.getSolution(i).getScalarFitnessValue());
//		}
//		for(int i=lowlim; i<size; i++){
//			System.out.println("\t["+i+"]"+original.getSolution(i).getScalarFitnessValue());
//		}
		
		if(size>maximumSize){
			SolutionSet<IRepresentation> newArchive = new SolutionSet<IRepresentation>();
			for(int i= 0; i<maximumSize;i++){
				ISolution<IRepresentation> solution = original.getSolution(i);
				double value = isUseScalarFitness() ? solution.getScalarFitnessValue() : solution.getSelectionValue();

				if(value>fitThreshold)
					newArchive.add(solution);
			}
			
			original = newArchive;					
		}
		
		return original;
	}

	
	@Override
	public AMFunctionType getFunctionType() {
		return AMFunctionType.TRIMMER;
	}

	/**
	 * @return the useScalarFitness
	 */
	public boolean isUseScalarFitness() {
		return useScalarFitness;
	}

}
