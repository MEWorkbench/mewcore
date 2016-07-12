package pt.uminho.ceb.biosystems.mew.core.balance;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.ContainerUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.ErrorsException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry.MetaboliteFormula;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.JSBMLValidationException;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.exceptions.InvalidSteadyStateModelException;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;


public class MyFirstSteadyStateModelLoad {
	
	public static void main(String[] args) throws FileNotFoundException, XMLStreamException, ErrorsException, IOException, ParserConfigurationException, SAXException, JSBMLValidationException, InvalidSteadyStateModelException {
		// Ler ficheiro xml
		//-----------------------
		String filePath = "C:\\workspace_java\\mewcore\\src\\test\\resources\\models\\ecoli_core_model.xml";
		String organismName = "MyOrganism";
		
		JSBMLReader myReader = new JSBMLReader(filePath, organismName);
		//-----------------------

		// Colocar informacao do ficheiro no container
		//--------------------------------
		// Container
		
		Container myFirstContainer = new Container(myReader);
		//-------------------------------
		
		Set<String> met = myFirstContainer.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		myFirstContainer.removeMetabolites(met);
		SteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(myFirstContainer);
		//--------------------------------
				
		System.out.println(model.getBiomassFlux());
		System.out.println();

		//--------------------------------
		// Matriz S (MxR)
		DoubleMatrix2D s = model.getStoichiometricMatrix().convertToColt();
		System.out.println(s.toString()); //72x95 matrix
		System.out.println();
		System.out.println("Numero de reações: " + model.getReactions().size());//95
		System.out.println("Numero de metabolitos: " + model.getMetabolites().size());//72
		System.out.println();
		
		// Mostrar matriz
		for (int i=0; i<model.getMetabolites().size(); i++) {
			for (int j=0; j<model.getReactions().size(); j++){
				System.out.print(s.get(i, j)+" ");				
			}System.out.println();
		}		
		System.out.println();
		//--------------------------------
				
		//Exemplo
		System.out.println("MetabolitoID-Elementos-NºElementos-Formula-Carga");
		DoubleMatrix2D x = getMetaboliteFormulasCharge(myFirstContainer);
		System.out.println();
		//--------------------------------
		
		//Todos os elementos quimicos
		Set<String> allComp = getAllComp(myFirstContainer);
		System.out.println("Elementos quimicos dos metabolitos: ");
		System.out.println(allComp.toString());
		System.out.println();
		
		//Todos os id dos metabolitos
		Set<String> allMetabolite = getAllMetabolite(myFirstContainer);
		System.out.println("id dos metabolitos: ");
		System.out.println(allMetabolite.toString());
		System.out.println();
		
		//Matriz M (MxC)
		int nlin = allMetabolite.size();
		int ncol = allComp.size();
		System.out.println("M matrix: "+nlin+"x"+ncol);
		
		double [][] matM = matrixM(myFirstContainer, model);
		
		for (int lin = 0; lin<nlin; lin++){
			for (int col=0; col<ncol; col++){
				System.out.print(matM[lin][col]+" ");
			}
			System.out.println();
		}
		//--------------------------------
		//Matriz R (RxC)
		SparseDoubleMatrix2D matrixM = new SparseDoubleMatrix2D(matM);
		Algebra a = new Algebra();
		DoubleMatrix2D matR = a.mult(s.viewDice(), matrixM);//transposta de S x matrizM em formato esparsa

		System.out.println("R matrix: " + matR.rows()+"x"+ matR.columns());
		System.out.println(matR.toString());
		System.out.println();
		
		//--------------------------------
		//Verificar Balanceamento
		ReactionCI rci = myFirstContainer.getReaction( model.getReactionId(66));
		Set<String> metabolites = rci.getMetaboliteSetIds();
		
		System.out.println(ContainerUtils.getReactionToString(rci));
		System.out.println();
		
		for(String id : metabolites){
			System.out.println(myFirstContainer.getMetabolite(id).getFormula());
		}
		System.out.println();
		
		System.out.println(s.viewColumn(66));
		
		System.out.println(matrixM.viewRow(3));
		System.out.println(matrixM.viewRow(4));
		System.out.println();
		
		//indices das reacoes na matrix R onde estão os drains
		Set <String> reationIDsToRemove= new HashSet<>();
		System.out.println("Drains position:");
		for(String drainId : myFirstContainer.getDrains()){
			reationIDsToRemove.add(drainId);
			System.out.print(model.getReactionIndex(drainId)+" ");
		}
		System.out.println();
		System.out.println("DrainIDs: " + reationIDsToRemove.size());
		System.out.println(reationIDsToRemove);
		System.out.println();
		
		SparseDoubleMatrix2D matResult = filterMatrix(matR, reationIDsToRemove, model);
		System.out.println(matResult);
	}
	//--------------------------------
	//METODOS
		
	static public SparseDoubleMatrix2D filterMatrix(DoubleMatrix2D matrix, Set<String> reactionIdsToRemove, SteadyStateModel model){
		
		int [] columnInd = new int [matrix.columns()];
		int [] rowInd = new int [matrix.rows()- reactionIdsToRemove.size()];
		Set <Integer> row = new HashSet<>();
		Set <Integer> drainsInd = new HashSet<>();
		
		for (String drainId : reactionIdsToRemove){
			drainsInd.add(model.getReactionIndex(drainId));
		}
		for (int i =0; i < matrix.rows(); i++){
			row.add(i);
		}
		
		row.removeAll(drainsInd);
		
		int index = 0;
		for( Integer i : row ) {
		  rowInd[index++] = i; 
		}
		int j=0;
		for (int i =0; i < matrix.columns(); i++){
			columnInd[j++]=i;
		}
		
		double [][] mat = new double [matrix.rows()- reactionIdsToRemove.size()][matrix.columns()];
		mat =matrix.viewSelection(rowInd, columnInd).toArray();
		SparseDoubleMatrix2D matEnd = new SparseDoubleMatrix2D(mat);
		
		return matEnd;
	}
	//marR.viewSelection(arg0, arg1)
	
	//Guardar todos os elementos quimicos dos metabolitos
	static public Set<String> getAllComp(Container c){
		Set<String> allcomp = new HashSet<>();
		
		for( String id : c.getMetabolites().keySet()){
			MetaboliteCI metabolites = c.getMetabolite(id);
			String formula = metabolites.getFormula();
			MetaboliteFormula metFormula = new MetaboliteFormula(formula);
			for (String elem : metFormula.getElements().keySet()){
				allcomp.add(elem);
			}
		}
		return allcomp;
	}
	
	static public Double getNumberElem (String formula, String elem ){
		MetaboliteFormula metFormula = new MetaboliteFormula(formula);
		metFormula.getElements().get(elem);
		return new Double(metFormula.getElements().get(elem));
	}
	
	//Guardar o id de todos os metabolitos
	static public Set<String> getAllMetabolite(Container c){
		Set<String> allmetabolites = new HashSet<>();
		
		for( String id : c.getMetabolites().keySet()){
			allmetabolites.add(id);		
		}
		return allmetabolites; 
	}
	//Obter o numero de cada elemento da formula para cada metabolito 
	static public Double getNumberElem (String metabolite, String elem, Container c ){
		MetaboliteCI met = c.getMetabolite(metabolite);
		String formula =met.getFormula();
		MetaboliteFormula metFormula = new MetaboliteFormula(formula);
		Map<String, Integer> mapFormula = metFormula.getElements();
		if (mapFormula.containsKey(elem)){
			return mapFormula.get(elem).doubleValue();						
		}
		return 0.0;	
	}
	//Matriz M (MxC)
	static public double[][] matrixM (Container c, SteadyStateModel model){
		Map<String, Integer> localComp = new HashMap<String, Integer>();
		Map<String, Integer> localMet = new HashMap<String, Integer>();
		Set<String> allcomp = getAllComp(c);
		
		double [][] matM = new double [model.getMetabolites().size()][allcomp.size()];

		int i = 0;
		for (String elem: allcomp){
			localComp.put(elem, i);
			i++;
		}
		//System.out.println(localComp.toString());		
		
		for (int j =0; j < model.getMetabolites().size(); j++){
			localMet.put(model.getMetaboliteId(j), j);
			for (String elem : localComp.keySet()) {
				Integer indComp = localComp.get(elem);
				matM[j][indComp] = getNumberElem (model.getMetaboliteId(j), elem, c); 
			}
		}
	return matM;
	}
	
	
	//Exemplo.: id : elementos : numElementos : formula carga 
	static public DoubleMatrix2D getMetaboliteFormulasCharge(Container c){
		for( String id : c.getMetabolites().keySet()){
			System.out.print(id + ": ");
			MetaboliteCI metabolite = c.getMetabolite(id);
			String met = metabolite.getId();
			String formula=metabolite.getFormula();
			Integer charge=metabolite.getCharge();			
			MetaboliteFormula metFormula= new MetaboliteFormula(formula);			
			System.out.print(metFormula.getElements().keySet()+ ";");			
			System.out.print(metFormula.getElements().values()+ ";");			
			System.out.println(formula+";"+charge);			
		}
		return null; 
	}
	

}
