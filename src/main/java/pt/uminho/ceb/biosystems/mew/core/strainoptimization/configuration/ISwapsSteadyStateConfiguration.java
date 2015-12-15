package pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration;

import java.util.List;
import java.util.Map;

public interface ISwapsSteadyStateConfiguration extends IGenericConfiguration {
	
	public int getMaxAllowedSwaps();
	
	public void setMaxAllowedSwaps(int maxAllowedSwaps);
	
	public Map<String, List<String>> getReactionSwapMap();
	
	public void setReactionSwapMap(Map<String, List<String>> reactionSwapMap);

}
