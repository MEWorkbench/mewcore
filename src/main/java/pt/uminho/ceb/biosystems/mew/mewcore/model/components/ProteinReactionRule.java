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
package pt.uminho.ceb.biosystems.mew.mewcore.model.components;

import java.io.Serializable;

import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.AbstractSyntaxTree;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.AbstractSyntaxTreeNode;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.DataTypeEnum;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.IValue;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.parser.ParseException;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.parser.ParserSingleton;

public class ProteinReactionRule implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	protected AbstractSyntaxTree<DataTypeEnum, IValue> rule;
	
	public ProteinReactionRule(String rule) throws ParseException{
		
		if(rule == null || rule.equals("")){
			this.rule = new AbstractSyntaxTree<DataTypeEnum, IValue>();
		}else{
		
			AbstractSyntaxTreeNode<DataTypeEnum, IValue> ast;
			
			//System.out.println(rule);
			ast = ParserSingleton.boolleanParserString(rule);
			
			this.rule = new AbstractSyntaxTree<DataTypeEnum,IValue>(ast);
		}
	
		
	}
	
	
	public AbstractSyntaxTree<DataTypeEnum, IValue> getRule() {
		return rule;
	}

	public void setRule(String rule) throws ParseException {
		
		if(rule == null || rule.equals("")){
			this.rule = new AbstractSyntaxTree<DataTypeEnum, IValue>();
		}else{
		
			AbstractSyntaxTreeNode<DataTypeEnum, IValue> ast;
			
			ast = ParserSingleton.boolleanParserString(rule);
			
			this.rule = new AbstractSyntaxTree<DataTypeEnum, IValue>(ast);
		}
	}
	
	public void setRule(AbstractSyntaxTree<DataTypeEnum, IValue> rule){
		this.rule = rule;
	}
	
	

}
