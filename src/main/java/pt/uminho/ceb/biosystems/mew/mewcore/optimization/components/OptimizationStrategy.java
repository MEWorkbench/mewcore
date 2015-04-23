package pt.uminho.ceb.biosystems.mew.mewcore.optimization.components;

public enum OptimizationStrategy {
	
	/**
	 * Reaction knockouts
	 */
	RK,
	/**
	 * Gene knockouts
	 */
	GK{
		@Override
		public boolean isGeneBasedOptimization(){
			return true;
		}
	},
	/**
	 * Reaction over/under expression
	 */
	ROU{
		@Override
		public boolean isOverUnderExpressionOptimization(){
			return true;
		}
	},
	/**
	 * Gene over/under expression
	 */
	GOU{
		@Override
		public boolean isGeneBasedOptimization(){
			return true;
		}
		@Override
		public boolean isOverUnderExpressionOptimization(){
			return true;
		}	
	};
	
	public boolean isGeneBasedOptimization(){
		return false;
	}
	
	public boolean isOverUnderExpressionOptimization(){
		return false;
	}
	

}
