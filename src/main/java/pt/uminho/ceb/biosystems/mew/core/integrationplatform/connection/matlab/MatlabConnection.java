package pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.matlab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.converter.IConverter;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.exceptions.MatlabNotFoundException;
import pt.uminho.ceb.biosystems.mew.core.model.components.IStoichiometricMatrix;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.components.enums.ModelType;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.SteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.OverrideSteadyStateModel;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix2D;

public class MatlabConnection implements IConverter{
	
	private MatlabProxy proxy;
	ISteadyStateModel model;
	Map<String, Object> param;
	String modelName;

	public MatlabConnection() throws MatlabConnectionException, MatlabInvocationException
	{
	}
	
	public void init() throws MatlabConnectionException, MatlabInvocationException, MatlabNotFoundException{
		init(null);
	}
	
	public void init(MatlabProxy proxy) throws MatlabConnectionException, MatlabInvocationException, MatlabNotFoundException
	{
		MatlabProxy p;
		p = (proxy != null) ? proxy : MatlabProxySingleton.getInstance().getProxy();
		
		// Reconnect Proxy. Used when user closes MATLAB but still want to use
		// Tries three times but this could be a input parameter
		int i = 0;
		while(!p.isConnected() && i < 3){
			p = MatlabProxySingleton.getInstance().reconnectProxy();
			i++;
		}
		
		if(i==3)
			throw new MatlabNotFoundException();
		
		setProxy(p);
		
		// Verify if cobra is already initialized
		runCommand("global CBTLPSOLVER;" +
				" if(isempty(CBTLPSOLVER))" +
				" initCobraToolbox;" +
				" end;" +
				" clear CBTLPSOLVER;");
		
		runCommand("setenv('PATH','" + System.getenv("PATH") + "');");
			
	}
	
	public void sendModel(ISteadyStateModel model, String modelName) throws MatlabInvocationException, CommandExecutionException
	{
		this.model = model;
		this.modelName = modelName;
		
		try {
			createModel();
		} catch (Exception e) {
			throw new CommandExecutionException("Problem while creating model", e);
		}
	}
	
	public void sendObjectiveFunction(MapStringNum objectiveFunctionsList) throws MatlabInvocationException
	{
		String rxnNameList = "";
		String objectiveCoeff = "";
		for(String reacao: objectiveFunctionsList.keySet())
		{
			rxnNameList += "'" +reacao + "' "; 
			objectiveCoeff += objectiveFunctionsList.get(reacao) + " ";
		}

		runFunction("changeObjective",
				modelName+",{"+rxnNameList.substring(0, rxnNameList.length()-1)+"},["+objectiveCoeff.substring(0, objectiveCoeff.length()-1)+"]",
				modelName);
	}
	
	public void sendModifiedModel(OverrideSteadyStateModel overrideModel) throws MatlabInvocationException
	{		

		for(String rId : overrideModel.getOverriddenReactions()){
			ReactionConstraint rc = overrideModel.getReactionConstraint(rId);
			double lower = rc.getLowerLimit();
			double upper = rc.getUpperLimit();
			
//			getProxy().eval(modelName +" = changeRxnBounds("+modelName+",'"+ rId +"',"+ lower+", 'l');");
//			getProxy().eval(modelName +" = changeRxnBounds("+modelName+",'"+ rId +"',"+ upper+", 'u');");
//			
//			System.out.println(modelName +" = changeRxnBounds("+modelName+",'"+ rId +"',"+ lower+", 'l');");
//			System.out.println(modelName +" = changeRxnBounds("+modelName+",'"+ rId +"',"+ upper+", 'u');");
//			
			
			runFunction("changeRxnBounds", 
					modelName+",'"+ rId +"',"+ lower+", 'l'", 
					modelName);
			
			runFunction("changeRxnBounds", 
					modelName+",'"+ rId +"',"+ upper+", 'u'", 
					modelName);
		}
	}
	
	public void sendCollectionStrings(String id, Collection<?> t) throws CommandExecutionException{
		String array = id+" = {};";
		if(!t.isEmpty())
			array = id + " = { '"+CollectionUtils.join(t, "','")+"'};";
		runCommand(array);
	}

	public void sendCollectionStrings(String id, Collection<?> t, boolean isVertical) throws CommandExecutionException{
		String array = id+" = {};";
		if(!t.isEmpty()) 
			array = id + " = { '"+CollectionUtils.join(t, "';'")+"'};";
		runCommand(array);
	}
	
	public void sendCollectionNumbers(String id, Collection<?> t) throws CommandExecutionException{
		String array = id+" = [];";
		if(t != null)
			if(!t.isEmpty()) 
				array = id + " = [ "+CollectionUtils.join(t, ", ")+"];";
		runCommand(array);
	}
	
	public void sendCollectionNumbers(String id, Collection<?> t, boolean isVertical) throws CommandExecutionException{
		String array = id+" = [];";
		if(!t.isEmpty()) 
			array = id + " = [ "+CollectionUtils.join(t, "; ")+"];";
		runCommand(array);
	}
	
	
	private void setStoichiometryMatrix(IStoichiometricMatrix matrix) throws Exception{
		
		DoubleMatrix2D sparse = matrix.convertToColt();
		
		IntArrayList x = new IntArrayList();
		IntArrayList y = new IntArrayList();
		DoubleArrayList value = new DoubleArrayList();
		sparse.getNonZeros(x, y, value);

		String mlI = "i = " + x + ";i = i.*1+1;";		
		String mlJ = "j = " + y + ";j = j.*1+1;";
		String mlS = "s = " + value + ";";
		
		runCommand(mlI);
		runCommand(mlJ);
		runCommand(mlS);
		
		sendInteger("m", model.getNumberOfMetabolites());
		sendInteger("n", model.getNumberOfReactions());
		
		runFunction("sparse",  Arrays.asList(new String[]{"i","j","s","m","n"}), "S");
	}
	
	@Override
	public void runCommand(String command) throws CommandExecutionException {
		try {
			System.out.println("Matlab command: " + command);
			getProxy().eval(command);
		} catch (MatlabInvocationException e) {
			throw new CommandExecutionException(e);
		}
	}

	// TODO: split method in smaller ones to make it ease to understand 
	private void createModel() throws Exception
	{
		sendCollectionStrings("mets", model.getMetabolites().keySet(), true);
				
		sendCollectionStrings("rxns", model.getReactions().keySet(), true);
		
		sendCollectionStrings("rxnNames", model.getReactions().keySet(), true);
		boolean hasGenes = false;
		try{
			
			if(model.getModelType().equals(ModelType.GENE_REACTION_STEADY_STATE_MODEL)){
				SteadyStateGeneReactionModel modelGR = (SteadyStateGeneReactionModel) model;
				
				sendCollectionStrings("genes", modelGR.getGenes().keySet(), true);
				
				String iGenes = "[";
				String jRxns = "[";
				String mtGRValue = "[";
				
				for (String gene : modelGR.getGeneReactionMapping().keySet()) {
	//				if(modelGR.getGeneReactionMapping().containsKey(gene)){
						for (String rxn : new HashSet<String>(modelGR.getGeneReactionMapping().get(gene))) {
							//System.out.println(gene + ": " + (new ArrayList(modelGR.getGenes().keySet()).indexOf(gene)) +"\t"+ 
						//rxn + ": " + (new ArrayList(model.getReactions().keySet()).indexOf(rxn)));
							iGenes += ((new ArrayList(modelGR.getGenes().keySet()).indexOf(gene))+1) + ",";
							jRxns += ((new ArrayList(model.getReactions().keySet()).indexOf(rxn))+1) + ",";
							mtGRValue += "1,";
						}
	//				}
				}
				
				iGenes = iGenes.substring(0, iGenes.length()-1) + "]";
				jRxns = jRxns.substring(0, jRxns.length()-1) + "]";
				mtGRValue = mtGRValue.substring(0, mtGRValue.length()-1) + "]";
				
				sendVariable("iGenes", iGenes);
				sendVariable("jRxns", jRxns);
				sendVariable("mtGRValue", mtGRValue);
				
				sendInteger("mRxns", model.getNumberOfReactions());
				sendInteger("nGenes", modelGR.getNumberOfGenes());
				
				runFunction("sparse",  Arrays.asList(new String[]{"jRxns","iGenes","mtGRValue","mRxns","nGenes"}), "rxnGeneMat");
				
				List<String> list = new ArrayList<String>();
				List<String> listGR = new ArrayList<String>();
				
				list.addAll( modelGR.getReactions().keySet());
				java.util.Collections.sort(list);
				for (int i = 0; i < list.size(); i++) {
					if(modelGR.getGeneReactionRule(list.get(i)) != null)
						listGR.add(i, modelGR.getGeneReactionRule(list.get(i)).getRule().toString());
					else
						listGR.add(i, "");
				}
				
				sendCollectionStrings("rules", listGR, true);
				hasGenes = true;
			}
		}catch(Exception e){
			throw new CommandExecutionException("Problem with setting gene rules", e);
		}
		
				
		setStoichiometryMatrix(model.getStoichiometricMatrix());
		
		String mlLb = "[";
		String mlUb = "[";
		String mlRev = "[";
		for(String metab: model.getReactions().keySet())
		{
			mlLb += model.getReaction(metab).getConstraints().getLowerLimit() + ";";
			mlUb += model.getReaction(metab).getConstraints().getUpperLimit() + ";";
			mlRev += model.getReaction(metab).isReversible() ? "1;" : "0;";
		
		}
		
		mlLb = mlLb.substring(0, mlLb.length()-1) + "];";
		mlUb = mlUb.substring(0, mlUb.length()-1) + "];";
		mlRev = mlRev.substring(0, mlRev.length()-1) + "];";
		
		sendVariable("lb", mlLb);
		sendVariable("ub", mlUb);
		sendVariable("rev", mlRev);
		
		String mlRecObj = "[";
		for(String react : model.getReactions().keySet())
		{
			if(react.equals(model.getBiomassFlux()))
				mlRecObj += "1;";
			else
				mlRecObj += "0;";
		}

		mlRecObj = mlRecObj.substring(0,mlRecObj.length()-1) + "];";
		
		sendVariable("c", mlRecObj);
		
		String mlB = "[";
		for(int i = 0; i < model.getMetabolites().size(); i++)
			mlB += "0;";
		mlB = mlB.substring(0,mlB.length()-1) + "];";
		
		sendVariable("b", mlB);
		
//		String mlCreateModel = modelName+ ".rxns = rxns;" +
//				modelName+ ".mets = mets;" +
//				//modelName+ ".grRules = grRules;" +
//				modelName+ ".rxnNames = rxnNames;" +
//				modelName+ ".S = S;" +
//				modelName+ ".lb = lb;" +
//				modelName+ ".ub = ub;" +
//				modelName+ ".c = c;" +
//				modelName+ ".rev = rev"+
//				modelName+ ".b = b";
		
//		if(hasGenes){
//			mlCreateModel += modelName+ ".genes = genes;"+
//					modelName+ ".rxnGeneMat = rxnGeneMat;";
//		}
		
		
		sendVariable(modelName+ ".rxns", "rxns");
		sendVariable(modelName+ ".mets", "mets");
		sendVariable(modelName+ ".S", "S");
		sendVariable(modelName+ ".lb", "lb");
		sendVariable(modelName+ ".ub", "ub");
		sendVariable(modelName+ ".c", "c");
		sendVariable(modelName+ ".rev", "rev");
		sendVariable(modelName+ ".b", "b");
		sendVariable(modelName+ ".rxnNames", "rxnNames");
		
//		if(hasGenes){
//			sendVariable(modelName+ ".genes", "genes");
//			sendVariable(modelName+ ".rxnGeneMat", "rxnGeneMat");
//			sendVariable(modelName+ ".rules", "rules");
//		}
		
		runCommand("clear s");
		runCommand("clear i");
		runCommand("clear j");
		runCommand("clear rxns");
		runCommand("clear mets");
		runCommand("clear rules");
		runCommand("clear rxnNames");
		runCommand("clear S");
		runCommand("clear lb");
		runCommand("clear ub");
		runCommand("clear c");
		runCommand("clear n");
		runCommand("clear m");
		runCommand("clear rev");
		runCommand("clear b");
		
		if(hasGenes){
			runCommand("clear genes");
			runCommand("clear rxnGeneMat");
		}
		
	}

	public MatlabProxy getProxy() {
		return proxy;
	}

	public void setProxy(MatlabProxy proxy) {
		this.proxy = proxy;
	}
	
	@Override
	public String getVariableString(String command)
	{
		try {
			return (String) getProxy().getVariable(command);
		} catch (MatlabInvocationException e) {
			// TODO Auto-generated catch block
			return "";
		}
	}

	@Override
	public String[] getVariableStringList(String command) {
		try {
			return (String[]) getProxy().getVariable(command);
		} catch (MatlabInvocationException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}

	@Override
	public int getVariableInteger(String command) {
		try {
			return (int) getProxy().getVariable(command);
		} catch (MatlabInvocationException e) {
			// TODO Auto-generated catch block
			return 0;
		}
	}

	@Override
	public int[] getVariableIntegerList(String command) {
		try {
			return (int[]) getProxy().getVariable(command);
		} catch (MatlabInvocationException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}

	@Override
	public double getVariableDouble(String command) {
		try {
			return (double) getProxy().getVariable(command);
		} catch (MatlabInvocationException e) {
			// TODO Auto-generated catch block
			return 0;
		}
	}

	@Override
	public double[] getVariableDoubleList(String command) {
		try {
			return (double[]) getProxy().getVariable(command);
		} catch (MatlabInvocationException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}


	@Override
	public void sendInteger(String variableName, int variableValue) throws Exception {
		runCommand(variableName +"="+ variableValue+";");
	}

	@Override
	public void sendIntegerList(String variableName, List variableValue) throws Exception {
		sendCollectionNumbers(variableName, Arrays.asList(variableValue));	
	}

	@Override
	public void sendDouble(String variableName, double variableValue) throws Exception {
		runCommand(variableName +"="+ variableValue+";");
	}

	@Override
	public void sendDoubleList(String variableName, List variableValue) throws Exception {
		sendCollectionNumbers(variableName, Arrays.asList(variableValue));		
	}

	@Override
	public void sendString(String variableName, String variableValue) throws Exception {
		runCommand(variableName +"= '" + variableValue + "';" );
	}

	@Override
	public void sendStringList(String variableName, String[] variableValue) throws Exception {
		sendCollectionStrings(variableName, Arrays.asList(variableValue));
	}
	
	@Override
	public void sendVariable(String variableName, String variableValue) throws Exception {
		runCommand(variableName +"="+ variableValue+";");
	}

	
	public void runFunction(String fName, String fInput, String fOutput) {	
		runFunction(fName, Arrays.asList(fInput), Arrays.asList(fOutput));
		
	}

	public void runFunction(String fName, List<String> fInput, String fOutput) {
		runFunction(fName, fInput, Arrays.asList(fOutput));
		
	}
	
	public void runFunction(String fName, String fInput, List<String> fOutput) {
		runFunction(fName, Arrays.asList(fInput), fOutput);
		
	}

	@Override
	public void runFunction(String fName, List<String> fInput, List<String> fOutput) {
		boolean thereIsInput = true;
		boolean thereIsOutput = true;
		
		if(fInput.isEmpty() || (fInput.size()==1 && fInput.get(0).isEmpty()))
			thereIsInput = false;
		
		if(fOutput.isEmpty() || (fOutput.size()==1 && fOutput.get(0).isEmpty()))
			thereIsOutput = false;	
		
		String str = "";
		
		if(thereIsInput && !thereIsOutput)
			str = fName +"("+ CollectionUtils.join(fInput, ",")+");";
		else if(thereIsOutput && !thereIsInput)
			str = "["+CollectionUtils.join(fOutput, " ")+"]="+fName+";";
		else if(thereIsInput && thereIsOutput)
			str = "["+CollectionUtils.join(fOutput, " ")+"]="+fName +"("+ CollectionUtils.join(fInput, ",")+");";
		else
			str = fName+";";
		
		try {
			runCommand(str);
		} catch (CommandExecutionException e) {
			throw new CommandExecutionException("Problem with run function", e);
		}
	}
	
	
	public static void main(String[] args) throws Exception{
		MatlabConnection cmc = new MatlabConnection();
		cmc.runCommand("initCobraToolbox");

		cmc.runFunction("readCbModel", "'C:\\Users\\Programador\\Desktop\\Uminho\\OptFlux coisas\\Ec_iJR904.xml'", "model");
		
//		model = changeObjective(model,{'BiomassEcoli'},1);
		cmc.runFunction("changeObjective", Arrays.asList(new String[]{"model", "{'BiomassEcoli'}","1"}), "model");
		
//		solution = optimizeCbModel(model);
		cmc.runFunction("optimizeCbModel", "model", "solution");
		
		
		System.out.println("\nMatlab in Optflux");
				
		cmc.runFunction("sparse",  Arrays.asList(new String[]{"i","j","s","m","n"}), "S");
	}

	@Override
	public Object getVariable(String command) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
