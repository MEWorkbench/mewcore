package pt.uminho.ceb.biosystems.mew.mewcore.simplification;

import java.util.List;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;

public class FVAZeroValueFluxes extends ZeroValueFluxes{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private EnvironmentalConditions environmentalConditions;

	public EnvironmentalConditions getEnvironmentalConditions() {
		return environmentalConditions;
	}

	public void setEnvironmentalConditions(
			EnvironmentalConditions environmentalConditions) {
		this.environmentalConditions = environmentalConditions;
	}

	public FVAZeroValueFluxes(List<String> values, EnvironmentalConditions ec) {
		super(values);
		this.environmentalConditions = ec;
		// TODO Auto-generated constructor stub
	}

}
