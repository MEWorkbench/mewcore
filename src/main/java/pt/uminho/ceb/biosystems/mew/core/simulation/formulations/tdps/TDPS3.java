package pt.uminho.ceb.biosystems.mew.core.simulation.formulations.tdps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.MILPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.utilities.math.MathUtils;

public class TDPS3 extends AbstractSSReferenceSimulation<MILPProblem> {
	
	public static double						DEFAULT_PENALTY	= 10;
																
	public Map<Integer, Collection<Double>>		regKmet			= new HashMap<Integer, Collection<Double>>();
	public Map<String, Double>					regKs			= new TreeMap<String, Double>();
	public Map<Integer, Collection<Integer>>	producers		= new HashMap<Integer, Collection<Integer>>();
	public Map<Integer, Collection<Integer>>	consumers		= new HashMap<Integer, Collection<Integer>>();
	public Map<Integer, Collection<Integer>>	products		= new HashMap<Integer, Collection<Integer>>();
	public Map<Integer, Collection<Integer>>	reactants		= new HashMap<Integer, Collection<Integer>>();
	public Map<Integer, Double>					rSum			= new HashMap<Integer, Double>();
	public Map<Integer, Double>					turnovers		= new HashMap<Integer, Double>();
																
	public double								defaultBound	= 100000000;
																
	protected IOverrideReactionBounds			overrideBounds	= null;
																
	/**
	 * This method describes a simulation approach that minimizes the differences between the the
	 * share of substrate consumed by a reaction in the wildtype and in the mutant
	 * 
	 * @param model
	 */
	public TDPS3(ISteadyStateModel model) {
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
	
	private void precomputations() throws LinearProgrammingTermAlreadyPresentException {
		
		//split reactions MILP
		Map<String, Integer> newVars = SimulationProperties.splitReactionsMILP(problem, model, overrideBounds, getDrains());
		putNewVariables(newVars);
		
		//compute consumers and producers of each metabolite (index based)
		SimulationProperties.computeConsumersProducersPerIndex(model, producers, consumers, getUnboundedMetabolites());
		
		//compute products and reactants of each reaction (index based)
		SimulationProperties.computeProductsReactantsPerIndex(model, products, reactants, getUnboundedMetabolites());
		
		//calculate production turnovers
		turnovers = SimulationProperties.getTurnOverCalculationByIndex(getModel(), getWTReference(), getUnboundedMetabolites());
		
		//calculate turnover fractions ( F^{R}_{m,n} )- RS in the old code
		calculateReferenceTurnoverFractions();
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
	
	public void setUnboundedMetabolites(Set<Integer> unboundedMetabolites) {
		properties.put(SimulationProperties.TDPS_UNBOUNDED_METABOLITES, unboundedMetabolites);
	}
	
	@SuppressWarnings("unchecked")
	public Set<Integer> getUnboundedMetabolites() {
		Set<Integer> unboundedMetab = null;
		try {
			unboundedMetab = ManagerExceptionUtils.testCast(properties, Set.class, SimulationProperties.TDPS_UNBOUNDED_METABOLITES, true);
		} catch (PropertyCastException e) {
			System.err.println("The property " + e.getProperty() + " was ignored!!\n Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {
			System.err.println("The property " + e.getProperty() + " is missing and it is mandatory!!\n Reason: " + e.getMessage());
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
	public void createVariables() {
		super.createVariables();
		try {
			precomputations();
			createTOVariables();
		} catch (LinearProgrammingTermAlreadyPresentException e1) {
			e1.printStackTrace();
		}
	}
	
	@Override
	public void createConstraints() {
		super.createConstraints();
		updateFractionsAccordingToGeneticConditions();
	}
	
	public void preSimulateActions() {
		_recreateProblem = true;
	};
	
	/**
	 * Pre-compute the reference turnover fraction values for all metabolites.
	 * F^{R}_{m,n}
	 *  
	 * @throws PropertyCastException
	 * @throws MandatoryPropertyException
	 */
	public void calculateReferenceTurnoverFractions() throws PropertyCastException, MandatoryPropertyException {
		
		//calculate the regK for each reaction's substrates		
		for (int metabolite = 0; metabolite < model.getNumberOfMetabolites(); metabolite++) {
			double TM = turnovers.get(metabolite);
			Collection<Integer> pro = producers.get(metabolite);
			Collection<Integer> cons = consumers.get(metabolite);
			regKmet.put(metabolite, new ArrayList<Double>());
			
			for (int p : pro) {
				//If a reaction can occur in the reverse direction create the R value
				if (overrideBounds.getReactionConstraint(p).getLowerLimit() < 0) {
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
				if (overrideBounds.getReactionConstraint(c).getUpperLimit() > 0) {
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
	}
	
	public void createTOVariables() throws PropertyCastException, MandatoryPropertyException, LinearProgrammingTermAlreadyPresentException {
		
		//Create Turnover variables and equations		
		for (int metabolite = 0; metabolite < model.getNumberOfMetabolites(); metabolite++) {
			
			//if the metabolite is in the list to remove, dont create a turnover variable
			if (getUnboundedMetabolites().contains(metabolite)) {
				continue;
			}
			
			LPProblemRow TURN = new LPProblemRow();			
			
			// for each reaction where the metabolite is in the right hand side of the equation
			for (int p : producers.get(metabolite)) {
				
				// add Vp to the TO calculation if the reaction has a upper bound bigger than zero
				if (overrideBounds.getReactionConstraint(p).getUpperLimit() > 0) {
//					System.out.println(">>>> TDPS: ("+p +"):"+model.getReactionId(p)+ "\t BOUNDS=["+overrideBounds.getReactionConstraint(p).getLowerLimit()+","+overrideBounds.getReactionConstraint(p).getUpperLimit()+"]");
//					MapUtils.prettyPrint(idToIndexVarMapings);
					int vpn = idToIndexVarMapings.get("TORV_" + model.getReactionId(p) + "(" + p + ")" + "_PST");
					TURN.addTerm(vpn, model.getStoichiometricValue(metabolite, p));
				}
			}
			
			// for each reaction where the metabolite is in the left hand side of the equation
			for (int c : consumers.get(metabolite)) {
				
				// add Vn to the TO calculation if the reaction has a lower bound smaller than zero				
				if (overrideBounds.getReactionConstraint(c).getLowerLimit() < 0) {					
					int vnn = idToIndexVarMapings.get("TORV_" + model.getReactionId(c) + "(" + c + ")" + "_NGT");
					TURN.addTerm(vnn, -model.getStoichiometricValue(metabolite, c));
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
			TURN.addTerm(varn, -1.00000);
			
			//add constraints to equation and add it to the problem
			LPConstraint turnOver = new LPConstraint(LPConstraintType.EQUALITY, TURN, 0.0000);			
			problem.addConstraint(turnOver);			
		}
	}
	
	/**
	 * Update the turnover fractions to accomodate the genetic modifications.
	 * 
	 * F_{m,n}
	 */
	private void updateFractionsAccordingToGeneticConditions() {
		
		ReactionChangesList geneticModifications = getGeneticConditions().getReactionList();
		
		//modify regKs according to genetic modifications		
		for (String rId : geneticModifications.keySet()) {
			
			int rIdx = model.getReactionIndex(rId);
			
			//if the reactions is to be modified in the forward direction and the reaction can occur in that direction
			if ((geneticModifications.get(rId) >= 0) && (overrideBounds.getReactionConstraint(rId).getUpperLimit() > 0)) {
				
				//get its reactants
				for (int met : reactants.get(rIdx)) {
					
					//In the case of activated reactions (sometimes its usefull to underexpress activated pathways) there is no original regK
					//therefore a estimated one should be calculated based on the number of for reactions that use the same metabolite
					//the regK of the newly activated reactions should be similar to the one of existing reactions
					if ((regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK") == 0)) {
						double sum = MathUtils.sumNumberCollection(regKmet.get(met));						
						double val = (sum==0) ? 1.0 : sum / (regKmet.get(met).size()+1);						
						regKs.put(rId + "_" + model.getMetaboliteId(met) + "_regK", val);
					}
					
					
					
					//if its a under-expression
					//NOTE: to simplify, simply uncomment next line and comment the rest - values will be different
					//regKs.put(rId + "_" + model.getMetaboliteId(met) + "_regK", oldR * geneticModifications.get(rId));

					// modify all the regks
					double oldR = regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK");
					if (geneticModifications.get(rId) < 1) {
						regKs.put(rId + "_" + model.getMetaboliteId(met) + "_regK", oldR * geneticModifications.get(rId));
					} else {
						//new methodology for over-expressions that allows significant over-expression of reactions with low regKs
						regKs.put(rId + "_" + model.getMetaboliteId(met) + "_regK", ((oldR * geneticModifications.get(rId)) + (((1 - oldR) / 5) * geneticModifications.get(rId))));
					}
				}
			}
			
			//if the reactions is to be modified in the reverse direction and the reaction can occur in that direction
			if ((geneticModifications.get(rId) <= 0) && (overrideBounds.getReactionConstraint(rId).getLowerLimit() < 0)) {
				
				//get its products
				for (int met : products.get(rIdx)) {
					
					//In the case of activated reactions(sometimes its usefull to underexpress activated pathways) there is no original regK~
					//therefore a estimated one should be calculated based on the number of for reactions that use the same metabolite
					//the regK of the newly activated reactions should be similar to the one of existing reactions
					if ((regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK") == 0)) {
						double sum = MathUtils.sumNumberCollection(regKmet.get(met));						
						double val = (sum==0) ? 1.0 : sum / (regKmet.get(met).size()+1);						
						regKs.put(rId + "_" + model.getMetaboliteId(met) + "_regK", val);
					}
					
					//if its a under-expression
					//NOTE: to simplify, simply uncomment next line and comment the rest - values will be different
					//regKs.put(rId + "_" + model.getMetaboliteId(met) + "_regK", -oldR * geneticModifications.get(rId));
					
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
//							System.out.println("REG_POS_DOWN (" + rId + " , " + met + ")= " + regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK") + "\tSUM=" + rSum.get(met) + "\tSTOICH=" + model.getStoichiometricValue(met, rIdx));
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
//							System.out.println("REG_POS_UP (" + rId + " , " + met + ")= " + regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK") + "\tSUM=" + rSum.get(met) + "\tSTOICH=" + model.getStoichiometricValue(met, rIdx));
									
							//Here the reaction will be forced to be greater than the higher restriction
							//In reality if a metabolite is limiting than the reaction should only be greater than the lesser restriction
							//therefore a boolean variable will be added to each restriction allowing only one of them to be active
							//the solver will choose the most biologically advantageous
							overExpressionMet += 1.0000;
							int varn = problem.getNumberVariables();
							problem.addIntVariable("y" + rId + met, 0, 1);
							idToIndexVarMapings.put("y" + rId + met, varn);
							indexToIdVarMapings.put(varn, "y" + rId + met);
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
							genMod.addTerm(vnn, model.getStoichiometricValue(met, rIdx));
							genMod.addTerm(idToIndexVarMapings.get("TO_" + met), -regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK") / rSum.get(met));
//							System.out.println("REG_NEG_DOWN (" + rId + " , " + met + ")= " + regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK") + "\tSUM=" + rSum.get(met) + "\tSTOICH="
//									+ model.getStoichiometricValue(met, rIdx));
									
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
							genMod.addTerm(vnn, model.getStoichiometricValue(met, rIdx));
							genMod.addTerm(idToIndexVarMapings.get("TO_" + met), -regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK") / rSum.get(met));
//							System.out.println("REG_NEG_UP (" + rId + " , " + met + ")= " + regKs.get(rId + "_" + model.getMetaboliteId(met) + "_regK") + "\tSUM=" + rSum.get(met) + "\tSTOICH="
//									+ model.getStoichiometricValue(met, rIdx));
									
							//Here the reaction will be forced to be greater than the higher restriction
							//In reality if a metabolite is limiting than the reaction should only be greater than the lesser restriction
							//therefore a boolean variable will be added to each restriction allowing only one of them to be active~
							//the solver will choose the most biologically advantageous
							overExpressionMet += 1.0000;
							int varn = problem.getNumberVariables();
							problem.addIntVariable("w" + rId + met, 0, 1);
							idToIndexVarMapings.put("w" + rId + met, varn);
							indexToIdVarMapings.put(varn, "w" + rId + met);
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
	}
	
	@Override
	protected void createObjectiveFunction() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException {
		
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
							MORAS.addTerm(vnn, model.getStoichiometricValue(metabolite, p) * (1 / regKs.get(model.getReactionId(p) + "_" + model.getMetaboliteId(metabolite) + "_regK")));
							//-2.TO/rSUM
							MORAS.addTerm(idToIndexVarMapings.get("TO_" + metabolite), -2 / rSum.get(metabolite));
							
//							System.out.println("MORAS_NEG: stoich=" + model.getStoichiometricValue(metabolite, p) + "\tREG= "
//									+ regKs.get(model.getReactionId(p) + "_" + model.getMetaboliteId(metabolite) + "_regK") + "\tRSUM=" + rSum.get(metabolite));
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
						objTerms.add(new VarTerm(vnn, model.getStoichiometricValue(metabolite, p) * getPenalty(), 0));
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
							
//							System.out.println("MORAS_POS: stoich=" + model.getStoichiometricValue(metabolite, c) + "\tREG= "
//									+ regKs.get(model.getReactionId(c) + "_" + model.getMetaboliteId(metabolite) + "_regK") + "\tRSUM=" + rSum.get(metabolite));
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
		return "This objective function string would not fit your screen!";
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
