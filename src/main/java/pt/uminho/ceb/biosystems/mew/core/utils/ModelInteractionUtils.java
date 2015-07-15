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
package pt.uminho.ceb.biosystems.mew.core.utils;

import java.util.ArrayList;

import pt.uminho.ceb.biosystems.mew.core.model.components.IStoichiometricMatrix;

public class ModelInteractionUtils {

 
	/**
	 * Simple heuristic to detect possible drains from a IStoichiometricMatrix
	 * 
	 * @param matrix
	 * @return
	 */
	public static ArrayList<Integer> identifyDrainsFromStoichiometricMatrix(IStoichiometricMatrix matrix)
	{
		ArrayList<Integer> detectedDrains = new ArrayList<Integer>();
		
		for(int i= 0; i < matrix.columns(); i++)
		{
			int numberCoefsNotZero = 0;
			for(int j=0; j < matrix.rows() && numberCoefsNotZero<2 ; j++)
				if (matrix.getValue(j,i)!=0) numberCoefsNotZero++;
			if (numberCoefsNotZero == 1) detectedDrains.add(i);
		}
		
		return detectedDrains;
	}
	
	
	public static int getMetaboliteFromDrainIndex(IStoichiometricMatrix matrix,int drainReactionIndex)
	{
		int numberCoefsNotZero = 0;
		int metaboliteIndex = -1;
		
		for(int j=0; j < matrix.rows() && numberCoefsNotZero<2 ; j++)
			if (matrix.getValue(j,drainReactionIndex)!=0) {
				numberCoefsNotZero++;
				metaboliteIndex = j;
			}
		if (numberCoefsNotZero != 1) metaboliteIndex = -1;
		
		return metaboliteIndex;
	}
}
