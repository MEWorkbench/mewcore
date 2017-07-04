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
import java.util.Iterator;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.AbstractSyntaxTree;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.Environment;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.IEnvironment;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.DataTypeEnum;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.DoubleValue;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.IValue;


public class KineticLaw implements Serializable{
	
	private static final long serialVersionUID = 1L;

	protected AbstractSyntaxTree<DataTypeEnum,IValue> reactionKineticLaw;

    protected LocalParameters localEnvironment;

    public KineticLaw(){
    	reactionKineticLaw = new AbstractSyntaxTree<DataTypeEnum,IValue>();
    	localEnvironment = new LocalParameters();
    }
    
    public KineticLaw(AbstractSyntaxTree<DataTypeEnum,IValue> reactionKineticLaw) {
        this.reactionKineticLaw = reactionKineticLaw;
        this.localEnvironment = new LocalParameters();
    }

    public KineticLaw(AbstractSyntaxTree<DataTypeEnum,IValue> kineticLawMath, List<Parameter> localEnvironment) {
        this.reactionKineticLaw = kineticLawMath;
        this.localEnvironment = new LocalParameters(localEnvironment);
    }

    public AbstractSyntaxTree<DataTypeEnum,IValue> getReactionKineticLaw() {
        return reactionKineticLaw;
    }

    public void setReactionKineticLaw(AbstractSyntaxTree<DataTypeEnum,IValue> reactionKineticLaw) {
        this.reactionKineticLaw = reactionKineticLaw;
    }
    
    public void addParameter(String parameterName, double value) {
        localEnvironment.addParameter(parameterName, value);
    }

    public Double evaluate(IEnvironment<IValue> environment){
   
    	if(reactionKineticLaw != null){
    		IEnvironment<IValue> kineticLawEnvironment = new Environment<IValue>((Environment<IValue>) environment);
        	setEnvironmentLocalParameters(kineticLawEnvironment);
    		return (Double) reactionKineticLaw.evaluate(kineticLawEnvironment).getValue();
    	}
    	return null;
    }

    protected void setEnvironmentLocalParameters(IEnvironment<IValue> kineticLawEnvironment) {
       List<Parameter> localParameterList = getLocalParameterList();
  	   
       for(Parameter parameter:localParameterList){
  		   String parameterId = parameter.getId();
  		   double parameterValue = parameter.getValue();
  		   kineticLawEnvironment.associate(parameterId,new DoubleValue(parameterValue));
  	   }
		
	}

	public Double getParameterValue(String parameterName) {
        return localEnvironment.getParameterValue(parameterName);
    }

    public Iterator<String> getLocalParameterIterator() {
        return localEnvironment.getLocalParameterIterator();
    }

	public List<Parameter> getLocalParameterList() {
		return localEnvironment.getLocalParameterList();
	}

}
