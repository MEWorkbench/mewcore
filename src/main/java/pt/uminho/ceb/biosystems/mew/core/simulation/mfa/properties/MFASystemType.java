package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.properties;


public enum MFASystemType {
	
	underdetermined {
		public String getPropertyDescriptor(){
			return MFAProperties.UNDERDETERMINED_SYSTEM;
		}
		
		@Override
		public String toString(){
			return "Underdetermined System";
		}
	},
	
	determined{
		public String getPropertyDescriptor(){
			return MFAProperties.DETERMINED_SYSTEM;
		}
		
		@Override
		public String toString(){
			return "Determined System";
		}
	},
	
	overdetermined{
		public String getPropertyDescriptor(){
			return MFAProperties.OVERDETERMINED_SYSTEM;
		}
		
		@Override
		public String toString(){
			return "Overdetermined System";
		}
	};
	
	public String getPropertyDescriptor(){
		return this.getPropertyDescriptor();
	}
	
	@Override
	public String toString(){
		return this.toString();
	}
}
