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
package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa;

import java.io.Serializable;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.nullspace.MFANullSpaceSolution;

public class MFASolution implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
    protected MFAResultType resultType;
    protected LPSolution underDeterminedMFASolution;
    protected List<ReactionConstraint> underDeterminedTightBoundsSolution;
    protected FluxValueMap determinedSystemSolution;
    protected FluxValueMap overDeterminedSystemSolution;
    protected LPSolution qpSystemSolution;
    protected FluxValueMap measureList;
    protected MFANullSpaceSolution marcellSolution;

//	private String solverOutput; - in LP Solution

    public MFASolution(MFANullSpaceSolution solution){
    	marcellSolution = solution;
    	resultType = MFAResultType.MARCELL_METHOD;
    }
    
	public MFASolution(FluxValueMap fluxMeasureList) {
        this.measureList = fluxMeasureList;
    }
    
    public List<ReactionConstraint> getUnderDeterminedTightBoundsSolution() {
		return underDeterminedTightBoundsSolution;
	}

	public void setUnderDeterminedTightBoundsSolution(List<ReactionConstraint> underDeterminedTightBoundsSolution) {
		this.underDeterminedTightBoundsSolution = underDeterminedTightBoundsSolution;
	}

	public FluxValueMap getMeasureList() {
        return measureList;
    }

    public void setResultType(MFAResultType resultType) {
        this.resultType = resultType;
    }

    public void setDeterminedSystemSolution(FluxValueMap result) {
        determinedSystemSolution = result;    
    }

    public void setUnderDeterminedMFASolution(LPSolution underDeterminedResult) {
        this.underDeterminedMFASolution = underDeterminedResult;
    }

    public void setOverDeterminedSolution(FluxValueMap overDeterminedResult) {
        overDeterminedSystemSolution = overDeterminedResult;
    }

    public MFAResultType getType() {
        return resultType;
    }

    public FluxValueMap getDeterminedSystemSolution() {
        return determinedSystemSolution;
    }


    public LPSolution getUnderDeterminedSystemSolution() {
        return underDeterminedMFASolution;
    }

    public FluxValueMap getOverDeterminedSystemSolution() {
        return overDeterminedSystemSolution;
    }
    
    public MFANullSpaceSolution getNullSpaceSolution() {
		return marcellSolution;
	}


	public LPSolution getQpSystemSolution() {
		return qpSystemSolution;
	}

	public void setQpSystemSolution(LPSolution qpSystemSolution) {
		this.qpSystemSolution = qpSystemSolution;
	}

}
