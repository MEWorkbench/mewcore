package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions;

import java.io.Serializable;

public enum ObjectiveFunctionParameterType implements Serializable{

	/**
	 * IDENTIFIER
	 */
	OF_ID,
	
	/**
	 * GENERIC
	 */
	INT,
	DOUBLE,
	BOOLEAN,
	STRING,
	OBJECT,
	
	/**
	 * MEW SPECIFIC
	 */
	REACTION,
	REACTION_BIOMASS,
	REACTION_PRODUCT,
	REACTION_SUBSTRATE,
	METABOLITE,
	MODEL,
	CONTAINER,
	SOLVER,
	
	/**
	 * COLLECTIONS
	 */	
	LIST{
		public ObjectiveFunctionParameterType of(ObjectiveFunctionParameterType inner){
			this.inner = inner;
			return this;
		}	
	},
	SET{
		public ObjectiveFunctionParameterType of(ObjectiveFunctionParameterType inner){
			this.inner = inner;
			return this;
		}
	};
	
	
	protected ObjectiveFunctionParameterType inner;

	public ObjectiveFunctionParameterType of(ObjectiveFunctionParameterType inner){
		this.inner = null;
		return this;
	}

	public ObjectiveFunctionParameterType getInner() {
		return inner;
	}
	
}
