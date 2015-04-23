package pt.uminho.ceb.biosystems.mew.mewcore.simulation.components;

import java.io.Serializable;

import pt.uminho.ceb.biosystems.mew.solvers.lp.LPMapVariableValues;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;

import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;

public class MetaboliteValueMap extends MapStringNum implements Serializable {
	
	private static final long serialVersionUID = 1L;

	
	public MetaboliteValueMap()
	{
		super();
	}
	
	public MetaboliteValueMap (LPMapVariableValues values, ISteadyStateModel model)
	{
		super();
		for(int index : values.getIndexes())
			this.put(model.getMetaboliteId(index), values.get(index));
	}
	
}
