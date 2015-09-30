package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs;

public enum ObjectiveFunctionParameterTypes2 {
	
	GENERIC_BOOLEAN,
	GENERIC_INT,
	GENERIC_DOUBLE,
	GENERIC_STRING,
	LIST;
	
	private ObjectiveFunctionParameterTypes2 inner;

	ObjectiveFunctionParameterTypes2 of(ObjectiveFunctionParameterTypes2 inner){
		this.inner = inner;
		return this;
	}

	public ObjectiveFunctionParameterTypes2 getInner() {
		return inner;
	}

}
