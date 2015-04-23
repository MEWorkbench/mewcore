package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ratioconstraints;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FluxRatioConstraintList extends LinkedList<FluxRatioConstraint> {
	
	private static final long serialVersionUID = 3118994767874262619L;
	
	protected List<String> positiveFluxes;
	protected List<String> negativeFluxes;
	
	
	public FluxRatioConstraintList(){
		super();
		positiveFluxes = new ArrayList<String>();
		negativeFluxes = new ArrayList<String>();
	}
	
	/** Used to determine the model degrees of freedom */
	public int numberOfEqualityRatios(){
		int n = 0;
		for(FluxRatioConstraint ratio : this)
			if(ratio.isEqualityRatio())
				n ++;
		return n;
	}
	
	public boolean isFluxPositive(String flux){
		return positiveFluxes.contains(flux);
	}
	
	public boolean isFluxNegative(String flux){
		return negativeFluxes.contains(flux);
	}
	
	public String getFluxIdFromNegativeForm(String negativeForm){
		return negativeForm.replaceAll(FluxRatioTokens.FLUX_REVERSE_SUFIX + "$", "");
	}
	
	public List<FluxRatioConstraint> getEqualityRatios(){
		List<FluxRatioConstraint> rs = new ArrayList<FluxRatioConstraint>();
		for(FluxRatioConstraint ratio : this)
			if(ratio.isEqualityRatio())
				rs.add(ratio);
		return rs;
	}
	
	public List<String> getPositiveFluxes() {return positiveFluxes;}
	public List<String> getNegativeFluxes() {return negativeFluxes;}

	@Override
	public boolean add(FluxRatioConstraint e) {
		boolean added = super.add(e);
		
		for(String flux : e.getFluxesCoeffs().keySet())
		{
			if(flux.endsWith(FluxRatioTokens.FLUX_REVERSE_SUFIX)) // Negative flux
			{
				String fluxId = flux.replaceAll(FluxRatioTokens.FLUX_REVERSE_SUFIX + "$", "");
				negativeFluxes.add(fluxId);
			}
			else // Positive flux
				positiveFluxes.add(flux);
		}
		return added;
	}
	
	@Override
	public FluxRatioConstraint remove(int index) {	
		FluxRatioConstraint removed = super.remove(index);
		
		for(String flux : removed.getFluxesCoeffs().keySet())
		{
			if(flux.endsWith(FluxRatioTokens.FLUX_REVERSE_SUFIX)) // Negative flux
				negativeFluxes.remove(getFluxIdFromNegativeForm(flux));
			else // Positive flux
				positiveFluxes.remove(flux);
		}
		return removed;
	}
	
	@Override
	public boolean remove(Object o) {
		for(String flux : ((FluxRatioConstraint) o).getFluxesCoeffs().keySet())
		{
			if(flux.endsWith(FluxRatioTokens.FLUX_REVERSE_SUFIX)) // Negative flux
				negativeFluxes.remove(getFluxIdFromNegativeForm(flux));
			else // Positive flux
				positiveFluxes.remove(flux);
		}
		return super.remove(o);
	}
}
