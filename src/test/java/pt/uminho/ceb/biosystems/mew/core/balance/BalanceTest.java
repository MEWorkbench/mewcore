package pt.uminho.ceb.biosystems.mew.core.balance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.ContainerUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.solvers.builders.CPLEXSolverBuilder;

public class BalanceTest {
	
	private static SteadyStateModel model;
	private static Container myFirstContainer;

	private static String getFile(String fileName){
		return BalanceTest.class.getClassLoader().getResource(fileName).getFile();
	}

	@BeforeClass
	public static void createContAndModel() throws Exception{
//		String f = getFile("models/ecoli_core_model.xml");
		System.out.println(getFile("models/ecoli_core_model.xml"));
		String filePath = getFile("models/ecoli_core_model.xml");
		String organismName = "MyOrganism";
		
		JSBMLReader myReader = new JSBMLReader(filePath, organismName);
		
		//--------------------------------
		
		// Container - Colocar informacao do ficheiro no container
		myFirstContainer = new Container(myReader);
		myFirstContainer.getMetabolites().put("biomass", new MetaboliteCI("biomass", "biomass"));
		myFirstContainer.getReaction(myFirstContainer.getBiomassId()).getProducts().put("biomass", new StoichiometryValueCI("biomass", 1.0, "C_c"));
		myFirstContainer.verifyDepBetweenClass();
		
		myFirstContainer.removeReaction(myFirstContainer.getBiomassId());
			
		Set<String> met = myFirstContainer.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		myFirstContainer.removeMetabolites(met);
		
		//-------------------------------		
		model = (SteadyStateModel) ContainerConverter.convert(myFirstContainer);
		
		/**
		//metabolits - pentose phosphate pathway
		myFirstContainer.getMetabolite("M_g6p_c").setFormula("");
		myFirstContainer.getMetabolite("M_6pgl_c").setFormula("");
		myFirstContainer.getMetabolite("M_h2o_c").setFormula("");
		myFirstContainer.getMetabolite("M_h_c").setFormula("");
		myFirstContainer.getMetabolite("M_6pgc_c").setFormula("");
		myFirstContainer.getMetabolite("M_co2_c").setFormula("");
		myFirstContainer.getMetabolite("M_ru5p_D_c").setFormula("");
		myFirstContainer.getMetabolite("M_nadp_c").setFormula("");
		myFirstContainer.getMetabolite("M_nadph_c").setFormula("");
		myFirstContainer.getMetabolite("M_r5p_c").setFormula("");
		myFirstContainer.getMetabolite("M_xu5p_D_c").setFormula("");
		myFirstContainer.getMetabolite("M_s7p_c").setFormula("");
		myFirstContainer.getMetabolite("M_f6p_c").setFormula("");
		myFirstContainer.getMetabolite("M_e4p_c").setFormula("");
		myFirstContainer.getMetabolite("M_g3p_c").setFormula("");**/
		
		
		/**Set <String> ids = new HashSet<>();
		ids.add("M_q8_c");
		ids.add("M_accoa_c");
		ids.add("M_atp_c");
		ids.add("M_nadph_c");
		ids.add("M_nadh_c");**/
		for (String id : myFirstContainer.getMetabolites().keySet()){
			//if( !myFirstContainer.getDrainToMetabolite().containsValue(id) && ids.contains(id)==false){
			if( !myFirstContainer.getDrainToMetabolite().containsValue(id)){
				myFirstContainer.getMetabolite(id).setFormula("");
				//System.out.println(id +" ==>> removed" );
			}
				
		}
				
		//myFirstContainer.getMetabolite("M_etoh_c").setFormula("");
		//myFirstContainer.getMetabolite("M_acald_c").setFormula("");
	}
	
	@Test
	public void newCode() throws Exception{
		
		//20 DRAINS
		Set <String> drainsIDsToRemove= new HashSet<>();
		Map <String, Integer> drains = new HashMap<>();
		for(String drainId : myFirstContainer.getDrains()){
			drainsIDsToRemove.add(drainId);
			drains.put(drainId, model.getReactionIndex(drainId));
		}
		System.out.println("Drain : ReactionIndex");
		System.out.println(drains);
		System.out.println("Numero de Drains: " + drainsIDsToRemove.size());
		System.out.println();
		//-------------
		//matriz S
		Smatrixbuilder sbuilder = new Smatrixbuilder(model);
		IndexedSparseColtMatrix s = sbuilder.buildMatrix();
		s.remmoveCol(drainsIDsToRemove);
		
		//matriz M
		Map<String,String> mapMetaboliteFormulas = getMapMetaboliteFormulas (myFirstContainer);
		Mmatrixbuilder mbuilder = new Mmatrixbuilder(model, mapMetaboliteFormulas);
		IndexedSparseColtMatrix m = mbuilder.buildMatrix();
		
		//matriz R
		System.out.println("Matrix R: "); //95x6
		BalanceChecker balance = new BalanceChecker(s, m, CPLEXSolverBuilder.ID);
		balance.matrixR.printMatrix();	
		System.out.println("Indices dos compostos (colunas):");
		System.out.println(balance.matrixR.getColIndex());
		System.out.println("Indices das reações (linhas):");
		System.out.println(balance.matrixR.getRowIndex());
		System.out.println("checkbalance: "+ balance.checkbalance());
		System.out.println("linhas zero: "+ balance.matrixR.getZeroRows());
		System.out.println("colunas zero: "+ balance.matrixR.getZeroColumns());		
		System.out.println("unbalanced IdsReactions: "+balance.unbalancedIdsReactions());
		System.out.println("met sem formula: "+balance.getMetsWithoutFormula());
		System.out.println("met sem formula: "+balance.getMetsWithoutFormula().size());
		System.out.println();
		
		//----------------------------------------------------------------
				
		//CASO EXISTAM METABOLITOS SEM FORMULA
		Set<String> reactionsIDsToRemove = new HashSet<>(myFirstContainer.getReactions().keySet());
		Set<String>[] mets = getIdsMetWithAndWithoutFormula(mapMetaboliteFormulas);//[0]-Without [1]-With
		Set<String> keepReactions = getReactionsWithMetabolites(myFirstContainer, mets[0]);
		reactionsIDsToRemove.removeAll(keepReactions);
		System.out.println();
				
		System.out.println("Reações em que M_etoh_c participa:");
		System.out.println(keepReactions);
		System.out.println(ContainerUtils.getReactionToString(myFirstContainer.getReaction("R_ETOHt2r")));
		System.out.println(ContainerUtils.getReactionToString(myFirstContainer.getReaction("R_ALCD2x")));
		System.out.println();
		System.out.println("Formula de M_etoh_e: "+myFirstContainer.getMetabolite("M_etoh_e").getFormula());
		System.out.println("Formula de M_acald_c: "+myFirstContainer.getMetabolite("M_acald_c").getFormula());
		System.out.println("Formula de M_nad_c: "+myFirstContainer.getMetabolite("M_nad_c").getFormula());
		System.out.println("Formula de M_nadh_c: "+myFirstContainer.getMetabolite("M_nadh_c").getFormula());
		System.out.println();
	
		System.out.println("Resultado do sistema:");
		System.out.println(balance.balance());
		System.out.println(balance.balance().size());
		
	}
	
	@Test
	public void oldCode(){
		//indices das reacoes onde estão os 20 DRAINS
		Set <String> drainsIDsToRemove= new HashSet<>();
		Map <String, Integer> drains = new HashMap<>();
		for(String drainId : myFirstContainer.getDrains()){
			drainsIDsToRemove.add(drainId);
			drains.put(drainId, model.getReactionIndex(drainId));
		}
		System.out.println("Drain : ReactionIndex");
		System.out.println(drains);
		System.out.println("Numero de Drains: " + drainsIDsToRemove.size());
		System.out.println();
		
		//----------------------------------------------------------------
		
		//Matriz S
		System.out.println("Matrix S:"); //72x95
		Smatrixbuilder sbuilder = new Smatrixbuilder(model);
		IndexedSparseColtMatrix s = sbuilder.buildMatrix();
		s.printMatrix();
		System.out.println("Indices das reações (colunas):");
		System.out.println(s.getColIndex());
		System.out.println("Indices dos metabolitos (linhas):");
		System.out.println(s.getRowIndex());
		System.out.println();

		//Matriz M 
		Map<String,String> mapMetaboliteFormulas = getMapMetaboliteFormulas (myFirstContainer);
				
		System.out.println("Matrix M:"); //72x6
		Mmatrixbuilder mbuilder = new Mmatrixbuilder(model, mapMetaboliteFormulas);
		IndexedSparseColtMatrix m = mbuilder.buildMatrix();
		m.printMatrix();
		System.out.println("Indices dos compostos (colunas):");
		System.out.println(m.getColIndex());
		System.out.println("Indices dos metabolitos (linhas):");
		System.out.println(m.getRowIndex());
		System.out.println();
		
		//Matriz R
		System.out.println("Matrix R:"); //95x6
		IndexedSparseColtMatrix r = s.transpose().mult(m);
		r.printMatrix();
		System.out.println("Indices dos compostos (colunas):");
		System.out.println(r.getColIndex());
		System.out.println("Indices das reações (linhas):");
		System.out.println(r.getRowIndex());
		System.out.println("checkbalance: "+checkbalance(r));
		System.out.println("unbalanced IdsReactions: "+unbalancedIdsReactions(r));
		System.out.println();
		
		//Matriz R sem DRAINS
		System.out.println("Matrix R sem drains:");
		r.remmoveRows(drainsIDsToRemove);
		r.printMatrix();
		System.out.println(r.getRowIndex());
		System.out.println(r.getColIndex());
		System.out.println("checkbalance: "+checkbalance(r));
		System.out.println("unbalanced IdsReactions: "+unbalancedIdsReactions(r));
		System.out.println();
		
		//Matriz R sem biomassa 
		System.out.println("Matrix R sem biomassa:");
		Set <String> BiomassIDToRemove= new HashSet<>();
		BiomassIDToRemove.add("R_Biomass_Ecoli_core_w_GAM");
		r.remmoveRows(BiomassIDToRemove);
		r.printMatrix();
		System.out.println("checkbalance: "+checkbalance(r));
		System.out.println("unbalanced IdsReactions: "+unbalancedIdsReactions(r));
		System.out.println();

	}
	
	public static void main(String[] args) throws Exception {
		
		String filePath = getFile("models/ecoli_core_model.xml");
		String organismName = "MyOrganism";
		
		JSBMLReader myReader = new JSBMLReader(filePath, organismName);
		
		//--------------------------------
		
		// Container - Colocar informacao do ficheiro no container
		Container myFirstContainer = new Container(myReader);
			
		Set<String> met = myFirstContainer.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		myFirstContainer.removeMetabolites(met);
		
		//-------------------------------		
		SteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(myFirstContainer);
		
		for (String id : myFirstContainer.getMetabolites().keySet()){
			myFirstContainer.getMetabolite(id).setFormula("");
			
		}
		
		myFirstContainer.getMetabolite("M_etoh_c").setFormula("");
		myFirstContainer.getMetabolite("M_for_c").setFormula("");
		
		//-------------------------------
		
				
		//TEST
			
		//indices das reacoes onde estão os 20 DRAINS
		Set <String> drainsIDsToRemove= new HashSet<>();
		Map <String, Integer> drains = new HashMap<>();
		for(String drainId : myFirstContainer.getDrains()){
			drainsIDsToRemove.add(drainId);
			drains.put(drainId, model.getReactionIndex(drainId));
		}
		System.out.println("Drain : ReactionIndex");
		System.out.println(drains);
		System.out.println("Numero de Drains: " + drainsIDsToRemove.size());
		System.out.println();
		
		//----------------------------------------------------------------
		
		//Matriz S
		System.out.println("Matrix S:"); //72x95
		Smatrixbuilder sbuilder = new Smatrixbuilder(model);
		IndexedSparseColtMatrix s = sbuilder.buildMatrix();
		s.printMatrix();
		System.out.println("Indices das reações (colunas):");
		System.out.println(s.getColIndex());
		System.out.println("Indices dos metabolitos (linhas):");
		System.out.println(s.getRowIndex());
		System.out.println();

		//Matriz M 
		Map<String,String> mapMetaboliteFormulas = getMapMetaboliteFormulas (myFirstContainer);
				
		System.out.println("Matrix M:"); //72x6
		Mmatrixbuilder mbuilder = new Mmatrixbuilder(model, mapMetaboliteFormulas);
		IndexedSparseColtMatrix m = mbuilder.buildMatrix();
		m.printMatrix();
		System.out.println("Indices dos compostos (colunas):");
		System.out.println(m.getColIndex());
		System.out.println("Indices dos metabolitos (linhas):");
		System.out.println(m.getRowIndex());
		System.out.println();
		
		//Matriz R
		System.out.println("Matrix R:"); //95x6
		IndexedSparseColtMatrix r = s.transpose().mult(m);
		r.printMatrix();
		System.out.println("Indices dos compostos (colunas):");
		System.out.println(r.getColIndex());
		System.out.println("Indices das reações (linhas):");
		System.out.println(r.getRowIndex());
		System.out.println("checkbalance: "+checkbalance(r));
		System.out.println("unbalanced IdsReactions: "+unbalancedIdsReactions(r));
		System.out.println();
		
		//Matriz R sem DRAINS
		System.out.println("Matrix R sem drains:");
		r.remmoveRows(drainsIDsToRemove);
		r.printMatrix();
		System.out.println(r.getRowIndex());
		System.out.println(r.getColIndex());
		System.out.println("checkbalance: "+checkbalance(r));
		System.out.println("unbalanced IdsReactions: "+unbalancedIdsReactions(r));
		System.out.println();
		
		//Matriz R sem biomassa 
		System.out.println("Matrix R sem biomassa:");
		Set <String> BiomassIDToRemove= new HashSet<>();
		BiomassIDToRemove.add("R_Biomass_Ecoli_core_w_GAM");
		r.remmoveRows(BiomassIDToRemove);
		r.printMatrix();
		System.out.println("checkbalance: "+checkbalance(r));
		System.out.println("unbalanced IdsReactions: "+unbalancedIdsReactions(r));
		System.out.println();
		
		//----------------------------------------------------------------
				
		//Matriz R usando classe BalanceChecker
		System.out.println("Matrix R usando classe BalanceChecker: "); //95x6
		BalanceChecker balance = new BalanceChecker(s, m, CPLEXSolverBuilder.ID);
		balance.matrixR.printMatrix();	
		System.out.println("Indices dos compostos (colunas):");
		System.out.println(balance.matrixR.getColIndex());
		System.out.println("Indices das reações (linhas):");
		System.out.println(balance.matrixR.getRowIndex());
		System.out.println("checkbalance: "+ balance.checkbalance());
		System.out.println("linhas zero: "+ balance.matrixR.getZeroRows());
		System.out.println("colunas zero: "+ balance.matrixR.getZeroColumns());		
		System.out.println("unbalanced IdsReactions: "+balance.unbalancedIdsReactions());
		System.out.println("met sem formula: "+balance.getMetsWithoutFormula());
		System.out.println();
		
		//----------------------------------------------------------------
				
		//CASO EXISTAM METABOLITOS SEM FORMULA
		Set<String> reactionsIDsToRemove = new HashSet<>(myFirstContainer.getReactions().keySet());
		Set<String>[] mets = getIdsMetWithAndWithoutFormula(mapMetaboliteFormulas);//[0]-Without [1]-With
		Set<String> keepReactions = getReactionsWithMetabolites(myFirstContainer, mets[0]);
		System.out.println(keepReactions);
		reactionsIDsToRemove.removeAll(keepReactions);
		System.out.println();
			
		//System.out.println("Matriz S filtrada do sistema:");
		//s.remmoveCol(reactionsIDsToRemove);
		//s.remmoveRows(mets[1]);
		//s.transpose().printMatrix();
				
		System.out.println("Reações em que M_etoh_c participa:");
		System.out.println(ContainerUtils.getReactionToString(myFirstContainer.getReaction("R_ETOHt2r")));
		System.out.println(ContainerUtils.getReactionToString(myFirstContainer.getReaction("R_ALCD2x")));
		System.out.println();
		System.out.println("Formula de M_etoh_e: "+myFirstContainer.getMetabolite("M_etoh_e").getFormula());
		System.out.println("Formula de M_nad_c: "+myFirstContainer.getMetabolite("M_nad_c").getFormula());
		System.out.println("Formula de M_acald_c: "+myFirstContainer.getMetabolite("M_acald_c").getFormula());
		System.out.println("Formula de M_nadh_c: "+myFirstContainer.getMetabolite("M_nadh_c").getFormula());
		System.out.println();
		
		System.out.println(r);
		System.out.println(r.getMatrix());
		System.out.println(r.getColIndex());
		
		System.out.println("Nº de C do M_etoh_c na reação R_ALCD2x [0]: "+r.getMatrix().getQuick(r.getRowIndex().get("R_ALCD2x"), r.getColIndex().get("C")));
		System.out.println("Nº de C do M_etoh_c na reação R_ETOHt2r [1]: "+r.getMatrix().getQuick(r.getRowIndex().get("R_ETOHt2r"),r.getColIndex().get("C")));
		System.out.println();
		
		System.out.println("Reações em que M_for_c participa:");
		System.out.println(ContainerUtils.getReactionToString(myFirstContainer.getReaction("R_FORti")));
		System.out.println(ContainerUtils.getReactionToString(myFirstContainer.getReaction("R_PFL")));
		System.out.println();
		System.out.println("Formula de M_for_e: "+myFirstContainer.getMetabolite("M_for_e").getFormula());
		System.out.println("Formula de M_coa_c: "+myFirstContainer.getMetabolite("M_coa_c").getFormula());
		System.out.println("Formula de M_pyr_c: "+myFirstContainer.getMetabolite("M_pyr_c").getFormula());
		System.out.println("Formula de M_accoa_c: "+myFirstContainer.getMetabolite("M_accoa_c").getFormula());
		System.out.println();
		
		//Componentes a romover para descobrir só os carbonos
		//Set <String> compToRemove= new HashSet<>();
		//compToRemove.addAll(m.getColIndex().keySet());
		//compToRemove.remove("C");
		
		System.out.println("Matriz v do sistema:");
		balance.balance();
		balance.matrixC.printMatrix(); 
		System.out.println();
		System.out.println(balance.matrixC.getColIndex());
		System.out.println(balance.matrixC.getRowIndex());
		System.out.println();
		System.out.println(balance.getUnknownFormulas());
		
	}
	
	//Ids de metabolitos sem formula [0] e com formula [1]
	static public Set<String>[] getIdsMetWithAndWithoutFormula(Map <String,String> metaboliteFormulas){ //map <idMetabolito,Formula>
		Set<String>[] idsWithAndWithoutFormula = new HashSet[2];
		idsWithAndWithoutFormula[0] = new HashSet<>();
		idsWithAndWithoutFormula[1] = new HashSet<>();
		
		for (String idMet: metaboliteFormulas.keySet()){
			if(metaboliteFormulas.get(idMet)==null || metaboliteFormulas.get(idMet).equals("")){
				idsWithAndWithoutFormula[0].add(idMet);
			}else{
				idsWithAndWithoutFormula[1].add(idMet);
			}
		}
		return idsWithAndWithoutFormula;
	}
	
	//Ids de metabolitos com formula, faço isto na anterior
	static public Set<String> getIdsMetWithFormula(Map <String,String> metaboliteFormulas){ //map <idMetabolito,Formula>
		Set<String> idsWithFormula = new HashSet<>();
		for (String idMet: metaboliteFormulas.keySet()){
			if(metaboliteFormulas.get(idMet)!=null){
				idsWithFormula.add(idMet);
			}
		}
		return idsWithFormula;
	}
	
	//todas as reações em que os metabolitos sem formula participam
	static public Set<String> getReactionsWithMetabolites(Container c , Set<String> mets){
		
		Set<String> reactions = new HashSet<>();
		
		for(String mId : mets){
			reactions.addAll(c.getMetabolite(mId).getReactionsId());
		}
		return reactions;
	}
	
	//verifica se a Matriz R está balanceada
	static public Boolean checkbalance(IndexedSparseColtMatrix r){
		
		Boolean resol = true;
		double [][] matR = new double [r.getRowIndex().size()][r.getColIndex().size()];
		
		for(int i = 0; i < matR.length; i++){
            for(int j = 0; j < matR[0].length; j++){
            	matR[i][j]=r.getMatrix().getQuick(i,j);
            	if (matR[i][j] != 0.0){
            		resol = false;
            		break;
            	}
            }
		}
		
		return resol;
	}
	
	//se a Matriz R não está balanceada, dá os ids das reação não balanceada
	static public Set<String> unbalancedIdsReactions(IndexedSparseColtMatrix r){
		
		Set <String> idsReactions= new HashSet<>();
		
		if(checkbalance(r)==false){
			
			double [][] matR = new double [r.getRowIndex().size()][r.getColIndex().size()];
			
			for(int i = 0; i < matR.length; i++){
	            for(int j = 0; j < matR[0].length; j++){
	            	matR[i][j]=r.getMatrix().getQuick(i,j);
	            	if (matR[i][j] != 0.0){
	            		for (String id:r.getRowIndex().keySet()){
	            			if (r.getRowIndex().get(id) == i){
	            				idsReactions.add(id);
	            				break;
	            			}
	            		}
	            		continue;
	            	}
	            }
			}			
		}
		
		
		return idsReactions;
	}
	
	
	//Map <idMetabolito,Formula> para construir matriz M 
	static public Map <String,String>  getMapMetaboliteFormulas(Container c){
		
		Map<String,String> map = new HashMap<>();
		
		for( String id : c.getMetabolites().keySet()){

			MetaboliteCI metabolite = c.getMetabolite(id);
			String met = metabolite.getId();
			String formula = metabolite.getFormula();
			if(formula == null) formula = "";
			map.put(met, formula);
		}
		return map; 
	}
	
}

