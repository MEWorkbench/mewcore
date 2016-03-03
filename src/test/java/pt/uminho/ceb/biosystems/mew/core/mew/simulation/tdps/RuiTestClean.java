package pt.uminho.ceb.biosystems.mew.core.mew.simulation.tdps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractObjTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.tdps.TDPS;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.tdps.TDPS_FBA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.tdps.TDPS_LMOMA;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.CplexParamConfiguration;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

public class RuiTestClean {
	
	@Test
	//iMM904 case study
	public void loadModelImportViaAndSaveModelR() throws Exception {
		
		AbstractObjTerm.setMaxValue(100000000);
		AbstractObjTerm.setMinValue(-100000000);
//		AbstractObjTerm.setMaxValue(Double.MAX_VALUE);
//		AbstractObjTerm.setMinValue(-Double.MAX_VALUE);
		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-6);
		CplexParamConfiguration.setDoubleParam("EpMrk", 0.01);
		CplexParamConfiguration.setDoubleParam("EpInt", 1e-5);
		CplexParamConfiguration.setBooleanParam("NumericalEmphasis", true);
		
		String bimassReaction = "R_biomass_SC5_notrace";
		
		String fileRef = "/home/pmaia/ownCloud/documents/TDPS/iMM904_2.0_testes1000_3HP.xml";
		String fileTest = "/home/pmaia/ownCloud/documents/TDPS/iMM904_2.0_sc1000_3HP.xml";
		
		JSBMLReader readerRef = new JSBMLReader(fileRef, "");
		Container containerRef = new Container(readerRef);
		Set<String> toRemRef = containerRef.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		containerRef.removeMetaboliteAndItsReactions(toRemRef);
		
		JSBMLReader readerTest = new JSBMLReader(fileTest, "");
		Container containerTest = new Container(readerTest);
		Set<String> toRemTest = containerTest.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		containerTest.removeMetaboliteAndItsReactions(toRemTest);
		
		ISteadyStateModel modelRef = ContainerConverter.convert(containerRef);
		ISteadyStateModel modelTest = ContainerConverter.convert(containerTest);
		modelTest.setBiomassFlux(bimassReaction);
		
		SimulationSteadyStateControlCenter ccRef = new SimulationSteadyStateControlCenter(null, null, modelRef, SimulationProperties.PFBA);
		ccRef.setMaximization(true);
		ccRef.setFBAObjSingleFlux(bimassReaction, 1.0);
		ccRef.setSolver(SolverType.CPLEX);
		
		SteadyStateSimulationResult reference = ccRef.simulate();
		BufferedWriter bw = new BufferedWriter(new FileWriter("/home/pmaia/ownCloud/documents/TDPS/reference_mine.txt"));
		for (String f : reference.getFluxValues().keySet()) {
			bw.write(f + "=" + reference.getFluxValues().getValue(f));
			bw.newLine();
		}
		
		bw.flush();
		bw.close();
		
		SimulationSteadyStateControlCenter ccTest = new SimulationSteadyStateControlCenter(null, null, modelTest, SimulationProperties.TDPS2);
		ccTest.setSolver(SolverType.CPLEX);
//		ccTest.setWTReference(reference.getFluxValues()); 
		
		ccTest.setWTReference(getReference("/home/pmaia/ownCloud/documents/TDPS/results.txt"));
		
		TDPS_FBA.OF_RELAX = 1.1;
		TDPS_FBA.Target = "R_biomass_SC5_notrace";
		TDPS_LMOMA.OF_RELAX = 1.1;
		
		TDPS.penalty = 50;
		//with oxygen + coa .... chemostat
		TDPS.removeMetabolites = CollectionUtils.parseSeparetedStringSet(
				"M_coa_c M_coa_m M_h2o_x M_h2o_e M_h2o_c M_h2o_m M_h2o_v M_h2o_r M_h2o_n M_h_e M_h_c M_h_m M_h_r M_h_v M_h_n M_so4_e M_so4_c M_pi_e M_pi_c M_pi_m M_nh4_e M_nh4_c M_nh4_m M_o2_e M_o2_c M_o2_m M_o2_r",
				" ");
				
		//Sol1
		TDPS.geneticModifications.put("R_PYRDC", 2.0);
		TDPS.geneticModifications.put("R_ACS", 2.0);
		TDPS.geneticModifications.put("R_ALDD2x", 2.0);
//		TDPS.geneticModifications.put("R_ACSm", 0.0);
		TDPS.geneticModifications.put("R_PYRt2m", 0.5);
		TDPS.geneticModifications.put("R_MCR", 2.0);
		TDPS.geneticModifications.put("R_ACCOACr", 2.00);
		
//		
//		ER_FBA.geneticModifications = MORA.geneticModifications;
//		ER_LMOMA.geneticModifications = MORA.geneticModifications;
//		ER_MIMBLE.geneticModifications = MORA.geneticModifications;
		
		SteadyStateSimulationResult result = ccTest.simulate();
		
		System.out.println(result.getOFvalue());
		MapUtils.prettyPrint(result.getNetConversionMap(true));
//		s.saveSimulationResultInFile("rui.txt");

//		MathContext mc = new MathContext(2, RoundingMode.UP);
//		BigDecimal bigDecimal = new BigDecimal(s.getFluxValue("R_biomass_SC5_notrace") / 0.207, mc);
//		
//		//3HP
//		BigDecimal bigDecimalP = new BigDecimal(((s.getFluxValue("R_EX_3hp_e_") * 90.08) / 207), mc);
//		System.out.println(bigDecimal + "/" + bigDecimalP);
//		
//		System.out.println(s.getSimulationResult().getOFvalue());
//		
//		System.out.println(s.printConsuming("M_4mop_c"));
	
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
	
}
