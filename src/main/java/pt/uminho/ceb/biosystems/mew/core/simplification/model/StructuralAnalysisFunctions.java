package pt.uminho.ceb.biosystems.mew.core.simplification.model;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;

public class StructuralAnalysisFunctions {

	public static EquivalentFluxes identifyEquivalentFluxes(ISteadyStateModel model)
	{

		List<ListEquivalentReactions> resultList = new ArrayList<ListEquivalentReactions>();

		
		
		for(int i=0;i<model.getNumberOfMetabolites();i++)
		{
			int pos[] = {0,0};
			int num = 0;
			double [] mat = model.getStoichiometricMatrix().getRow(i);
			double [] nums = {0,0};
			for(int a=0; a < model.getNumberOfReactions() && num < 3; a++) 
			{
				if(mat[a]!=0) {
					if (num < 2) {
						nums[num]=mat[a];
						pos[num]=a;
					}
					num++;
				}
			}

			if(num==2 && (nums[0]+nums[1])==0 && !model.getReaction(pos[0]).getType().equals(ReactionType.DRAIN) && !model.getReaction(pos[1]).getType().equals(ReactionType.DRAIN))
			{
				resultList = addEquivalence(model.getReactionId(pos[0]), model.getReactionId(pos[1]),resultList);
			}
		}
		
		return new EquivalentFluxes(resultList);
	}
	
	public static int hasEquivalences (String reactionId, List<ListEquivalentReactions> equivalenceList)
	{
		int res = -1;

		for(int i=0; res < 0 && i < equivalenceList.size(); i++)
		{
			ListEquivalentReactions ler = equivalenceList.get(i);
			if (ler.containsReaction(reactionId)) res = i;
		}

		return res;
	}

	/** Adds a new aquivalence between two variables / fluxes */
	public static List<ListEquivalentReactions> addEquivalence (String reaction1, String reaction2, List<ListEquivalentReactions> list)
	{
		int posR1 = hasEquivalences (reaction1, list);
		int posR2 = hasEquivalences (reaction2, list);

		if( posR1 == -1 && posR2 == -1) // none of the fluxes are in any equivalence
		{
			ListEquivalentReactions ler = new ListEquivalentReactions();
			ler.addReaction(reaction1);
			ler.addReaction(reaction2);
			list.add(ler);
		}
		else if (posR1 >= 0 && posR2 == -1) // R1 is in an equivalence - join R2
		{
			ListEquivalentReactions ler = list.get(posR1);
			ler.addReaction(reaction2);
		}
		else if (posR1 == -1 && posR2 >= 0) // R2 is in an equivalence - join R1
		{
			ListEquivalentReactions ler = list.get(posR2);
			ler.addReaction(reaction1);
		}
		else if (posR1 != posR2) // both are - join the two vectors
		{
			ListEquivalentReactions ler1 = list.get(posR1);
			ListEquivalentReactions ler2 = list.get(posR2);
			ler1.joinLists(ler2);
			list.remove(ler2);
		}
		return list;
	}

	public static ZeroValueFluxes identifyZeroValuesFromStoichiometry(ISteadyStateModel model) 
	{
		List<String> zeroValuesFluxes = new ArrayList<String>();

		for(int i=0;i< model.getNumberOfMetabolites();i++)
		{
			int pos = -1;
			int num = 0;
			for(int a=0; a < model.getNumberOfReactions() && num < 2; a++) 
			{
				if(model.getStoichiometricValue(i, a) !=0) {
					if (num == 0) pos=a;
					num++;
				}
			}

			if(num==1)
				if ( !zeroValuesFluxes.contains(model.getReactionId(pos)) )
					zeroValuesFluxes.add(model.getReactionId(pos));
		}

		ZeroValueFluxes result = new ZeroValueFluxes(zeroValuesFluxes);
		return result;
	}

}
