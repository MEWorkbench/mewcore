package pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.FlatFilesReader;

import pt.uminho.ceb.biosystems.mew.mewcore.criticality.CriticalReactions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.fva.FBAFluxVariabilityAnalysis;

public class ReferenceGenerator {


	public static void main(String... args) throws Exception{
		
		String MODEL				= "iAF1260";
		String FLUXES 				= "files/iAF1260_orig_simpG/Ec_iAF1260.flu";
		String MATRIX 				= "files/iAF1260_orig_simpG/Ec_iAF1260.mat";
		String METABOLITES 			= "files/iAF1260_orig_simpG/Ec_iAF1260.met";
		String GENE_RULES 			= "files/iAF1260_orig_simpG/Ec_iAF1260.gr";
		String CRITICAL_GENES 		= "files/iAF1260_orig_simpG/Ec_iAF1260.gr";
		String CRITICAL_REACTIONS 	= null;
		
		String BIOMASS 				= "R_Ec_biomass_iAF1260_core_59p81M";
//		String TARGET 				= "R_EX_succ_e_";
		String TARGET 				= "R_EX_lac_D_e_";
		
		String OUTPUT				= MODEL+"#"+TARGET+"#"+BIOMASS+".ref";
		
		boolean isGeneKnockout = true;
		
		double minBioPercent = 0.0;
		double maxBioPercent = 1.0;
		double stepSize = 0.0005;
		
		boolean minimize_nk = true;
		int mink = 0;
		int maxk = 20;
		
		// model
		FlatFilesReader reader = new FlatFilesReader(FLUXES, MATRIX, METABOLITES, GENE_RULES, "Test Model");
		
		Container container = new Container(reader);
		Set<String> bMetabolites = container.identifyMetabolitesWithDrain();
		Set<String> drains = container.getDrains();

		for(String m: bMetabolites)
			System.out.println(m);

		for(String d : drains){
			ReactionConstraintCI constraint = container.getDefaultEC().get(d);
			if(constraint==null){
				constraint = new ReactionConstraintCI(-10000, 100000);
				container.getDefaultEC().put(d, constraint);
			}
			String bounds = constraint.getLowerLimit()+"/"+constraint.getUpperLimit();
			System.out.println(d+" = "+bounds);			
		}

		ISteadyStateModel model = ContainerConverter.convert(container);
		ContainerConverter.setBoundaryMetabolitesInModel(model, bMetabolites);

		model.setBiomassFlux(BIOMASS);

		// solver
		SolverType solver = SolverType.CPLEX;

		// critical
		List<String> notAllowedIDs = new ArrayList<String>();
		if(CRITICAL_GENES!=null || CRITICAL_REACTIONS!=null)
			if (isGeneKnockout)
				notAllowedIDs = loadCriticalIDs(CRITICAL_GENES);		
			else{
				CriticalReactions cr = new CriticalReactions(model, null, solver);
				cr.loadCriticalReactionsFromFile(CRITICAL_REACTIONS);
				cr.setDrainReactionsAsCritical();
				cr.setTransportReactionsAsCritical();
				notAllowedIDs = cr.getCriticalReactionIds();
			}

		System.out.println("REACTIONS= "+model.getNumberOfReactions());
		System.out.println("METABOLITES= "+model.getNumberOfMetabolites());
		if(isGeneKnockout)
			System.out.println("GENES= "+((ISteadyStateGeneReactionModel)model).getNumberOfGenes());
		if(!isGeneKnockout)
			System.out.println("VARIABLES= "+(model.getNumberOfReactions()-notAllowedIDs.size()));
		else
			System.out.println("VARIABLES= "+(((ISteadyStateGeneReactionModel)model).getNumberOfGenes()-notAllowedIDs.size()));
		
		
		
		FBAFluxVariabilityAnalysis fva = new FBAFluxVariabilityAnalysis(model, solver);
		double wtbio = fva.getWildTypeFluxes().getValue(BIOMASS);
		
		int nsteps = (int) (maxBioPercent/stepSize);
		double currentbio = minBioPercent;
		TreeMap<Double,Double> refmap = new TreeMap<Double, Double>();
		
		System.out.println("WT Biomass = "+wtbio);
		System.out.println("Num steps = "+nsteps);
		for(int i=0; i<=nsteps; i++){
			
			double realmaxbio = (currentbio*wtbio);
			System.out.print(i+" - FVA max [wt bio = "+realmaxbio+"] target = ");
			double maxTarget = fva.optimumFlux(TARGET, currentbio, true);
			System.out.println("["+maxTarget+"]");
						
			refmap.put(realmaxbio, maxTarget);
			currentbio += stepSize;			
			
		}
		
		FileWriter fw = new FileWriter(OUTPUT);
		BufferedWriter bw = new BufferedWriter(fw);
		
		for(Double key : refmap.keySet())
			bw.append(key+","+refmap.get(key)+"\n");
		
		bw.flush();
		fw.flush();
		bw.close();
		fw.close();
		
		if(minimize_nk){
			String OUTPUT_NK = MODEL+"#"+TARGET+"#"+BIOMASS+"#MIN_K.ref";
			FileWriter fwk = new FileWriter(OUTPUT_NK);
			BufferedWriter bwk = new BufferedWriter(fwk);
			for(int i=mink; i<=maxk; i++){
				double normk = normalizek(i);
				for(Double key : refmap.keySet())
					bwk.append(key+","+refmap.get(key)+","+normk+"\n");
			}
			bwk.flush();
			fwk.flush();
			bwk.close();
			fwk.close();
		}
	}

	
	public static List<String> loadCriticalIDs(String filename) throws Exception{
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);

		ArrayList<String> critical = new ArrayList<String>();

		while(br.ready()){
			String str = br.readLine().trim();			
			critical.add(str);
		}

		br.close();
		fr.close();

		return critical;
	}
	
	public static double normalizek(int nk){
		return ( 1.0/ ( new Double(nk) +1.0 ));
	}
}
