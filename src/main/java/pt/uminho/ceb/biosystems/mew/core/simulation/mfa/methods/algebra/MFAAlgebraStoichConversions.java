package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.algebra;

import java.util.List;

import cern.colt.matrix.DoubleMatrix2D;
import pt.uminho.ceb.biosystems.mew.core.model.components.ColtSparseStoichiometricMatrix;
import pt.uminho.ceb.biosystems.mew.core.model.components.IStoichiometricMatrix;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ratioconstraints.FluxRatioConstraint;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ratioconstraints.FluxRatioConstraintList;

public class MFAAlgebraStoichConversions {
	
	/** For each equality flux ratio, a column is added to the stoichiometric matrix 
	 * @return a new stoichiometric matrix is returned with the flux ratios linear equations */
	public static IStoichiometricMatrix addRatiosToStoichiometricMatrix(ISteadyStateModel model, FluxRatioConstraintList ratios) {
	
		List<FluxRatioConstraint> equalityRatios = null;

		if(ratios!=null)
			equalityRatios = ratios.getEqualityRatios();
		if(equalityRatios==null || equalityRatios.size()==0)
			return model.getStoichiometricMatrix().copy();
		
		IStoichiometricMatrix mMatrix = model.getStoichiometricMatrix().copy();
		DoubleMatrix2D modelMatrix = mMatrix.convertToColt();	
		IStoichiometricMatrix newMatrix = new ColtSparseStoichiometricMatrix(modelMatrix.rows() + equalityRatios.size(), modelMatrix.columns());
System.out.println("<RCC> S rows before ratios : " + modelMatrix.rows());		
		for(int i=0; i<modelMatrix.rows(); i++)
			for(int j=0; j<modelMatrix.columns(); j++)
				newMatrix.setValue(i, j, modelMatrix.get(i, j));
		
		int ratioIndex = model.getNumberOfMetabolites();
		
//		try {
			for(FluxRatioConstraint ratio : equalityRatios)
			{
				for(String reactionId : ratio.getFluxesCoeffs().keySet())
				{
					double coeff = ratio.getFluxesCoeffs().get(reactionId);
					String fluxId = ratios.getFluxIdFromNegativeForm(reactionId);
					
					if(ratios.isFluxNegative(fluxId))
						coeff *= -1;
					int reactionIndex = model.getReactionIndex(fluxId);
					
					newMatrix.setValue(ratioIndex, reactionIndex, coeff);
				}	
				ratioIndex++;
			}
//		} catch (NonExistentIdException e) {
//			e.printStackTrace();
//			return mMatrix;
//		}	
System.out.println("<RCC> S rows after ratios : " + newMatrix.rows());	
		return newMatrix;
	}

}
