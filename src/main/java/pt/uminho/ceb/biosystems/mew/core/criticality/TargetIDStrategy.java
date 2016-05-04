package pt.uminho.ceb.biosystems.mew.core.criticality;

public enum TargetIDStrategy {
	
	IDENTIFY_CRITICAL {
		@Override
		public String getTag() {
			return "C";
		}
	},
	IDENTIFY_ZEROS {
		@Override
		public String getTag() {
			return "Z";
		}
	},
	IDENTIFY_EQUIVALENCES {
		@Override
		public String getTag() {
			return "EQ";
		}
	},
	IDENTIFY_NONGENE_ASSOCIATED {
		@Override
		public String getTag() {
			return "NG";
		}
	},
	IDENTIFY_DRAINS_TRANSPORTS {
		@Override
		public String getTag() {
			return "DT";
		}
	},
	IDENTIFY_PATHWAY_RELATED {
		@Override
		public String getTag() {
			return "P";
		}
	},
	IDENTIFY_HIGH_CARBON_RELATED {
		@Override
		public String getTag() {
			return "HC";
		}
	},
	IDENTIFY_NO_FLUX_WT {
		@Override
		public String getTag() {
			return "NF";
		}
	},
	IDENTIFY_EXPERIMENTAL {
		@Override
		public String getTag() {
			return "EXP";
		}
	};
	
	public abstract String getTag();
}
