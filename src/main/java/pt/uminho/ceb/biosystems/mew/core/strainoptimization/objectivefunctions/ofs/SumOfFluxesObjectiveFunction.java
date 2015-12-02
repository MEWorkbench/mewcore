/*
 * Copyright 2010
 * IBB-CEB - Institute for Biotechnology and Bioengineering - Centre of
 * Biological Engineering
 * CCTC - Computer Science and Technology Center
 * University of Minho
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Public License for more details.
 * You should have received a copy of the GNU Public License
 * along with this code. If not, see http://www.gnu.org/licenses/
 * Created inside the SysBioPseg Research Group (http://sysbio.di.uminho.pt)
 */
package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.AbstractObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ObjectiveFunctionParameterType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public class SumOfFluxesObjectiveFunction extends AbstractObjectiveFunction {
	
	private static final long serialVersionUID = 1L;
	
	public static final String ID = "SUM_F";
	
	public static final String	SUM_F_PARAM_MAXIMIZATION	= "Maximization";
	public static final String	SUM_F_PARAM_FLUXES_TO_SUM	= "FluxesToSum";
	
	public Map<String, ObjectiveFunctionParameterType> loadParameters(){
		Map<String, ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(SUM_F_PARAM_MAXIMIZATION, ObjectiveFunctionParameterType.BOOLEAN);
		myparams.put(SUM_F_PARAM_FLUXES_TO_SUM, ObjectiveFunctionParameterType.SET.of(ObjectiveFunctionParameterType.REACTION));
		return Collections.unmodifiableMap(myparams);
	}
	
	public SumOfFluxesObjectiveFunction(Boolean maximize, Set<String> fluxesToSum) {
		super(maximize, fluxesToSum);
	}
	
	public SumOfFluxesObjectiveFunction(Boolean maximize, String... fluxesToSum) {
		super(maximize, fluxesToSum);
	}
	
	public SumOfFluxesObjectiveFunction(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration {
		super(configuration);
	}
	
	public SumOfFluxesObjectiveFunction(Boolean maximize) {
		super(maximize);
	}
	
	@Override
	protected void processParams(Object... params) {
		setParameterValue(SUM_F_PARAM_MAXIMIZATION, params[0]);
		if (params.length == 1) {
			setParameterValue(SUM_F_PARAM_FLUXES_TO_SUM, null);
		} else if (params.length == 2) {
			setParameterValue(SUM_F_PARAM_FLUXES_TO_SUM, params[1]);
		} else {
			Set<String> fluxes = new HashSet<String>();
			String[] range = (String[]) Arrays.copyOfRange(params, 1, params.length);
			fluxes.addAll(Arrays.asList(range));
			setParameterValue(SUM_F_PARAM_FLUXES_TO_SUM, fluxes);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		
		Set<String> fluxesToSum = (Set<String>) getParameterValue(SUM_F_PARAM_FLUXES_TO_SUM);
		Boolean maximize = (Boolean) getParameterValue(SUM_F_PARAM_MAXIMIZATION);
		
		double sumFluxes = 0.0;
		
		if (fluxesToSum == null) fluxesToSum = new HashSet<>(simResult.getFluxValues().keySet());
		
		for (String v : fluxesToSum)
			sumFluxes += Math.abs(simResult.getFluxValues().getValue(v));
			
		if (maximize)
			return sumFluxes;
		else {
			if (sumFluxes <= 1)
				return 0;
			else
				return Math.log10(1 / sumFluxes);
		}
	}
	
	@Override
	public double getWorstFitness() {
		return -Double.MAX_VALUE; //NOTE: the worst fitness is always negative double max value.
	}
	
	
	public String toString() {
		String objSense = (Boolean) (getParameterValue(SUM_F_PARAM_MAXIMIZATION)) ? "Maximize " : "Minimize ";
		return objSense + "Metabolic Activity";
	}
	
	@Override
	public boolean isMaximization() {
		return (Boolean) getParameterValue(SUM_F_PARAM_MAXIMIZATION);
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction#
	 * getUnnormalizedFitness(double)
	 */
	@Override
	public double getUnnormalizedFitness(double fit) {
//		double ret = (Boolean) (getParameterValue(SUM_F_PARAM_MAXIMIZATION)) ? (fit) : (1 / (Math.pow(10, fit))); //TODO: what is this doing here??? what is the correct behaviour?
		return 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction#
	 * getShortString()
	 */
	@Override
	public String getShortString() {
		return String.valueOf('\u03A3') + "|flux values|";
	}
	
	@Override
	public String getLatexString() {
		return "$" + getLatexFormula() + "$";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String getLatexFormula() {
		
		Set<String> fluxesToSum = (Set<String>) getParameterValue(SUM_F_PARAM_FLUXES_TO_SUM);
		String n;
		if (fluxesToSum != null)
			n = "N=" + fluxesToSum.size();
		else
			n = "N";
		String sense = (Boolean) getParameterValue(SUM_F_PARAM_MAXIMIZATION) ? "max" : "min";
		
		return sense + "\\sum_{i}^{" + n + "} v_i";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String getBuilderString() {
		Set<String> fluxesToSum = (Set<String>) getParameterValue(SUM_F_PARAM_FLUXES_TO_SUM);
		return getID() + "(" + getParameterValue(SUM_F_PARAM_MAXIMIZATION)+ ",[" + CollectionUtils.join(fluxesToSum, ",") + "])";
	}
	
	@Override
	public String getID() {
		return ID;
	}
}
