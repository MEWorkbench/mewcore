package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.IAlgorithmResult;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.IAlgorithmStatistics;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.writer.IAlgorithmResultWriter;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.configuration.InvalidConfigurationException;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRandomMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetUniformCrossover;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.selection.BestSelection;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.selection.TournamentSelection;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.randomnumbergenerator.DefaultRandomNumberGenerator;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.integer.IntegerSetRepresentationFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.set.AbstractSetRepresentationFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.set.ISetRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolution;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.statistics.StatisticsConfiguration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.ITerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.NumberOfFunctionEvaluationsTerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.evolutionary.EvolutionaryAlgorithm;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.evolutionary.EvolutionaryConfiguration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.evolutionary.RecombinationParameters;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.algebra.MFAClassicAlgebra;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ratioconstraints.FluxRatioConstraint;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

public class EstimateVMs {

	ProblemConf problemInstance;
	int maxPopulationSize;
	int maxElement;
	int nFunctionEvaluations;
	int maxNelements;
	
	EvolutionaryAlgorithm algorithm;	
	IAlgorithmStatistics statistics;	
	IAlgorithmResult results;
	

	public EstimateVMs(MFAClassicAlgebra mfa, String vmCandidatesFile, int maxPopulationSize, int nFunctionEvaluations) throws InvalidConfigurationException, Exception{
		this.problemInstance = new ProblemConf(mfa, vmCandidatesFile);
		this.maxPopulationSize = maxPopulationSize;
		this.nFunctionEvaluations = nFunctionEvaluations;
		this.maxElement = problemInstance.vmCandidatesToModelIndexes.size();
		this.maxNelements = 10;
		configureAlgorithm();
	}
	
	
	public void configureAlgorithm() throws InvalidConfigurationException, Exception{
		
		EvolutionaryConfiguration<ISetRepresentation<Integer>, AbstractSetRepresentationFactory<Integer>> configuration = 
			new EvolutionaryConfiguration<ISetRepresentation<Integer>, AbstractSetRepresentationFactory<Integer>>();
		
		IEvaluationFunction<ISetRepresentation<Integer>> evaluationFunction = new EvalFunction(problemInstance);
		configuration.setEvaluationFunction(evaluationFunction);
		
		AbstractSetRepresentationFactory solutionFactory = new IntegerSetRepresentationFactory(maxElement, maxNelements);
		configuration.setSolutionFactory(solutionFactory);
		
		RecombinationParameters recombinationParameters = new RecombinationParameters(maxPopulationSize);
		configuration.setRecombinationParameters(recombinationParameters);
		
		configuration.setRandomNumberGenerator(new DefaultRandomNumberGenerator());
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<ISetRepresentation<Integer>>>());
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		
		configuration.setSelectionOperators(new BestSelection<ISetRepresentation<Integer>>());
		configuration.setSurvivorSelectionOperator(new TournamentSelection<ISetRepresentation<Integer>>(1,4));
		
//		ReproductionOperatorContainer<ILinearRepresentation<Double>, RealValueRepresentationFactory> cont = new ReproductionOperatorContainer<ILinearRepresentation<Double>, RealValueRepresentationFactory>();
//		cont.addOperator(0.1, new LinearGenomeRandomMutation<Double>(3));
		
		
		ReproductionOperatorContainer operatorContainer = new ReproductionOperatorContainer();

		operatorContainer.addOperator(0.7, new SetUniformCrossover<Integer>());// LinearGenomeRandomMutation<Double>((RealValueRepresentationFactory)solutionFactory));
//		operatorContainer.addOperator(1, new GaussianPerturbationMutation(1)); // new GaussianPerturbationMutation((RealValueRepresentationFactory)solutionFactory));
//		operatorContainer.addOperator(1, new RealValueArithmeticalCrossover());
		operatorContainer.addOperator(0.3, new SetRandomMutation<ISetRepresentation<Integer>>()); // new TwoPointCrossOver<Double>((RealValueRepresentationFactory)solutionFactory));
		
		configuration.setReproductionOperatorContainer(operatorContainer);
		ITerminationCriteria terminationCriteria = new NumberOfFunctionEvaluationsTerminationCriteria(nFunctionEvaluations);
		configuration.setTerminationCriteria(terminationCriteria);
		
		configuration.setPopulationSize(maxPopulationSize);
		algorithm = new EvolutionaryAlgorithm(configuration);
	}
	
	
	public void run(String candidateVmsFile, String outputFile) throws Exception
	{
		String biomassFlux = problemInstance.mfa.getModel().getBiomassFlux();
		int biomassColumn = problemInstance.mfa.getModel().getReactionIndex(biomassFlux);
//		System.out.println("Decimal: " + problemInstance.mfa.getColumnMaxDecimal(problemInstance.stoichiometricMatrix, biomassColumn));
		
		Algebra algebra = new Algebra();
		if(problemInstance.mfa.getFluxRatioConstraints()!=null)
		{
			int size = problemInstance.mfa.getFluxRatioConstraints().size();
			for(int i=0; i<size; i++)
			{
				FluxRatioConstraint ratio = problemInstance.mfa.getFluxRatioConstraints().get(0);
				problemInstance.mfa.getFluxRatioConstraints().remove(0);
				DoubleMatrix2D modelS = problemInstance.mfa.addRatiosToStoichiometricMatrix().convertToColt();
				int r1 = algebra.rank(algebra.transpose(problemInstance.mfa.getModel().getStoichiometricMatrix().convertToColt()));
				int r2 = algebra.rank(algebra.transpose(modelS));
				System.out.println(">>>>>> >>>>> >>> >> > " + ratio.getExpression());
				System.out.println(">>>>>> >>>>> >>>>> >>> >> > " + r1 + " # " + r2);
				problemInstance.mfa.getFluxRatioConstraints().add(ratio);
			}
		}
		
		IAlgorithmResult results =  algorithm.run();
		IAlgorithmStatistics statistics = results.getAlgorithmStatistics();
		ISolution bestSolution =results.getSolutionContainer().getBestSolutionCellContainer(true).getSolution();
		ISetRepresentation<Integer> genome = (ISetRepresentation<Integer>) bestSolution.getRepresentation();
		EvalFunction evaluationFunction = (EvalFunction) (algorithm.getConfiguration().getEvaluationFunction());
		int[] res = evaluationFunction.decodeGenome(genome);
		System.out.println("#\n#\n# Solution size: " + res.length);
		for(int i:res)
		{
			System.out.print(" # " + i);
		}
		
		problemInstance.print(res);
		problemInstance.vmCsvFromSolution(candidateVmsFile, res, outputFile);
		System.out.println("$$$ " + problemInstance.best);
		System.out.println("#### DONE, saved to: " + new File(outputFile).getAbsolutePath());
	}

	
//	public void printStatsPerIteration ()
//	{
//		System.out.println("Iteration\tBest\t\tMean\n");
//		for(int i=0; i < getStatistics().getNumberOfIterations(); i+=20)
//		{
//			System.out.print(i+"\t");
//			System.out.print(getStatistics().getRunObjectiveMaxFitnessValue(i) + "\t");
//			System.out.print(getStatistics().getRunObjectiveMeanFitnessValue(i)+"\n");
//		}	
//	}
	

	class ProblemConf{
		MFAClassicAlgebra mfa;
		DoubleMatrix2D stoichiometricMatrix;
		Map<Integer, Integer> vmCandidatesToModelIndexes;
		Map<Integer, Double> vmModelWeights;
		double bestSol;
		String best;
		
		public ProblemConf(MFAClassicAlgebra mfa, String vmCandidatesFile){
			this.mfa = mfa;
			this.stoichiometricMatrix = mfa.addRatiosToStoichiometricMatrix().convertToColt();
			this.vmCandidatesToModelIndexes = new HashMap<Integer, Integer>();
			this.vmModelWeights = new HashMap<Integer, Double>();
			bestSol = 0;
			best = "";
			
			try {
				FileReader fr = new FileReader(vmCandidatesFile);
				BufferedReader br = new BufferedReader(fr);
				String line;
				int count=0;
				while((line=br.readLine())!=null)
				{
					String[] tokens = line.split(",");
					int modelIndex = mfa.getModel().getReactionIndex(tokens[0]);
					vmCandidatesToModelIndexes.put(count++, modelIndex);
					vmModelWeights.put(modelIndex, Double.parseDouble(tokens[1]));
				}
				br.close();
				fr.close();
			} catch (Exception e) {e.printStackTrace();}
		}
		
		public ProblemConf(MFAClassicAlgebra mfa, Map<String, Double> candidateVms){
			this.mfa = mfa;
			this.stoichiometricMatrix = mfa.addRatiosToStoichiometricMatrix().convertToColt();
			this.vmCandidatesToModelIndexes = new HashMap<Integer, Integer>();
			this.vmModelWeights = new HashMap<Integer, Double>();
			bestSol = 0;
			best = "";
			
			int count=0;
			for(String fId : candidateVms.keySet())
			{
				int modelIndex = mfa.getModel().getReactionIndex(fId);
				vmCandidatesToModelIndexes.put(count++, modelIndex);
				vmModelWeights.put(modelIndex, candidateVms.get(fId));
			}
		}
		
		public double calculateFitness(int[] sol) throws Exception{
			ExpMeasuredFluxes measuredFluxes = new ExpMeasuredFluxes();
			for(int r : sol)
			{
				String rId = mfa.getModel().getReactionId(r);
				measuredFluxes.put(rId, 0.0, 0.0);
			}
			mfa.setVmModelIndexesMap(measuredFluxes.getIndexesFluxeValuesErrorMap(mfa.getModel()));
			DoubleMatrix2D gC = (DoubleMatrix2D) mfa.configureMatrices(stoichiometricMatrix)[1];
			
			Algebra algebra = new Algebra();
			int rankGc = algebra.rank(gC);
			
			int diff = gC.columns() - rankGc;
			
			double absSum = 1;
			for(int i : sol)
				absSum += Math.abs(problemInstance.vmModelWeights.get(i));
			
			System.out.println(">>>>>>>>>>>>>>>> " + diff + " >> " + absSum + " added: " + sol.length);
//			
//			if(diff==0)
//			{
//				double b = rankGc - absSum;
//				if(b>bestSol)
//				{
//					String s = "";
//					for(int i : sol)
//						s += i + ",";
//					best = "= Gc columns : " + gC.columns() + " = Rank GC: "  + rankGc + " ## diff " + diff + " ## sum " + absSum + " = " + s + " # size: " + sol.length;
//					bestSol = b;
//				}
//			}
			
			double f = (double) (Math.pow(diff+2, 7) + Math.pow(sol.length, 4) ) / Math.pow(absSum,2);
			System.out.println("Fit: " + f);
			return f;
			
//			System.out.println(">>>>>>>>>>>>>>>>>>>>>>> " + diff);
//			return (double) 1/(Math.pow(diff+1, 2));
		}
		
		public void print(int[] sol) throws Exception{
			ExpMeasuredFluxes measuredFluxes = new ExpMeasuredFluxes();
			System.out.println("############## SOL ##############");
			for(int r : sol)
			{
				String rId = mfa.getModel().getReactionId(r);
				System.out.println(r+"#"+rId);
				measuredFluxes.put(rId, 0.0, 0.0);
			}
			System.out.println("############## /SOL ##############");
			mfa.setVmModelIndexesMap(measuredFluxes.getIndexesFluxeValuesErrorMap(mfa.getModel()));
			DoubleMatrix2D gC = (DoubleMatrix2D) mfa.configureMatrices(stoichiometricMatrix)[1];
			
			Algebra algebra = new Algebra();
			double rankGc = algebra.rank(gC);
			System.out.println("\n############################################# RANK Gc: " + rankGc + " # " + gC.rows() + "x" + gC.columns() + " # " + sol.length);
			System.out.println("############################################# RANK Sr: " + algebra.rank(algebra.transpose(stoichiometricMatrix)) + " # " + stoichiometricMatrix.rows() + "x" + stoichiometricMatrix.columns());
			
			System.out.println("############################################# Fitness: " + (1/(gC.columns() - rankGc)));
		}
		
		public void vmCsvFromSolution(String vmCandidatesFile, int[] solution, String outputFile) throws IOException{
			Map<String,String> allFluxes = new HashMap<String,String>();
			FileReader fr = new FileReader(new File(vmCandidatesFile));
			BufferedReader br = new BufferedReader(fr);
			String line;
			while((line=br.readLine())!=null)
			{
				String[] ts = line.split(",");
				allFluxes.put(ts[0], ts[1]);
			}
			br.close();
			fr.close();
			
			FileWriter fw = new FileWriter(new File(outputFile));
			BufferedWriter bw = new BufferedWriter(fw);
			for(int f : solution)
			{
				String fId = mfa.getModel().getReactionId(f);
				bw.write(fId + "," + allFluxes.get(fId) + "\n");
			}
			bw.flush();
			bw.close();
			fw.close();
		}
	}
	
	class EvalFunction extends AbstractEvaluationFunction<ISetRepresentation<Integer>>{
	
		private static final long serialVersionUID = 3777659218627978693L;
	
		private ProblemConf problemInstance;
		
		public EvalFunction(ProblemConf problemInstance){
			super(false); // Minimization Problem
			this.problemInstance = problemInstance;
		}
		
		public int[] decodeGenome(ISetRepresentation<Integer> solution){
			int[] res = new int[solution.getNumberOfElements()];
			for(int i = 0;i < res.length; i++)
				res[i] = problemInstance.vmCandidatesToModelIndexes.get(solution.getElementAt(i));
			return res;
		}
	
		@Override
		public double evaluate(ISetRepresentation<Integer> solutionRepresentation) throws Exception {
			int[] decodedSolution = decodeGenome(solutionRepresentation);
			double fitness = problemInstance.calculateFitness(decodedSolution);
//			double penalty = decodedSolution.length*0.005;
//			System.out.println(">>>>>>>> fit: " + fitness + " # penalty: " + penalty);
//			return fitness - penalty;
			return fitness;
		}
		
		@Override
		public IEvaluationFunction<ISetRepresentation<Integer>> deepCopy()throws Exception {return null;}
		@Override
		public void verifyInputData() throws InvalidEvaluationFunctionInputDataException {}
	
	}
}


