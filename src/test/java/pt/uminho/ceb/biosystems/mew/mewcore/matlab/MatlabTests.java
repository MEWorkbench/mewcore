package pt.uminho.ceb.biosystems.mew.mewcore.matlab;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.connection.matlab.MatlabConnection;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.formulations.cobra.simulation.CobraFBAFormulation;
import pt.uminho.ceb.biosystems.mew.mewcore.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.gpr.SteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;

public class MatlabTests {
	
	private String getFile(String fileName){
		URL nyData = getClass().getClassLoader().getResource(fileName);
		return nyData.getFile();
	}

	SteadyStateModel model;
	
	@Before
	public void init() throws Exception{
		//JSBMLReader reader = new JSBMLReader("files/models/Ec_iJR904.xml", "1",false);
		JSBMLReader reader = new JSBMLReader(getFile("models/ecoli_core_model.xml"), "1",false);
		
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));

		cont.removeMetabolites(met);
		model = (SteadyStateModel) ContainerConverter.convert(cont);
		
//		CobraMatlabConnection conn = new CobraMatlabConnection();
//		conn.init();
	}
	
	@Test
	public void matlabTest() throws Exception
	{
		System.out.println(System.getProperty("java.class.path"));
		MatlabConnection conn = new MatlabConnection();
		conn.init();
		
		CobraFBAFormulation fba = new CobraFBAFormulation(model);
		fba.setProperty(SimulationProperties.IS_MAXIMIZATION, true);
		fba.simulate();
	}
	
	//@Test
	public void modelTest() throws Exception{
		
		SteadyStateGeneReactionModel modelGR = (SteadyStateGeneReactionModel) model;
//		if (model.getClass().isAssignableFrom(SteadyStateGeneReactionModel.class)) {
//			System.out.println("SIM");
//			
//		}
		
//		System.out.println(modelGR.getGeneReactionRules().size());
	
		
		List<String> list = new ArrayList<String>();
		
		list.addAll( modelGR.getReactions().keySet());
		java.util.Collections.sort(list);
		String grRules = "model.grRules = {";
		for (int i = 0; i < list.size(); i++) {
			grRules+= "'";
			//System.out.println(list.get(i));
			if(modelGR.getGeneReactionRule(list.get(i)) != null)
				
				grRules+= modelGR.getGeneReactionRule(list.get(i)).getRule().toString();
			else
				grRules+= "";
			grRules+= "';";
		}
		grRules = grRules.substring(0, grRules.length()-1);
		grRules+= "};";
		
		System.out.println(grRules);
		
		
		System.out.println("model.genes = ");
		
		System.out.println(modelGR.getGenes().keySet());
		
		
//		for (String rxn : modelGR.getReactions().keySet()) {
//			if(modelGR.getGeneReactionRule(rxn) != null)
//				System.out.println(modelGR.getGeneReactionRule(rxn).getRule());
//			else
//				System.out.println();
//		}
		
//		for (int i =0; i<modelGR.getGeneReactionRules().size(); i++) {
//			System.out.println(modelGR.getGeneReactionRules().getValueAt(i).getRule());
//		}
	}
	

}
