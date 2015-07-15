package pt.uminho.ceb.biosystems.mew.core.simulation.mfa;

public enum MFAConstraintSource {

	GeneticCondition {
		@Override
		public String toString(){
			return "Genetic condition";
		}
	},
	
	EnvironmentalCondition {
		@Override
		public String toString(){
			return "Environmental Condition";
		}
	},
	
	MeasuredFlux {
		@Override
		public String toString(){
			return "Measured Flux";
		}
	},
	
	FluxRatio {
		@Override
		public String toString(){
			return "Flux Ratio";
		}
	},
	
	Model {
		@Override
		public String toString(){
			return "Model";
		}
	};
	
	@Override
	public String toString(){
		return this.toString();
	}
}
