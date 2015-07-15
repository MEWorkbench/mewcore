package pt.uminho.ceb.biosystems.mew.core.mew.simulation.formulations;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.configuration.ModelConfiguration;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.FBA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.PFBA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractObjTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.properties.MFAProperties;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.CplexParamConfiguration;

public class PFBATest {
	
	public static void main(String[] args) throws Exception {
		String _modelConfFile = "files/mfafiles/ecoli_core.conf";		
		String _envConditionsFile1 = "files/mfafiles/ecEnvConditions1.env";
		String _envConditionsFile2 = "files/mfafiles/ecEnvConditions2.env";
		
		AbstractObjTerm.setMaxValue(Double.MAX_VALUE);
		AbstractObjTerm.setMinValue(-Double.MAX_VALUE);
		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);
		CplexParamConfiguration.setIntegerParam("MIPEmphasis", 2);
		CplexParamConfiguration.setBooleanParam("NumericalEmphasis", true);
		CplexParamConfiguration.setBooleanParam("PreInd", true);
		CplexParamConfiguration.setIntegerParam("HeurFreq", -1);
		
		/**
		 * MODEL
		 */
		ModelConfiguration modelConf = new ModelConfiguration(_modelConfFile);
		ISteadyStateModel _model = modelConf.getModel();
		String _biomassID = modelConf.getModelBiomass();
		
		/**
		 * ENVIRONMENTAL CONDITIONS
		 */
		EnvironmentalConditions _envConditions1 = EnvironmentalConditions.readFromFile(_envConditionsFile1, ",");
		EnvironmentalConditions _envConditions2 = EnvironmentalConditions.readFromFile(_envConditionsFile2, ",");

		/**
		 * GENETIC CONDITIONS
		 */
		GeneticConditions _genConditions = new GeneticConditions(new ReactionChangesList(new String[] { "R_PTAr", "R_ALCD2x" }));
		
		String _targetID = "R_EX_lac_D_e";
		
		
		
//		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, _model, SimulationProperties.PARSIMONIUS);
//		cc.setMaximization(true);
//		cc.setSolver(SolverType.CLP);
//		cc.setFBAObjSingleFlux(_biomassID, 1.0);
//		cc.setEnvironmentalConditions(_envConditions1);
//		
//		SteadyStateSimulationResult res = cc.simulate();
//		System.out.println("biomass 1 = "+res.getFluxValues().get(_biomassID));
//		System.out.println("target= "+res.getFluxValues().get(_targetID));
//		System.out.println(res.getOFvalue());
//		
//		cc.setEnvironmentalConditions(_envConditions2);
//		res = cc.simulate();
//		System.out.println("biomass 2 = "+res.getFluxValues().get(_biomassID));
//		System.out.println("target= "+res.getFluxValues().get(_targetID));
//		System.out.println(res.getOFvalue());
//		
//		cc.setGeneticConditions(_genConditions);
//		cc.setEnvironmentalConditions(_envConditions1);
//		res = cc.simulate();
//		System.out.println("biomass 3 = "+res.getFluxValues().get(_biomassID));
//		System.out.println("target= "+res.getFluxValues().get(_targetID));
//		System.out.println(res.getOFvalue());
//		
		PFBA<FBA> mfaPars = new PFBA<>(_model);	
		mfaPars.setEnvironmentalConditions(_envConditions1);
		mfaPars.setProperty(MFAProperties.IS_MAXIMIZATION, true);		
		mfaPars.setSolverType(SolverType.CPLEX);
		
		SteadyStateSimulationResult res = mfaPars.simulate();
		System.out.println("biomass 1 = "+res.getFluxValues().get(_biomassID));
		System.out.println("target= "+res.getFluxValues().get(_targetID));
		System.out.println(res.getOFvalue());
		
		mfaPars.setEnvironmentalConditions(_envConditions2);
		res = mfaPars.simulate();
		System.out.println("biomass 2 = "+res.getFluxValues().get(_biomassID));
		System.out.println("target= "+res.getFluxValues().get(_targetID));
		System.out.println(res.getOFvalue());
		
		mfaPars.setEnvironmentalConditions(_envConditions1);
		mfaPars.setGeneticConditions(_genConditions);		
		res = mfaPars.simulate();
		System.out.println("biomass 3 = "+res.getFluxValues().get(_biomassID));
		System.out.println("target= "+res.getFluxValues().get(_targetID));
		System.out.println(res.getOFvalue());
	
	}
	
}
