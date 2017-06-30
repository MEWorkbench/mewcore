package pt.uminho.ceb.biosystems.mew.core.balance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.solvers.SolverFactory;
import pt.uminho.ceb.biosystems.mew.solvers.lp.ILPSolver;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;

public class BalanceChecker<T> {
	
	AbstractIndexedMatrix<T> matrixS;
	AbstractIndexedMatrix<T> matrixM;
	AbstractIndexedMatrix<T> matrixR;
	AbstractIndexedMatrix<T> matrixC;
	AbstractIndexedMatrix<T> matrixSf;
	AbstractIndexedMatrix<T> matrixRf;
	Map<String,Integer> mapLP;
	Map<String,String> mapLPmet;
	Map<String,String> mapLPcomp;
	
//	SolverType solverType = SolverType.CPLEX;
	protected String solverId;
	
	public BalanceChecker (AbstractIndexedMatrix<T> s, AbstractIndexedMatrix<T> m, String solverId){
		this.matrixS = s;
		this.matrixM = m;
		this.matrixR = buildMatrixR();
	}
	
	public String getSolverId() {
		return solverId;
	}

	public void setSolverId(String solverId) {
		this.solverId = solverId;
	}
	
	public AbstractIndexedMatrix<T> buildMatrixR() { // matriz R 
	
		AbstractIndexedMatrix<T> r = matrixS.transpose().mult(matrixM);
		return r;
	}
	
	public Boolean checkbalance() { // balanceado -> R is zero matrix
		
		Boolean resol = false;
		if(matrixR.getZeroRows().size()== matrixR.getNumberOfRows()){
			resol = true;
		}
		return resol;
	}
	
	public Set<String> unbalancedIdsReactions(){ //linhas de R que contem elem !=0
		
		Set <String> idsReactions= new HashSet<>();
		
		if(checkbalance()==false){
			Set <String> ids= new HashSet<>(matrixR.getRowIndex().keySet());
			ids.removeAll(matrixR.getZeroRows());
			idsReactions.addAll(ids);
		}
		return idsReactions;
	}	
	
	public void buildMatrixF() { // matriz S e R filtradas 
		
		Set<String> metsWithOutFormula = getMetsWithoutFormula(); //metabolitos sem formula
		Set<String> metsWithFormula = new HashSet<>(matrixS.getRowIndex().keySet()); //metabolitos com formula -> remover
		metsWithFormula.removeAll(metsWithOutFormula);
		
		Set<String> reactionsIDsToRemove = new HashSet<>(matrixS.getColIndex().keySet());
		reactionsIDsToRemove.removeAll(keepReactions()); //reacoes onde não participam metabolitos sem formula -> remover
		
		AbstractIndexedMatrix<T> matrixSf = matrixS.clone(); 
		matrixSf.remmoveRows(metsWithFormula);
		matrixSf.remmoveCol(reactionsIDsToRemove);
		this.matrixSf=matrixSf.transpose(); 
		
		AbstractIndexedMatrix<T> matrixRf = matrixR.clone();
		matrixRf.remmoveRows(reactionsIDsToRemove);
		this.matrixRf=matrixRf;
	}
	
	public Set<String> getMetsWithoutFormula(){ 
		
		return matrixM.getZeroRows();
	}

	public Set<String> keepReactions(){ //reacoes com metabolitos sem formula
	
		Set<String> idsReactions = new HashSet<>();
		Set<String> MetsWithoutFormula = getMetsWithoutFormula();
		
		for (String idrec:matrixS.getColIndex().keySet()){
			for (String idmet:MetsWithoutFormula){
				if(matrixS.getValue(matrixS.rowIndex.get(idmet), matrixS.colIndex.get(idrec))!=0.0){
					idsReactions.add(idrec);
				}
			}
		}
		return idsReactions;		
	}
	
	public Map<String, String>  balance(){ 
		
		Map<String, String> metsFormulas = new HashMap<>(); // Metabolito sem formula : Formula
		
		buildMatrixF(); 
		//SF.C=RF -> Sistema Determinado se rank(SF)=rank(SF|RF)=n(nr de variaveis ou nr linhas)
		if (matrixSf.rank()==matrixSf.augmentedMatrix(matrixRf).rank() && matrixSf.rank()==matrixSf.getNumberOfColumns()){
			AbstractIndexedMatrix<T> matrixC = matrixSf.solve(matrixRf);
			this.matrixC=matrixC;			
			metsFormulas.putAll(getUnknownFormulas()); 
			System.out.println("Sistema Determinado!");
			}
		else{ // se e um sistema indeterminado -> LP
			Map<String, String> sol = lp();
			metsFormulas.putAll(sol);
			System.out.println("Sistema Indeterminado!");
			}
		return metsFormulas;	
	}
	
	public Map<String, String> lp(){
		try {
			LPProblem p = createLP();
			ILPSolver solver = SolverFactory.getInstance().lpSolver(getSolverId(), p);
			LPSolution sol = solver.solve();
			
			Map<String, String> metsFormulas = new HashMap<>();
			for (String metconc:mapLP.keySet()){
				String comp = mapLPcomp.get(metconc);
				String met = mapLPmet.get(metconc);
				Double val = sol.getValues().get(mapLP.get(metconc));
			
				if(metsFormulas.containsKey(met)){
					String formula =metsFormulas.get(met);
					formula += comp+""+Math.round(val);
					metsFormulas.put(met, formula);
				}
				else{
					String formula = comp+""+Math.round(val);
					metsFormulas.put(met, formula);
				}				
			}
			return metsFormulas;
			
		} catch (LinearProgrammingTermAlreadyPresentException e) {
			throw new RuntimeException("Problema na criacao do lp", e);
		}
	}	
	
	LPProblem createLP() throws LinearProgrammingTermAlreadyPresentException{
		LPProblem lp = new LPProblem();
		LPProblemRow row = new LPProblemRow();
		Map<String, String> mapLPcomp = new HashMap<>();
		Map<String, String> mapLPmet = new HashMap<>();
		Map<String, Integer> idx = new HashMap<>();
		
		int i=0;
		for(int met = 0; met < matrixSf.getNumberOfColumns(); met++){
			for(int comp = 0; comp < matrixRf.getNumberOfColumns(); comp++){
				String name = matrixSf.getIDColumn(met);
				name = name.concat(matrixRf.getIDColumn(comp)+"");
				mapLPcomp.put(name,matrixRf.getIDColumn(comp));
				mapLPmet.put(name,matrixSf.getIDColumn(met));
				lp.addVariable(new LPVariable(name, 0, 1000));//name, lower bound, upper bound
				idx.put(name, i);
				i++;
			}
		}
		
		for(int comp = 0; comp < matrixRf.getNumberOfColumns(); comp++){
			for(int r = 0; r < matrixSf.getNumberOfRows(); r++){
				row = new LPProblemRow();
				for(int m = 0; m < matrixSf.getNumberOfColumns(); m++){
					String name =matrixSf.getIDColumn(m);
					name = name.concat(matrixRf.getIDColumn(comp)+"");
					row.addTerm(idx.get(name), matrixSf.getValue(r, m));
				}
				lp.addConstraint(row, LPConstraintType.EQUALITY , -matrixRf.getValue(r,comp));
			}
		}
		
		LPProblemRow obj = new LPProblemRow();
		for (String name: idx.keySet()){
			obj.addTerm(idx.get(name), 1.0);
		}
		lp.setObjectiveFunction(obj, false);
		
		this.mapLP=idx;
		this.mapLPcomp=mapLPcomp;
		this.mapLPmet=mapLPmet;
		
		return lp;
	}
	
	public Map<String, String> getUnknownFormulas(){
		
		Map<String, String> metsFormulas = new HashMap<>();
		
		for (String met:matrixC.getRowIndex().keySet()){
			String f = "";
			for(String e : matrixC.getColIndex().keySet()){
				Double n = matrixC.getValue(matrixC.getRowIndex().get(met),matrixC.getColIndex().get(e));
				if(n<0.0){
					n=-n;
				}
				if(n!=0.0){
					f+=e+""+Math.round(n);
				}
			}
			metsFormulas.put(met, f);
		}
		return metsFormulas;
	}
	
}
