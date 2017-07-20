package pt.uminho.ceb.biosystems.mew.core.mew.simulation.tdps;

import static org.junit.Assert.assertArrayEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractObjTerm;
import pt.uminho.ceb.biosystems.mew.solvers.builders.CPLEX3SolverBuilder;
import pt.uminho.ceb.biosystems.mew.solvers.lp.CplexParamConfiguration;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

public class TDPS2TestClean {
	
	protected static double[] results = null;
	protected static String[] netconv = new String[]{"M_ala_L_e","M_h2o_e","M_co2_e","M_ser_L_e","M_pi_e","R_biomass_SC5_notrace","M_glc_D_e","M_h_e","M_so4_e","M_pro_L_e","M_glyc_e","M_nh4_e","M_o2_e","M_ttdca_e","M_thym_e","M_3hp_c","M_ac_e","OF"};
	
	@BeforeClass
	public static void load(){
		results = getResults();
	}
	
	@Test
	//iMM904 case study
	public void loadModelImportViaAndSaveModelR() throws Exception {
		
		AbstractObjTerm.setMaxValue(100000000);
		AbstractObjTerm.setMinValue(-100000000);
		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-6);
		CplexParamConfiguration.setDoubleParam("EpMrk", 0.01);
		CplexParamConfiguration.setDoubleParam("EpInt", 1e-5);
		CplexParamConfiguration.setBooleanParam("NumericalEmphasis", true);
		
		String bimassReaction = "R_biomass_SC5_notrace";
		
		String fileTest = "/home/pmaia/ownCloud/documents/TDPS/iMM904_2.0_sc1000_3HP_new_pvilaca.xml";
		
		JSBMLReader readerTest = new JSBMLReader(fileTest, "");
		Container containerTest = new Container(readerTest);
		Set<String> toRemTest = containerTest.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		containerTest.removeMetaboliteAndItsReactions(toRemTest);
		
		ISteadyStateModel modelTest = ContainerConverter.convert(containerTest);
		modelTest.setBiomassFlux(bimassReaction);
		
		Set<String> metabolites2remove = CollectionUtils.parseSeparetedStringSet(
				"M_coa_c M_coa_m M_h2o_x M_h2o_e M_h2o_c M_h2o_m M_h2o_v M_h2o_r M_h2o_n M_h_e M_h_c M_h_m M_h_r M_h_v M_h_n M_so4_e M_so4_c M_pi_e M_pi_c M_pi_m M_nh4_e M_nh4_c M_nh4_m M_o2_e M_o2_c M_o2_m M_o2_r",
				" ");
		
		SimulationSteadyStateControlCenter ccTest = new SimulationSteadyStateControlCenter(null, null, modelTest, SimulationProperties.TDPS2);
		ccTest.setSolver(CPLEX3SolverBuilder.ID);
		ccTest.addProperty(SimulationProperties.TDPS_PENALTY, 50.0);
		ccTest.addProperty(SimulationProperties.TDPS_REMOVE_METABOLITES, metabolites2remove);
		ccTest.setWTReference(getReference("/home/pmaia/ownCloud/documents/TDPS/results.txt"));

		
		Map<String,Double> exprs = new HashMap<String,Double>();
		exprs.put("R_PYRDC", 2.0);
		exprs.put("R_ACS", 2.0);
		exprs.put("R_ALDD2x", 2.0);
		exprs.put("R_PYRt2m", 0.5);
		exprs.put("R_MCR", 2.0);
		exprs.put("R_ACCOACr", 2.00);
		GeneticConditions gc = new GeneticConditions(new ReactionChangesList(exprs));
		ccTest.setGeneticConditions(gc);
		
		ccTest.addProperty(SimulationProperties.DEBUG_SOLVER_MODEL, "/home/pmaia/ownCloud/documents/TDPS/export_models/TDPS_mew.mps");
		SteadyStateSimulationResult result = ccTest.simulate();
		
		Map<String,Double> netconversionMap = result.getNetConversionMap(true);
		netconversionMap.put("OF", result.getOFvalue());
		System.out.println(result.getOFvalue());
		MapUtils.prettyPrint(netconversionMap);
				
		
//		double[] thisres = new double[results.length];
//		for(int i=0; i<netconv.length;i++){
//			String r = netconv[i];
//			double val = netconversionMap.get(r);
//			thisres[i] = val;
//		}
//		
		double[] thisres = new double[results.length];
		for(int i=0; i<netconv.length;i++){
			String r = netconv[i];
			Double val = netconversionMap.get(r);
			thisres[i] = (val==null)?0.0:netconversionMap.get(r);
		}
		
		
		assertArrayEquals(results, thisres, 1e-4);
	}
	
	public FluxValueMap getReference(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		FluxValueMap map = new FluxValueMap();
		
		while (br.ready()) {
			String line = br.readLine();
			if (!line.isEmpty()) {
				String[] tokens = line.split("\t");
				if (tokens.length > 1) {
					String reac = tokens[0].trim();
					Double val = Double.parseDouble(tokens[1].trim());
					map.put(reac, val);
				}
			}
		}
		br.close();
		
		return map;
	}
	
	public static double[] getResults(){
		double [] res = new double[]{0.008328926767980721,3.46375497263792,2.679113694180263,0.011825219560817522,-0.012574051610250159,0.06360167734232831,-1.15,0.9255314727504371,-0.004916409658562097,0.002768004734997614,0.0030533699798469837,-0.38010730720543506,-2.5803927605131416,0.009458514364276069,6.204564816178139E-4,0.5719762848748636,-2.1094237467877974E-15,253275.87384354815};
		return res;
	}
	
}
