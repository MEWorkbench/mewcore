package pt.uminho.ceb.biosystems.mew.core.criticality;

import java.util.HashSet;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.configuration.ModelConfiguration;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;

public class DeadEnds {
	
	private Container				_container;
	private EnvironmentalConditions	_environmentalConditions;
	
	public DeadEnds(Container container, EnvironmentalConditions environmentalConditions) {
		_container = container;
		_environmentalConditions = environmentalConditions;
	}
	
	public Set<String> identifyReactionWithDeadEnds() {
		
		Set<String> met = removeDeadEndsIteratively(_container.clone(), /* useComp */true);
		
		Set<String> reactions = new HashSet<String>();
		
		for (String m : met) {
			reactions.addAll(_container.getMetabolite(m).getReactionsId());
		}
		
		return reactions;
	}
	
	public Set<String> removeDeadEndsIteratively(Container cont, boolean useComp) {
		Set<String> allDeadEnds = new HashSet<String>();
		
		Set<String> aux = removeMetaboliteDeadEnds(cont, useComp);
		while (aux.size() > 0) {
			allDeadEnds.addAll(aux);
			aux = removeMetaboliteDeadEnds(cont, useComp);
		}
		
		return allDeadEnds;
	}
	
	public Set<String> removeMetaboliteDeadEnds(Container cont, boolean useComp) {
		
		Set<String> dead = identifyDeadEnds(cont, useComp);
		cont.removeMetaboliteAndItsReactions(dead);
		
		return dead;
	}
	
	public Set<String> identifyDeadEnds(Container container, boolean useCompartments) {
		
		// boolean useCompartments = true;
		
		Set<String> metabGaps = new HashSet<String>();
		
		int dead = 0;
		if (!useCompartments) {
			
			for (MetaboliteCI metab : container.getMetabolites().values()) {
				
				Set<String> reactions = metab.getReactionsId();
				
				// stopFlag= -1 Nenhuma reaction encontrada que produza/consuma
				// metabolito
				int stopFlag = -1;
				for (String reactionId : reactions) {
					ReactionCI reaction = container.getReaction(reactionId);
					
					stopFlag = nextFlag(stopFlag, reaction, null, metab.getId());
					
					if (stopFlag == 3) break;
				}
				
				if (stopFlag != 3) {
					System.out.println(metab.getName() + "\t" + stopFlag + "\t" + metab.getId());
					dead++;
					metabGaps.add(metab.getId());
				}
			}
		} else {
			for (CompartmentCI comp : container.getCompartments().values()) {
				
				for (String metId : comp.getMetabolitesInCompartmentID()) {
					MetaboliteCI metab = container.getMetabolite(metId);
					
					Set<String> reactions = metab.getReactionsId();
					
					// stopFlag= -1 Nenhuma reaction encontrada que
					// produza/consuma metabolito
					int stopFlag = -1;
					for (String reactionId : reactions) {
						ReactionCI reaction = container.getReaction(reactionId);
						
						stopFlag = nextFlag(stopFlag, reaction, comp.getId(), metab.getId());
						
						if (stopFlag == 3) break;
					}
					
					if (stopFlag != 3) {
						System.out.println(metab.getName() + "\t" + stopFlag + "\t" + metab.getId() + "\t" + comp.getId());
						dead++;
						metabGaps.add(metab.getId());
					}
				}
			}
		}
		System.out.println("Number of dead ends: " + dead);
		return metabGaps;
	}
	
	// FIXME: Change flag to something static (enum? or constant) in a diferent
	// class
	// significado da flag
	// -1 -> nenhuma reaction encontrada com o metabolito
	// 0 -> Reaction reversivel
	// 1 -> Reaction de producao do metabolito
	// 2 -> Reaction de consumo do metabolito
	// 3 -> Metabolito Balanceado
	
	private int nextFlag(int flagAnt, ReactionCI reaction, String comp, String metab) {
		int flag = 0;
		
		if (comp == null) {
			if (isReversible(reaction)) {
				if (flagAnt >= 0)
					flag = 3;
				else
					flag = 0;
			} else {
				if ((flagAnt == -1 || flagAnt == 1) && reaction.getProducts().containsKey(metab))
					flag = 1;
				else if ((flagAnt == -1 || flagAnt == 2) && reaction.getReactants().containsKey(metab))
					flag = 2;
				else
					flag = 3;
			}
			
		} else {
			boolean containsInProducts = reaction.containsMetaboliteInProducts(metab, comp);
			boolean containsInReactants = reaction.containsMetaboliteInReactants(metab, comp);
			
			if (containsInProducts == false && containsInReactants == false)
				flag = flagAnt;
			else {
				if (isReversible(reaction)) {
					if (flagAnt >= 0)
						flag = 3;
					else
						flag = 0;
				} else {
					if ((flagAnt == -1 || flagAnt == 1) && containsInProducts)
						flag = 1;
					else if ((flagAnt == -1 || flagAnt == 2) && containsInReactants)
						flag = 2;
					else
						flag = 3;
				}
			}
			
		}
		
		return flag;
	}
	
	boolean isReversible(ReactionCI reaction) {
		boolean isReversible = false;
		if (_environmentalConditions != null && _environmentalConditions.containsKey(reaction.getId())) {
			ReactionConstraint constraint = _environmentalConditions.getReactionConstraint(reaction.getId());
			if (constraint.getLowerLimit() != 0 && constraint.getUpperLimit() != 0) isReversible = true;
		} else {
			isReversible = reaction.isReversible();
		}
		
		return isReversible;
	}
	
	public static void main(String... args) throws Exception {
		String test = "iaf"; // imm
		boolean anaerobic = false;
		
		String file = null;
		String envfile = null;
		
		if (test.equals("iaf")) {
			file = "files/iAF1260_full/iAF1260.conf";
			if (anaerobic) envfile = "files/iAF1260_full/iAF1260_anaerobic.env";
		} else {
			file = "files/iMM904/iMM904.conf";
			if (anaerobic) envfile = "files/iMM904/iMM904_anaerobic.env";
		}
		
		ModelConfiguration conf = new ModelConfiguration(file);
		
		Container container = conf.getContainer();
		
		EnvironmentalConditions env = (envfile != null) ? EnvironmentalConditions.readFromFile(envfile, ",") : null;
		
		DeadEnds deadEndsAnalysis = new DeadEnds(container, env);
		
		Set<String> deadEnds = deadEndsAnalysis.identifyReactionWithDeadEnds();
		int i=0;
		for(String de : deadEnds){
			System.out.println("["+i+"] "+de);
			i++;
		}
	}
}
