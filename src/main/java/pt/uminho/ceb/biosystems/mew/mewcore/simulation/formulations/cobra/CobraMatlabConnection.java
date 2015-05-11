package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.cobra;
//package metabolic.simulation.formulations.cobra;
//
//import java.util.Map;
//
//import MatlabConnectionException;
//import MatlabInvocationException;
//import MatlabProxy;
//import metabolic.model.components.ReactionConstraint;
//import metabolic.model.steadystatemodel.ISteadyStateModel;
//import metabolic.simulation.components.FluxValueMap;
//import metabolic.simulation.components.OverrideSteadyStateModel;
//import metabolic.simulation.components.SimulationProperties;
//import metabolic.simulation.components.SteadyStateSimulationResult;
//import metabolic.simulation.formulations.exceptions.ManagerExceptionUtils;
//import metabolic.simulation.formulations.exceptions.MandatoryPropertyException;
//import metabolic.simulation.formulations.exceptions.PropertyCastException;
//import utilities.datastructures.map.MapStringNum;
//import cern.colt.list.DoubleArrayList;
//import cern.colt.list.IntArrayList;
//import cern.colt.matrix.DoubleMatrix2D;
//
//public class CobraMatlabConnection implements IConverter{
//	
//	MatlabProxy proxy;
//	ISteadyStateModel model;
//	Map<String, Object> param;
//
//	public CobraMatlabConnection() throws MatlabConnectionException, MatlabInvocationException
//	{
//	}
//	
//	public void init() throws MatlabConnectionException, MatlabInvocationException
//	{
//		proxy = MatlabProxySingleton.getInstance().getProxy();
//		//proxy.feval("try");
//		proxy.eval("initCobraToolbox;");
//		System.out.println("initCobraToolbox");
//	}
//	
//	public void sendModel(ISteadyStateModel model) throws MatlabInvocationException
//	{
//		this.model = model;
//		
//		createModel();
//	}
//	
//	public void setObjectiveFunction(MapStringNum objectiveFunctionsList) throws MatlabInvocationException
//	{
//		String rxnNameList = "";
//		String objectiveCoeff = "";
//		for(String reacao: objectiveFunctionsList.keySet())
//		{
//			rxnNameList += "'" +reacao + "' "; 
//			objectiveCoeff += objectiveFunctionsList.get(reacao) + " ";
//		}
//
//		// Find another way!!!!!!!!!!!!!!!
//		proxy.eval("modelSBMLf = changeObjective(modelSBMLf,{"+rxnNameList.substring(0, rxnNameList.length()-1)+"},["+objectiveCoeff.substring(0, objectiveCoeff.length()-1)+"]);");
//		
//		System.out.println("modelSBMLf = changeObjective(modelSBMLf,{"+rxnNameList.substring(0, rxnNameList.length()-1)+"},["+objectiveCoeff.substring(0, objectiveCoeff.length()-1)+"]);");
//	}
//	
//	public void setOverrideModel(OverrideSteadyStateModel overrideModel) throws MatlabInvocationException
//	{		
//
//		for(String rId : overrideModel.getOverridedReactions()){
//			ReactionConstraint rc = overrideModel.getReactionConstraint(rId);
//			double lower = rc.getLowerLimit();
//			double upper = rc.getUpperLimit();
//			
//			proxy.eval("modelSBMLf = changeRxnBounds(modelSBMLf,'"+ rId +"',"+ lower+", 'l');");
//			proxy.eval("modelSBMLf = changeRxnBounds(modelSBMLf,'"+ rId +"',"+ upper+", 'u');");
//			
//			System.out.println("modelSBMLf = changeRxnBounds(modelSBMLf,'"+ rId +"',"+ lower+", 'l');");
//			System.out.println("modelSBMLf = changeRxnBounds(modelSBMLf,'"+ rId +"',"+ upper+", 'u');");
//		}
//	}
//	
//
//	public SteadyStateSimulationResult simulate() throws MatlabInvocationException, PropertyCastException, MandatoryPropertyException
//	{
//		try{
//			
//			//proxy.eval("try");
//			
//			SteadyStateSimulationResult result = new SteadyStateSimulationResult(model, "", null);
//			
//			// Throw exception when no isMaximized
//			Boolean isMaximized = ManagerExceptionUtils.testCast(param, Boolean.class, SimulationProperties.IS_MAXIMIZATION, false);
//			
//			// COBRA function that perform a FBA
//			if(isMaximized)
//				proxy.eval("FBAsolution = optimizeCbModel(modelSBMLf);");
//			else
//				proxy.eval("FBAsolution = optimizeCbModel(modelSBMLf, 'min');");
//			
//			double[] reducedCosts = (double[]) proxy.getVariable("FBAsolution.w");
//			double[] dual = (double[]) proxy.getVariable("FBAsolution.y");
//			double[] primal = (double[]) proxy.getVariable("FBAsolution.x");
//			
//			String[] rxns = ((String[]) proxy.getVariable("modelSBMLf.rxns"));
//			
//			FluxValueMap fluxValues = new FluxValueMap();
//			for (int i = 0; i < rxns.length; i++)
//				fluxValues.put(rxns[i], primal[i]);
//			
//			
//			// Objective function
//			proxy.eval("objectiveAbbr = checkObjective(modelSBMLf);");
//			String oFString = (String) proxy.getVariable("objectiveAbbr");
//			result.setOFString(oFString);
//			
//			result.setOFvalue(((double[]) proxy.getVariable("FBAsolution.f"))[0]);
//			result.setSolverOutput(((String) proxy.getVariable("FBAsolution.solver")));
//			result.setFluxValues(fluxValues);
//			
//			//proxy.eval("catch err");
//			//proxy.eval("msgError = getReport(err);");
//			//proxy.feval("end");
//			
//			
//			//proxy.exit();
//			
//			System.out.println("FBAsolution = optimizeCbModel(modelSBMLf);");
//			System.out.println("objectiveAbbr = checkObjective(modelSBMLf);");
//			
//			return result;
//		
//		}	catch(Exception e){
//			System.out.println("Erro do lado do Matlab");
//			//System.out.println( ((String) proxy.getVariable("msgError")));
//		}
//		return null;
//		
//	}
//	
//	
//	private void createModel() throws MatlabInvocationException
//	{
//		// String com a lista dos metabolitos pronta para ser enviada para o Matlab
//		String mlMetab = "mets = {";
//		for(String meta: model.getMetabolites().keySet())
//			mlMetab = mlMetab + "'" + meta + "';";
//		
//		mlMetab = mlMetab.substring(0, mlMetab.length()-1) + "};";
//				
//		// String com a lista das reactions pronta para ser enviada para o Matlab
//		String mlReact = "rxns = {";
//		for(String reac: model.getReactions().keySet())
//				mlReact = mlReact + "'" + reac +"';";
//		
//		mlReact = mlReact.substring(0, mlReact.length()-1) + "};"; 
//				
//		
//		DoubleMatrix2D sparse = model.getStoichiometricMatrix().convertToColt();
//		
//		IntArrayList x = new IntArrayList();
//		IntArrayList y = new IntArrayList();
//		DoubleArrayList value = new DoubleArrayList();
//		sparse.getNonZeros(x, y, value);
//		
//		// i e o vetor com as posicoes dos metabolitos
//		String mlI = "i = " + x + ";i = i.*1+1;";
//		// j e o vetor com as posicoes das reacoes
//		String mlJ = "j = " + y + ";j = j.*1+1;";
//		// s e a matriz sem os zeros
//		String mlS = "s = " + value + ";";
//		// m e n correspondem ao numero de metabolitos e reacoes respetivamente
//		String mlM = "m = " + model.getNumberOfMetabolites() + ";";
//		String mlN = "n = " + model.getNumberOfReactions() + ";";
//		// A funcao sparse do matlab cria a matriz estequiometrica
//		String mlSparse = "S = sparse(i,j,s,m,n);";
//		
//		
//		// Criar strings referentes aos upperbounds e lowerbounds para inserir no matlab
//		// Pode estar dentro da criacao da lista de reacoes e metabolitos
//		String mlLb = "lb = [";
//		String mlUb = "ub = [";
//		for(String metab: model.getReactions().keySet())
//		{
//			mlLb += model.getReaction(metab).getConstraints().getLowerLimit() + ";";
//			mlUb += model.getReaction(metab).getConstraints().getUpperLimit() + ";";
//		}
//		
//		mlLb = mlLb.substring(0, mlLb.length()-1) + "];";
//		mlUb = mlUb.substring(0, mlUb.length()-1) + "];";
//		
//		
//		String mlRecObj = "c = [";
//		for(String react : model.getReactions().keySet())
//		{
//			if(react == model.getBiomassFlux())
//				mlRecObj += "1;";
//			else
//				mlRecObj += "0;";
//		}
//
//		mlRecObj = mlRecObj.substring(0,mlRecObj.length()-1) + "];";
//		
//		// String enviada para o matlab para criar modelo
//		String mlCreateModel = "modelSBMLf.rxns = rxns;" +
//							   "modelSBMLf.mets = mets;" +
//							   "modelSBMLf.S = S;" +
//							   "modelSBMLf.lb = lb;" +
//							   "modelSBMLf.ub = ub;" +
//							   "modelSBMLf.c = c;";
//		
//		proxy.eval(mlI);
//		proxy.eval(mlJ);
//		proxy.eval(mlS);
//		proxy.eval(mlM);
//		proxy.eval(mlN);
//		proxy.eval(mlSparse);
//		
//		proxy.eval(mlMetab);
//		proxy.eval(mlReact);
//		
//		proxy.eval(mlLb);
//		proxy.eval(mlUb);
//		
//		proxy.eval(mlRecObj);
//		
//		proxy.eval(mlCreateModel);
//		
//		System.out.println(mlI);
//		System.out.println(mlJ);
//		System.out.println(mlS);
//		System.out.println(mlM);
//		System.out.println(mlN);
//		System.out.println(mlSparse);
//		System.out.println(mlMetab);
//		System.out.println(mlReact);
//		System.out.println(mlLb);
//		System.out.println(mlUb);
//		System.out.println(mlRecObj);
//		System.out.println(mlCreateModel);
//	}
//
//	@Override
//	public void setParameters(Map<String, Object> param) {
//		this.param = param;
//	}
//	
//}
