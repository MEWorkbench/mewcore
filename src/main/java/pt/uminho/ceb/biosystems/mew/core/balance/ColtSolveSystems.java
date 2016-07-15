package pt.uminho.ceb.biosystems.mew.core.balance;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

public class ColtSolveSystems implements SolveSystems<DoubleMatrix2D> {

	@Override
	public DoubleMatrix2D solve(DoubleMatrix2D a, DoubleMatrix2D b) {
		Algebra al = new Algebra();
		DoubleMatrix2D result = al.solve(a, b);
		
		return result;
	}

}
