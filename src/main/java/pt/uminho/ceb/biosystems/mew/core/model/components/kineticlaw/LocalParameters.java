/*
 * Copyright 2010
 * IBB-CEB - Institute for Biotechnology and Bioengineering - Centre of Biological Engineering
 * CCTC - Computer Science and Technology Center
 *
 * University of Minho 
 * 
 * This is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This code is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Public License for more details. 
 * 
 * You should have received a copy of the GNU Public License 
 * along with this code. If not, see http://www.gnu.org/licenses/ 
 * 
 * Created inside the SysBioPseg Research Group (http://sysbio.di.uminho.pt)
 */
package pt.uminho.ceb.biosystems.mew.core.model.components.kineticlaw;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class LocalParameters implements Serializable{
    
	private static final long serialVersionUID = 1L;
	
	protected List<Parameter> localEnvironment;
	
    public LocalParameters() {
        localEnvironment = new ArrayList<Parameter>();
    }

    public LocalParameters(List<Parameter> parameters) {
        localEnvironment = parameters;
    }

    public void addParameter(String parameterName, double value) {
        Parameter parameter = new Parameter(parameterName, value);
        localEnvironment.add(parameter);
    }

    public Double getParameterValue(String parameterName) {
        Iterator<Parameter> parameterIterator = localEnvironment.iterator();

        while (parameterIterator.hasNext()) {
            Parameter parameter = parameterIterator.next();
            if (parameter.getId().equals(parameterName))
                return new Double(parameter.getValue());
        }

        return null;
    }

    public int getNumberOfParameters() {
        return localEnvironment.size();
    }

    public Iterator<String> getLocalParameterIterator() {
        return null;
    }

	public List<Parameter> getLocalParameterList() {
		return localEnvironment;
	}

}