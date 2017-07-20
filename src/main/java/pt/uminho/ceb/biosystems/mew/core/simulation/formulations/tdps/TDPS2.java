package pt.uminho.ceb.biosystems.mew.core.simulation.formulations.tdps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.IOverrideReactionBounds;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSReferenceSimulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.MILPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;

public class TDPS2 extends AbstractSSReferenceSimulation<MILPProblem> {
	
	public static double						DEFAULT_PENALTY			= 10;
																		
	public Map<Integer, Collection<Double>>		regKmet					= new HashMap<Integer, Collection<Double>>();
	public Map<String, Double>					regKs					= new HashMap<String, Double>();
	public Map<Integer, Collection<Integer>>	producers				= new HashMap<Integer, Collection<Integer>>();
	public Map<Integer, Collection<Integer>>	consumers				= new HashMap<Integer, Collection<Integer>>();
	public Map<Integer, Collection<Integer>>	products				= new HashMap<Integer, Collection<Integer>>();
	public Map<Integer, Collection<Integer>>	reactants				= new HashMap<Integer, Collection<Integer>>();
	public Map<Integer, Double>					rSum					= new HashMap<Integer, Double>();
	public Map<Integer, Double>					turnovers				= new HashMap<Integer, Double>();
																		
	public double								defaultBound			= 100000000;
																		
	protected IOverrideReactionBounds			overrideBounds			= null;
																		
	/**
	 * This method describes a simulation approach that minimizes the differences between the the
	 * share of substrate consumed by a reaction in the wildtype and in the mutant
	 * 
	 * @param model
	 */
	public TDPS2(ISteadyStateModel model) {
		super(model);
		overrideBounds = createModelOverride();
		initTDPSProperties();
	}
	
	private void initTDPSProperties() {
		optionalProperties.add(SimulationProperties.TDPS_PENALTY);
		optionalProperties.add(SimulationProperties.TDPS_DRAINS);
		optionalProperties.add(SimulationProperties.TDPS_UNBOUNDED_METABOLITES);
		mandatoryProperties.add(SimulationProperties.TDPS_REMOVE_METABOLITES);
	}
	
	public void setPenalty(Double penalty) {
		properties.put(SimulationProperties.TDPS_PENALTY, penalty);
	}
	
	public Double getPenalty() {
		Double penalty = null;
		try {
			penalty = ManagerExceptionUtils.testCast(properties, Double.class, SimulationProperties.TDPS_PENALTY, true);
		} catch (PropertyCastException e) {
			System.err.println("The property " + e.getProperty() + " was ignored!!\n Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {
		
		}
		
		if (penalty == null) {
			penalty = DEFAULT_PENALTY;
			setPenalty(DEFAULT_PENALTY);
		}
		
		return penalty;
	}
	
	public void setMetabolitesToRemove(Set<String> metabolitesToRemove) {
		properties.put(SimulationProperties.TDPS_REMOVE_METABOLITES, metabolitesToRemove);
	}
	
	@SuppressWarnings("unchecked")
	public Set<String> getMetabolitesToRemove() {
		try {
			Set<String> metabolitesToRemove = ManagerExceptionUtils.testCast(properties, Set.class, SimulationProperties.TDPS_REMOVE_METABOLITES, false);
			return metabolitesToRemove;
		} catch (PropertyCastException e) {
			System.err.println("The property " + e.getProperty() + " was ignored!!\n Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {
			System.err.println("The mandatory property " + e.getProperty() + " was not found!\n Reason: " + e.getMessage());
		}
		return null;
	}
	
	public void setDrains(Set<Integer> drains) {
		properties.put(SimulationProperties.TDPS_DRAINS, drains);
	}
	
	@SuppressWarnings("unchecked")
	public Set<Integer> getDrains() {
		Set<Integer> drains = null;
		try {
			drains = ManagerExceptionUtils.testCast(properties, Set.class, SimulationProperties.TDPS_DRAINS, true);
		} catch (PropertyCastException e) {
			System.err.println("The property " + e.getProperty() + " was ignored!!\n Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {
		
		}
		
		if (drains == null) {
			drains = computeDrains();
			setDrains(drains);
		}
		
		return drains;
	}
	
	private Set<Integer> computeDrains() {
		Set<Integer> drains = new HashSet<Integer>();
		for (String id : model.getReactions().keySet()) {
			//if it is an input drain,it should be kept because it is important in the calculation of the production turnovers
			if (overrideBounds.getReactionConstraint(id).getLowerLimit() < 0) {
				continue;
			}
			
			if (model.getReaction(id).getType().equals(ReactionType.DRAIN)) {
				drains.add(model.getReactionIndex(id));
			}
		}
		return drains;
	}
	
	public void setUnboundedMetabolites(Set<Integer> unboundedMetabolites){
		properties.put(SimulationProperties.TDPS_UNBOUNDED_METABOLITES, unboundedMetabolites);
	}
	
	@SuppressWarnings("unchecked")
	public Set<Integer> getUnboundedMetabolites(){
		Set<Integer> unboundedMetab = null;
		try {
			unboundedMetab = ManagerExceptionUtils.testCast(properties, Set.class, SimulationProperties.TDPS_UNBOUNDED_METABOLITES, true);
		} catch (PropertyCastException e) {
			System.err.println("The property " + e.getProperty() + " was ignored!!\n Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {
		
		}
		
		if (unboundedMetab == null) {
			unboundedMetab = computeUnboundedMetabolites();
			setUnboundedMetabolites(unboundedMetab);
		}
		
		return unboundedMetab;
	}
	
	private Set<Integer> computeUnboundedMetabolites() {
		Set<Integer> unboundMet = new HashSet<Integer>();
		for (int met = 0; met < model.getNumberOfMetabolites(); met++) {
			if (getMetabolitesToRemove().contains(model.getMetaboliteId(met))) {
				unboundMet.add(met);
			}
		}
		return unboundMet;
	}
	
	@Override
	public MILPProblem constructEmptyProblem() {
		return new MILPProblem();
	}
	
	@Override
	public void createVariables(){
		super.createVariables();			
	}
	
	@Override
	public void createConstraints(){
		super.createConstraints();
	}
	
	public void halfReactionsMILP() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException {
		//Split all REVERSIBLE reactions in two positive variables
		for (int i = 0; i < model.getNumberOfReactions(); i++) {
			
			//get reaction id
			String id = model.getReactionId(i);
			
			String name = model.getReactionId(i);
			final String idPositive = "TORV_" + name + "(" + i + ")_PST";
			final String idNegative = "TORV_" + name + "(" + i + ")_NGT";
			
			//except drains
			if (getDrains().contains(i)) {
				continue;
			}
			
//			get reaction constraint, either from override bounds or directly from the model
			ReactionConstraint rc = overrideBounds.getReactionConstraint(i);
			rc = (rc != null) ? rc : model.getReactionConstraint(i);
			
			//if the reaction is irreversible ignore the split routine and add its index to the var mappings according to the split nomenclature: "TORV_"+name+"("+i+")_PST"			
			if (rc.getLowerLimit() >= 0 && rc.getUpperLimit() > 0) {
				idToIndexVarMapings.put(idPositive, idToIndexVarMapings.get(id));
				continue;
			}
			//if the reaction is irreversible ignore the split routine and add its index to the var mappings according to the split nomenclature: "TORV_"+name+"("+i+")_NGT";
			if (rc.getUpperLimit() <= 0 && rc.getLowerLimit() < 0) {
				idToIndexVarMapings.put(idNegative, idToIndexVarMapings.get(id));
				continue;
			}
						
			Map<String, Integer> newVars = null;
			try {
				
				//split reversible reactions into a positive Vp and a negative Vn half reaction
				newVars = splitNegAndPosVariable(problem, i, idPositive, idNegative, rc.getLowerLimit(), rc.getUpperLimit());
//				newVars = L1VarTerm.splitNegAndPosVariable(problem, i, idPositive, idNegative, rc.getLowerLimit(), rc.getUpperLimit());
				putNewVariables(newVars);
			} catch (WrongFormulationException e) {
				e.printStackTrace();
			}
			
			//get the spilt variables' indexes	
			int vpn = idToIndexVarMapings.get(idPositive);
			int vnn = idToIndexVarMapings.get(idNegative);
						
			//get the number of variables
			int num_vars = problem.getNumberVariables();
			int bp = num_vars;
			int bn = bp+1;
			
			problem.addIntVariable("y" + i, 0, 1);
			problem.addIntVariable("w" + i, 0, 1);
			//add the boolean variables to the var mappings
			indexToIdVarMapings.put(bp, "y" + i);
			indexToIdVarMapings.put(bn, "w" + i);
			idToIndexVarMapings.put("y" + i, bp);
			idToIndexVarMapings.put("w" + i, bn);
			
			//create two new rows
			LPProblemRow binaryP = new LPProblemRow();
			LPProblemRow binaryN = new LPProblemRow();
			try {
				// create: Vp< 1000 * Bp ; If the boolean variable is 1, Vn is lower than 1000,if it is 0, Vp has to be zero
				binaryP.addTerm(vpn, 1);
				binaryP.addTerm(bp, -1000);
				LPConstraint MILPpos = new LPConstraint(LPConstraintType.LESS_THAN, binaryP, 0);
				problem.addConstraint(MILPpos);
				
			} catch (LinearProgrammingTermAlreadyPresentException e) {
				e.printStackTrace();
				
			}
			
			try {
				// create: Vn > -1000 * Bn ; If the boolean variable is 1, Vn is higher than -1000,if it is 0, Vn has to be zero
				
				binaryN.addTerm(vnn, -1);
//				binaryN.addTerm(vnn, 1);
				binaryN.addTerm(bn, -1000);
				
				LPConstraint MILPneg = new LPConstraint(LPConstraintType.LESS_THAN, binaryN, 0);
				problem.addConstraint(MILPneg);
				
			} catch (LinearProgrammingTermAlreadyPresentException e) {
				e.printStackTrace();
				
			}
			
			try {
				//create: Bp+Bn<1; With this constraint the sum of the boolean variables has to be lower or equal to 1
				//which means they cannot both be equal to 1 and Vp and Vn will never be active at the same time
				LPProblemRow binaryS = new LPProblemRow();
				binaryS.addTerm(bp, 1);
				binaryS.addTerm(bn, 1);
				
				LPConstraint MILPsum = new LPConstraint(LPConstraintType.LESS_THAN, binaryS, 1);
				problem.addConstraint(MILPsum);
				
			} catch (LinearProgrammingTermAlreadyPresentException e) {
				e.printStackTrace();
				
			}
			
		}
		
	}
	
	public Map<String, Integer> splitNegAndPosVariable(LPProblem problem, int idx, String posVarName, String negVarName, Double lower, Double upper) throws WrongFormulationException {
		Map<String, Integer> variablePositions = new HashMap<String, Integer>();
		
		int numVariables = problem.getNumberVariables();
		LPVariable normFoVarNeg = new LPVariable(negVarName, lower, 0);
		LPVariable normFoVarPos = new LPVariable(posVarName, 0, upper);
//		
//		System.out.println("LOWER = "+lower);
//		System.out.println("UPPER= "+upper);
		
		System.out.println(idx+"\tNEG:["+(lower)+"\tPOS:["+0.0+","+upper);
		
		int varPos = numVariables;
		int varNeg = numVariables + 1;	
		problem.addVariable(normFoVarPos);
		variablePositions.put(posVarName, varPos);
		problem.addVariable(normFoVarNeg);
		variablePositions.put(negVarName, varNeg);
		
		LPProblemRow rowFoPosNeg = new LPProblemRow();
		try {
			rowFoPosNeg.addTerm(varPos, -1);
			rowFoPosNeg.addTerm(varNeg, -1);
			rowFoPosNeg.addTerm(idx, 1);
			
		} catch (LinearProgrammingTermAlreadyPresentException e) {
			e.printStackTrace();
			throw new WrongFormulationException("Linear Programming Term Already Present");
		}
		
		LPConstraint constPosNeg = new LPConstraint(LPConstraintType.EQUALITY, rowFoPosNeg, 0.0);
		problem.addConstraint(constPosNeg);
		
		return variablePositions;
	}
	
	public void calculateRs() throws PropertyCastException, MandatoryPropertyException {
		
		//create metabolite-reaction maps
		reactionMapper();
		
		//calculate production turnovers
		turnoverCalculator();
		
		//calculate the regK for each reaction's substrates
		
		for (int metabolite = 0; metabolite < model.getNumberOfMetabolites(); metabolite++) {
			double TM = turnovers.get(metabolite);
			Collection<Integer> pro = producers.get(metabolite);
			Collection<Integer> cons = consumers.get(metabolite);
			regKmet.put(metabolite, new ArrayList<Double>());
			
			for (int p : pro) {
				
				//If a reaction can occur in the reverse direction create the R value
				if (model.getReactionConstraint(p).getLowerLimit() < 0) {
					
					//if the reaction is occuring the the reverse direction calculate the R, otherwise assign zero
					if (wtReference.get(model.getReactionId(p)) < 0) {
						double regK = -wtReference.get(model.getReactionId(p)) * model.getStoichiometricValue(metabolite, p) / TM;
						regKs.put(model.getReactionId(p) + "_" + model.getMetaboliteId(metabolite) + "_regK", regK);
						regKmet.get(metabolite).add(regK);
						
					} else {
						regKs.put(model.getReactionId(p) + "_" + model.getMetaboliteId(metabolite) + "_regK", 0.000);
						
					}
				}
				
			}
			
			for (int c : cons) {
				
				//If a reaction can occur in the forward direction create the R value
				if (model.getReactionConstraint(c).getUpperLimit() > 0) {
					
					//if the reaction is occuring the the forward direction calculate the R, otherwise assign zero
					if (wtReference.get(model.getReactionId(c)) > 0) {
						double regK = wtReference.get(model.getReactionId(c)) * -model.getStoichiometricValue(metabolite, c) / TM;
						regKs.put(model.getReactionId(c) + "_" + model.getMetaboliteId(metabolite) + "_regK", regK);
						regKmet.get(metabolite).add(regK);
					} else {
						regKs.put(model.getReactionId(c) + "_" + model.getMetaboliteId(metabolite) + "_regK", 0.000);
						
					}
					
				}
			}
			
		}
		
		//	System.out.println(regKs);
	}

	public void reactionMapper() {		
		
		//create a map with all the producers and consumers of each metabolite
		
		for (int met = 0; met < model.getNumberOfMetabolites(); met++) {
			
			producers.put(met, new ArrayList<Integer>());
			consumers.put(met, new ArrayList<Integer>());
			
			for (int reac = 0; reac < model.getNumberOfReactions(); reac++) {
				
				double stok = model.getStoichiometricValue(met, reac);
				
				//dont include unbounded metabolites
				if ((stok > 0) && (!getUnboundedMetabolites().contains(met))) {
					producers.get(met).add(reac);
					
				}
				//dont include unbounded metabolites
				if ((stok < 0) && (!getUnboundedMetabolites().contains(met))) {
					consumers.get(met).add(reac);
					
				}
			}
		}
		
		//create a map with all the reactants and products of each reaction
		
		for (int reac = 0; reac < model.getNumberOfReactions(); reac++) {
			
			products.put(reac, new ArrayList<Integer>());
			reactants.put(reac, new ArrayList<Integer>());
			
			for (int met = 0; met < model.getNumberOfMetabolites(); met++) {
				
				double stok = model.getStoichiometricValue(met, reac);
				
				//dont include unbounded metabolites
				if ((stok > 0) && (!getUnboundedMetabolites().contains(met))) {
					products.get(reac).add(met);
					
				}
				//dont include unbounded metabolites
				if ((stok < 0) && (!getUnboundedMetabolites().contains(met))) {
					reactants.get(reac).add(met);
					
				}
			}
		}
		
	}
	
	public void turnoverCalculator() throws PropertyCastException, MandatoryPropertyException {
		
		getWTReference();
		
		//according to reaction values calculate production turnovers 
		
		for (int metabolite = 0; metabolite < model.getNumberOfMetabolites(); metabolite++) {
			Collection<Integer> pro = producers.get(metabolite);
			Collection<Integer> cons = consumers.get(metabolite);
			double sum = 0.00;
			
			for (int p : pro) {
				if (wtReference.get(model.getReactionId(p)) > 0) {
					sum += wtReference.get(model.getReactionId(p)) * model.getStoichiometricValue(metabolite, p);
				}
				
			}
			for (int c : cons) {
				if (wtReference.get(model.getReactionId(c)) < 0) {
					sum += wtReference.get(model.getReactionId(c)) * model.getStoichiometricValue(metabolite, c);
					
				}
				
			}
			
			turnovers.put(metabolite, sum);
			//	System.out.println(model.getMetaboliteId(metabolite)+"\t"+sum);
			
		}
	}
		
	public void createTOVariables() throws PropertyCastException, MandatoryPropertyException {
		//Create Turnover variables and equations
		
		for (int metabolite = 0; metabolite < model.getNumberOfMetabolites(); metabolite++) {
			
			//if the metabolite is in the list to remove, dont create a turnover variable
			if (getUnboundedMetabolites().contains(metabolite)) {
				continue;
			}
			
			LPProblemRow TURN = new LPProblemRow();
			
			Collection<Integer> pro = producers.get(metabolite);
			Collection<Integer> cons = consumers.get(metabolite);
			
			// for each reaction where the metabolite is in the right hand side of the equation
			for (int p : pro) {
				
				// add Vp to the TO calculation if the reaction has a upper bound bigger than zero
				if (model.getReactionConstraint(p).getUpperLimit() > 0) {
					
					int vpn = idToIndexVarMapings.get("TORV_" + model.getReactionId(p) + "(" + p + ")" + "_PST");
					try {
						TURN.addTerm(vpn, model.getStoichiometricValue(metabolite, p));
					} catch (LinearProgrammingTermAlreadyPresentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			// for each reaction where the metabolite is in the left hand side of the equation
			for (int c : cons) {
				
				// add Vn to the TO calculation if the reaction has a lower bound smaller than zero
				
				if (model.getReactionConstraint(c).getLowerLimit() < 0) {
					
					int vnn = idToIndexVarMapings.get("TORV_" + model.getReactionId(c) + "(" + c + ")" + "_NGT");
					try {
						TURN.addTerm(vnn, model.getStoichiometricValue(metabolite, c));
					} catch (LinearProgrammingTermAlreadyPresentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			
			//get the number of variables
			int varn = problem.getNumberVariables();
			//create turnover variable
			problem.addVariable("TO_" + metabolite, 0, defaultBound);
			//add variable to the var mappings
			idToIndexVarMapings.put("TO_" + metabolite, varn);
			indexToIdVarMapings.put(varn, "TO_" + metabolite);
			//add turnover variable to the equation
			try {
				TURN.addTerm(varn, -1.00000);
			} catch (LinearProgrammingTermAlreadyPresentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//add constraints to equation and add it to the problem
			LPConstraint turnOver = new LPConstraint(LPConstraintType.EQUALITY, TURN, 0.0000);
			problem.addConstraint(turnOver);
			
		}
		
	}
	
	@Override
	protected void createObjectiveFunction() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException {
		
		halfReactionsMILP();		
		calculateRs();
		createTOVariables();
		
		ReactionChangesList geneticModifications = getGeneticConditions().getReactionList();
		
		//modify regKs according to genetic modifications
		
		for (String rId : geneticModifications.keySet()) {
			
			int rIdx = model.getReactionIndex(rId);
			
			//if the reactions is to be modified in the forward direction and the reaction can occur in that direction
			if ((geneticModifications.get(rId) >= 0) && (model.getReactionConstraint(rId).getUpperLimit() > 0)) {
				
				//get its reactants
				for (int met : reactants.get(rIdx)) {
					
					//In the case of activated reactions (sometimes its usefull to underexpress activated pathways) there is no original regK
					//therefore a estimated one should be calculated based on the number of for reactions that use the same metabolite
					//the regK of the newly activated reactions should be similar to the one of existing reactions
					if ((regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK") == 0)) {
						double sum = 0.000;
						int number = 1;
						//calcular a media de regKs
						for (double r : regKmet.get(met)) {
							sum += r;
							number = number + 1;
						}
						
						//If that metabolite was not being produced in the original network, then assume that the activated reaction is the only consumer
						if (sum == 0) {
							sum = 1.0000;
							number = 1;
						}
						
						regKs.put(rId + "_" + model.getMetaboliteId(met) + "_regK", sum / number);
					}
					
					// modify all the regks
					double oldR = regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK");
					
					//if its a under-expression
					if (geneticModifications.get(rId) < 1) {
						regKs.put(rId + "_" + model.getMetaboliteId(met) + "_regK", oldR * geneticModifications.get(rId));
					} else {
						//new methodology for over-expressions that allows significant over-expression of reactions with low regKs
						regKs.put(rId + "_" + model.getMetaboliteId(met) + "_regK", ((oldR * geneticModifications.get(rId)) + (((1 - oldR) / 5) * geneticModifications.get(rId))));
					}
				}
			}
			
			//if the reactions is to be modified in the reverse direction and the reaction can occur in that direction
			if ((geneticModifications.get(rId) <= 0) && (model.getReactionConstraint(rId).getLowerLimit() < 0)) {
				
				//get its products
				for (int met : products.get(rIdx)) {
					
					//In the case of activated reactions(sometimes its usefull to underexpress activated pathways) there is no original regK~
					//therefore a estimated one should be calculated based on the number of for reactions that use the same metabolite
					//the regK of the newly activated reactions should be similar to the one of existing reactions
					if ((regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK") == 0)) {
						double sum = 0.000;
						int number = 1;
						//calcular a media de regKs
						for (double r : regKmet.get(met)) {
							sum += r;
							number = number + 1;
						}
						
						//If that metabolite was not being produced in the original network, then assume that the activated reaction is the only consumer
						if (sum == 0) {
							sum = 1.0000;
							number = 1;
						}
						
						regKs.put(rId + "_" + model.getMetaboliteId(met) + "_regK", sum / number);
					}
					
					// modify all the regks
					double oldR = regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK");
					if (geneticModifications.get(rId) > -1) {
						regKs.put(rId + "_" + model.getMetaboliteId(met) + "_regK", -oldR * geneticModifications.get(rId));
					} else {
						regKs.put(rId + "_" + model.getMetaboliteId(met) + "_regK", ((oldR * -geneticModifications.get(rId)) + (((1 - oldR) / 5) * -geneticModifications.get(rId))));
						
					}
					
				}
			}
		}
		
		// regK sum calculation for consumption reactions
		for (int metabolite = 0; metabolite < model.getNumberOfMetabolites(); metabolite++) {
			Collection<Integer> pro = producers.get(metabolite);
			Collection<Integer> cons = consumers.get(metabolite);
			double Temp = 0.000;
			for (int p : pro) {
				
				if (model.getReactionConstraint(p).getLowerLimit() < 0) {
					Temp += regKs.get(model.getReactionId(p) + "_" + model.getMetaboliteId(metabolite) + "_regK");
				}
			}
			
			for (int c : cons) {
				if (model.getReactionConstraint(c).getUpperLimit() > 0) {
					Temp += regKs.get(model.getReactionId(c) + "_" + model.getMetaboliteId(metabolite) + "_regK");
				}
			}
			rSum.put(metabolite, Temp);
			
		}
		
		//create constraints for genetic modifications
		
		for (String rId : geneticModifications.keySet()) {
			int rIdx = model.getReactionIndex(rId);
			
			//if the reactions is to be modified in the forward direction and it can occur in that direction
			if ((geneticModifications.get(rId) >= 0) && (model.getReactionConstraint(rId).getUpperLimit() > 0)) {
				
				//what kind of modification will be applied
				//If lower than 1, its an under expression/deletion
				if (geneticModifications.get(rId) < 1) {
					//get its reactants
					for (int met : reactants.get(rIdx)) {
						
						//if you delete a reaction with zero value in the wt this needs to have a value higher than zero
						if (rSum.get(met) == 0) {
							rSum.put(met, 1.000);
						}
						
						//create constraints
						
						try {
							LPProblemRow genMod = new LPProblemRow();
							
							int vpp = idToIndexVarMapings.get("TORV_" + rId + "(" + rIdx + ")" + "_PST");
							genMod.addTerm(vpp, -model.getStoichiometricValue(met, rIdx));
							genMod.addTerm(idToIndexVarMapings.get("TO_" + met), -regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK") / rSum.get(met));
							System.out.println("REG_POS_DOWN ("+rId+" , "+met+")= "+regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK")+"\tSUM="+rSum.get(met)+"\tSTOICH="+model.getStoichiometricValue(met, rIdx));
							LPConstraint under = new LPConstraint(LPConstraintType.LESS_THAN, genMod, 0.0000);
							problem.addConstraint(under);
							//when restraining the forward half of a reversible equation, make sure it is the only one active
							if (model.getReactionConstraint(rId).getLowerLimit() < 0) {
								LPProblemRow inac = new LPProblemRow();
								inac.addTerm(idToIndexVarMapings.get("w" + rIdx), 1);
								LPConstraint forceVn = new LPConstraint(LPConstraintType.EQUALITY, inac, 0.0000);
								problem.addConstraint(forceVn);
							}
							
						} catch (LinearProgrammingTermAlreadyPresentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}
				
				//what kind of modification will be applied
				//If higher than 1, its an over expression/activation
				if (geneticModifications.get(rId) > 1) {
					//create boolean restriction for over-expression problem and a counter to calculate the number of metabolites
					LPProblemRow boolRest = new LPProblemRow();
					double overExpressionMet = 0.000;
					
					//get its reactants
					for (int met : reactants.get(rIdx)) {
						
						//create constraints
						
						//If a reaction is activated and one of the reactants is not available, its R is going to be zero and rSum also be zero
						//because the average R for that metabolite is zero; therefore rSum can be set to 1 to prevent 0/0 problem
						if (rSum.get(met) == 0) {
							rSum.put(met, 1.000);
						}
						
						try {
							LPProblemRow genMod = new LPProblemRow();
							
							int vpp = idToIndexVarMapings.get("TORV_" + rId + "(" + rIdx + ")" + "_PST");
							genMod.addTerm(vpp, -model.getStoichiometricValue(met, rIdx));
							genMod.addTerm(idToIndexVarMapings.get("TO_" + met), -regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK") / rSum.get(met));
							System.out.println("REG_POS_UP ("+rId+" , "+met+")= "+regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK")+"\tSUM="+rSum.get(met)+"\tSTOICH="+model.getStoichiometricValue(met, rIdx));
							//Here the reaction will be forced to be greater than the higher restriction
							//In reality if a metabolite is limiting than the reaction should only be greater than the lesser restriction
							//therefore a boolean variable will be added to each restriction allowing only one of them to be active
							//the solver will choose the most biologically advantageous
							overExpressionMet += 1.0000;
							int varn = problem.getNumberVariables();
							problem.addIntVariable("y" + rId + met, 0, 1);
							idToIndexVarMapings.put("y" + rId + met, varn);
							indexToIdVarMapings.put(varn,"y" + rId + met);
							genMod.addTerm(varn, 1000000);
							boolRest.addTerm(varn, 1);
							//over-expression constraint
							LPConstraint over = new LPConstraint(LPConstraintType.GREATER_THAN, genMod, 0);
							problem.addConstraint(over);
						} catch (LinearProgrammingTermAlreadyPresentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
					//
					LPConstraint boolRestriction = new LPConstraint(LPConstraintType.LESS_THAN, boolRest, overExpressionMet - 1.0000);
					problem.addConstraint(boolRestriction);
					
				}
				
			}
			
			//if the reactions is to be modified in the reverse direction
			if ((geneticModifications.get(rId) <= 0) && (model.getReactionConstraint(rId).getLowerLimit() < 0)) {
				//what kind of modification will be applied
				//If higher than -1, its an under expression/deletion
				if (geneticModifications.get(rId) > -1) {
					//get its products
					for (int met : products.get(rIdx)) {
						//create constraints
						
						//if you delete a reaction with zero value in the wt it needs to have a value higher than zero
						if (rSum.get(met) == 0) {
							rSum.put(met, 1.000);
						}
						try {
							LPProblemRow genMod = new LPProblemRow();
							int vnn = idToIndexVarMapings.get("TORV_" + rId + "(" + rIdx + ")" + "_NGT");
							genMod.addTerm(vnn, -model.getStoichiometricValue(met, rIdx));
							genMod.addTerm(idToIndexVarMapings.get("TO_" + met), -regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK") / rSum.get(met));
							System.out.println("REG_NEG_DOWN ("+rId+" , "+met+")= "+regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK")+"\tSUM="+rSum.get(met)+"\tSTOICH="+model.getStoichiometricValue(met, rIdx));
							LPConstraint under = new LPConstraint(LPConstraintType.LESS_THAN, genMod, 0.0000);
							problem.addConstraint(under);
							//when restraining the reverse half of a reversible equation, make sure it is the only one active
							if (model.getReactionConstraint(rId).getUpperLimit() > 0) {
								LPProblemRow inac = new LPProblemRow();
								inac.addTerm(idToIndexVarMapings.get("y" + rIdx), 1);
								LPConstraint forceVp = new LPConstraint(LPConstraintType.EQUALITY, inac, 0.0000);
								problem.addConstraint(forceVp);
							}
							
						} catch (LinearProgrammingTermAlreadyPresentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				//what kind of modification will be applied
				//If higher than 1, its an over expression/activation
				if (geneticModifications.get(rId) < -1) {
					//create boolean restriction for over-expression problem
					LPProblemRow boolRest = new LPProblemRow();
					double overExpressionMet = 0.000;
					
					//get its reactants
					for (int met : products.get(rIdx)) {
						
						//create constraints
						
						//If a reaction is activated and one of the reactants is not available, its R is going to be zero and rSum also be zero
						//because the average R for that metabolite is zero; therefore rSum can be set to 1 to prevent 0/0 problem
						if (rSum.get(met) == 0) {
							rSum.put(met, 1.000);
						}
						
						try {
							LPProblemRow genMod = new LPProblemRow();
							
							int vnn = idToIndexVarMapings.get("TORV_" + rId + "(" + rIdx + ")" + "_NGT");
							genMod.addTerm(vnn, -model.getStoichiometricValue(met, rIdx));
							genMod.addTerm(idToIndexVarMapings.get("TO_" + met), -regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK") / rSum.get(met));
							System.out.println("REG_NEG_UP ("+rId+" , "+met+")= "+regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK")+"\tSUM="+rSum.get(met)+"\tSTOICH="+model.getStoichiometricValue(met, rIdx));
							//Here the reaction will be forced to be greater than the higher restriction
							//In reality if a metabolite is limiting than the reaction should only be greater than the lesser restriction
							//therefore a boolean variable will be added to each restriction allowing only one of them to be active~
							//the solver will choose the most biologically advantageous
							overExpressionMet += 1.0000;
							int varn = problem.getNumberVariables();
							problem.addIntVariable("w" + rId + met, 0, 1);
							idToIndexVarMapings.put("w" + rId + met, varn);
							indexToIdVarMapings.put(varn,"w" + rId + met);
							genMod.addTerm(varn, 1000000);
							boolRest.addTerm(varn, 1);
							//over-expression constraint
							LPConstraint over = new LPConstraint(LPConstraintType.GREATER_THAN, genMod, 0.0000);
							problem.addConstraint(over);
						} catch (LinearProgrammingTermAlreadyPresentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
					//
					LPConstraint boolRestriction = new LPConstraint(LPConstraintType.LESS_THAN, boolRest, overExpressionMet - 1.0000);
					problem.addConstraint(boolRestriction);
					
				}
				
			}
			
		}
		
		//for every comsumed metabolite create a equation that describes its share of the turnover
		//create objective function: minimize the differences between the wild-type shares and the mutant ones
		problem.setObjectiveFunction(new LPProblemRow(), false);
		
		for (int metabolite = 0; metabolite < model.getNumberOfMetabolites(); metabolite++) {
			
			//ignore unbounded metabolites
			if (getUnboundedMetabolites().contains(metabolite)) {
				continue;
			}
			
			Collection<Integer> pro = producers.get(metabolite);
			Collection<Integer> cons = consumers.get(metabolite);
			
			//for every reactions where the metabolite is in the right side of the equation
			for (int p : pro) {
				
				// only for reactions that can consume this metabolite (except drains)
				if ((model.getReactionConstraint(p).getLowerLimit() < 0) && (!getDrains().contains(p))) {
					
					int vnn = idToIndexVarMapings.get("TORV_" + model.getReactionId(p) + "(" + p + ")" + "_NGT");
					
					//for every reactions that was active in the reference network and manually activated ones
					if (regKs.get(model.getReactionId(p) + "_" + model.getMetaboliteId(metabolite) + "_regK") > 0) {
						
						LPProblemRow MORAS = new LPProblemRow();
						try {
							// Vn*S/R
							MORAS.addTerm(vnn, -model.getStoichiometricValue(metabolite, p) * (1 / regKs.get(model.getReactionId(p) + "_" + model.getMetaboliteId(metabolite) + "_regK")));
							
							//-2.TO/rSUM
							MORAS.addTerm(idToIndexVarMapings.get("TO_" + metabolite), -2 / rSum.get(metabolite));
							System.out.println("MORAS_NEG: stoich="+model.getStoichiometricValue(metabolite, p)+"\tREG= "+regKs.get(model.getReactionId(p) + "_" + model.getMetaboliteId(metabolite) + "_regK")+"\tRSUM="+rSum.get(metabolite));
							//get the number of variables
							int varn = problem.getNumberVariables();
							//create Minimization variable
							problem.addVariable("MORA_" + metabolite + "_" + p + "P", 0, defaultBound);
							problem.addVariable("MORA_" + metabolite + "_" + p + "N", 0, defaultBound);
							//add variable to the var mappings
							idToIndexVarMapings.put("MORA_" + metabolite + "_" + p + "P", varn);
							indexToIdVarMapings.put(varn, "MORA_" + metabolite + "_" + p + "P");
							idToIndexVarMapings.put("MORA_" + metabolite + "_" + p + "N", varn + 1);
							indexToIdVarMapings.put(varn + 1, "MORA_" + metabolite + "_" + p + "N");
							//add MORA variable to the equation
							MORAS.addTerm(varn, -1);
							MORAS.addTerm(varn + 1, 1);
							//create constraint
							LPConstraint moras = new LPConstraint(LPConstraintType.EQUALITY, MORAS, -turnovers.get(metabolite) / rSum.get(metabolite));
							problem.addConstraint(moras);
							//add difference to the objective function as a absolute value 
							objTerms.add(new VarTerm(varn, 1, 0));
							objTerms.add(new VarTerm(varn + 1, 1, 0));
							
						}
						
						catch (LinearProgrammingTermAlreadyPresentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//for reactions that are inactive, prevent their appearance (min: Vi.Si.penalty)
					//it is more relevant to keep inactive reactions inactive then keep the active ones in the right proportion
					else {
						objTerms.add(new VarTerm(vnn, -model.getStoichiometricValue(metabolite, p) * getPenalty(), 0));
					}
				}
			}
			
			for (int c : cons) {
				// consumption drains were usefull in the calculation of regks but now they should be ignored
				
				if ((model.getReactionConstraint(c).getUpperLimit() > 0) && (!getDrains().contains(c))) {
					
					int vpp = idToIndexVarMapings.get("TORV_" + model.getReactionId(c) + "(" + c + ")" + "_PST");
					
					if (regKs.get(model.getReactionId(c) + "_" + model.getMetaboliteId(metabolite) + "_regK") > 0) {
						
						LPProblemRow MORAS = new LPProblemRow();
						try {
							
							// Vn*S*R
							MORAS.addTerm(vpp, -model.getStoichiometricValue(metabolite, c) * (1 / regKs.get(model.getReactionId(c) + "_" + model.getMetaboliteId(metabolite) + "_regK")));
							//-2.TO/rSUM
							MORAS.addTerm(idToIndexVarMapings.get("TO_" + metabolite), -2 / rSum.get(metabolite));
							
							System.out.println("MORAS_POS: stoich="+model.getStoichiometricValue(metabolite, c)+"\tREG= "+regKs.get(model.getReactionId(c) + "_" + model.getMetaboliteId(metabolite) + "_regK")+"\tRSUM="+rSum.get(metabolite));
							//get the number of variables
							int varn = problem.getNumberVariables();
							//create Minimization variable
							problem.addVariable("MORA_" + metabolite + "_" + c + "P", 0, defaultBound);
							problem.addVariable("MORA_" + metabolite + "_" + c + "N", 0, defaultBound);
							//add variable to the var mappings
							idToIndexVarMapings.put("MORA_" + metabolite + "_" + c + "P", varn);
							indexToIdVarMapings.put(varn, "MORA_" + metabolite + "_" + c + "P");
							idToIndexVarMapings.put("MORA_" + metabolite + "_" + c + "N", varn + 1);
							indexToIdVarMapings.put(varn + 1, "MORA_" + metabolite + "_" + c + "N");
							
							//add minimization variable to the equation
							
							MORAS.addTerm(varn, -1);
							MORAS.addTerm(varn + 1, 1);
							//add constraint
							LPConstraint moras = new LPConstraint(LPConstraintType.EQUALITY, MORAS, -turnovers.get(metabolite) / rSum.get(metabolite));
							
							problem.addConstraint(moras);
							//add difference to the objective function as a absolute value
							objTerms.add(new VarTerm(varn, 1, 0));
							objTerms.add(new VarTerm(varn + 1, 1, 0));
							
						} catch (LinearProgrammingTermAlreadyPresentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//for reactions that are inactive, prevent their appearance (min: Vi.Si.penalty)
					//it is more relevant to keep inactive reactions inactive then keep the active ones in the right proportion
					else {
						objTerms.add(new VarTerm(vpp, -model.getStoichiometricValue(metabolite, c) * getPenalty(), 0));
					}
				}
			}
		}
	}
	
	@Override
	public String getObjectiveFunctionToString() {
		return "Σ (1/|wt|) * |v-wt|";
	}
	
//	@Override
//		public SteadyStateSimulationResult convertLPSolutionToSimulationSolution(
//				LPSolution solution) throws PropertyCastException,
//				MandatoryPropertyException {
//			
//		
//			for(String id: idToIndexVarMapings.keySet()){
//				System.out.println(id + "\t" + solution.getValues().get(idToIndexVarMapings.get(id)));
//			}
////			System.out.println(turnovers);
////			System.out.println(model.getMetaboliteId(96));
////			System.out.println(model.getReactionId(169));
////			System.out.println(regKs.get(model.getReactionId(169)+"_"+model.getMetaboliteId(96)+"_regK"));
////
////		
////
//			return super.convertLPSolutionToSimulationSolution(solution);
//		}

}
