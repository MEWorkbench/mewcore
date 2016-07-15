package pt.uminho.ceb.biosystems.mew.mewcore.mew.simulation.cplex;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.FBA;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.CplexParamConfiguration;

public class CPLEXBatchTest2 {
	
	public static final int numReps = 40000;
	public String file = null;
	
	public CPLEXBatchTest2(String file) {
		this.file = file;
	}
	
	private void doit() throws Exception {
		URL nyData = getClass().getClassLoader().getResource(file);
		String biomass = "R10";
		JSBMLReader reader = new JSBMLReader(nyData.getFile(), "stelling");
		Container container = new Container(reader);
		Set<String> toRemove = container.identifyMetabolitesIdByPattern(Pattern.compile(".*_ext"));
		container.removeMetabolites(toRemove);
//		container.associateDrainToMetabolite();
//		container.constructDrains(new );
		container.putDrainsInReactantsDirection();
		
		ISteadyStateModel model = ContainerConverter.convert(container);
		EnvironmentalConditions env = new EnvironmentalConditions();
		env.addReactionConstraint("R1", new ReactionConstraint(-10.0, 1000.0));
		env.addReactionConstraint("R2", new ReactionConstraint(0.0, 1000.0));
		model.setBiomassFlux(biomass);
		
//		System.out.println(model.getReactions().keySet());
//		System.out.println(model.getMetabolites().keySet());																																																																																		
		
		long before = System.currentTimeMillis();
		for(int i=0; i<numReps; i++){

			double res = simulate(model,env);
			if(i % 1000 ==0 || i==numReps-1){
				long now = System.currentTimeMillis();
				System.out.println("["+i+"] ="+ res+"! "+(now-before)+"ms ("+((int)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000000)+" MB)");
				before = now;																														
//				System.gc();
			}
		}
	}

	public static void main(String[] args) throws Exception {																							
				
		String file = "models/stelling.xml";
		CPLEXBatchTest2 batch = new CPLEXBatchTest2(file);
		CplexParamConfiguration.setIntegerParam("MIPDisplay", 0);
		CplexParamConfiguration.setIntegerParam("SimDisplay", 0);
		CplexParamConfiguration.setIntegerParam("BarDisplay", 0);
		CplexParamConfiguration.setIntegerParam("NetDisplay", 0);
		batch.doit();
				
	}
	
	public double simulate(ISteadyStateModel model, EnvironmentalConditions env) throws Exception{
		
		Map<String,Double> of = new HashMap<String,Double>();
		of.put("R10", 1.0);
		FBA fba = new FBA(model);
		fba.setIsMaximization(true);
		fba.setEnvironmentalConditions(env);
		fba.setObjectiveFunction(of);
		fba.setSolverType(SolverType.CPLEX3);
		double d = fba.simulate().getOFvalue();
		fba.forceSolverCleanup();
		return d;
	}
	
}
