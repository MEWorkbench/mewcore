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
package pt.uminho.ceb.biosystems.mew.core.simulation.components;

import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;

public interface ISteadyStateSimulationMethod {
	
	void preSimulateActions();
	
	void postSimulateActions();
	
	SteadyStateSimulationResult simulate() throws Exception;
	
	ISteadyStateModel getModel();
	
	EnvironmentalConditions getEnvironmentalConditions() throws PropertyCastException, MandatoryPropertyException;
	
	void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions);
	
	GeneticConditions getGeneticConditions() throws PropertyCastException, MandatoryPropertyException;
	
	void setGeneticConditions(GeneticConditions geneticConditions);
	
	Set<String> getPossibleProperties();
	
	Set<String> getMandatoryProperties();
	
	void setProperty(String m, Object o);
	
	void putAllProperties(Map<String, Object> properties);
	
	void clearAllProperties();
	
	<T> T getProperty(String k);
	
	Class<?> getFormulationClass();
	
	void setRecreateOF(boolean recreateOF);
	
	boolean isRecreateOF();
	
	void addPropertyChangeListener(PropertyChangeListener listener);
	
	void saveModelToMPS(String file, boolean includeTime);
}
