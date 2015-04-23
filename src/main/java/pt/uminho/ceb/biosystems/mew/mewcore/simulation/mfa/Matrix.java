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

/**
 * Matrix - Linear Algebra routines
 * @copyright (@copyleft GNU GPLv3) 1992, 2006 Rand Huso
 * (see http://www.gnu.org/copyleft/copyleft.html for details on copyleft)
 * @author Rand Huso
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.Serializable;
import java.util.Random;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;



/**
 * This class implements some matrix operations that I need for other things I'm doing. Why not use existing packages?
 * Well, I've tried, but they seem to not have simple linear algebra things like inverting singular or non-square
 * inverses. Maybe I've just been looking in the wrong places.
 * 
 * Anyway I wrote this algorithm in 1989 using Ada and Fortran77. In 1992 I converted it to C++, and in 2003 I made it a
 * C++ template to be used for absolutely anything that you'd want.
 * 
 * I attempted to convert this to a Java template, but I need to make objects of the parameter type, and without doing
 * some inspection or copy tricks, I can't get there from here. I only use this for doubles anyway, so here's the
 * version that I use. The C++ version is still available.
 * 
 * The matrix inversion was in from the start, as the only really useful part of the Class. I added the bandwidth
 * reduction routine in 1991 - I was stuck in SOS (USAF school) at the time and was thinking about optimizing the
 * bandwidth of a matrix made from a finite-element grid by renumbering the nodes of the grid.
 * 
 * Changes - 01 October 2007 by SLewis (as noted) (I'd never seen "Cloneable" :-)
 */
public class Matrix implements Serializable,Cloneable {
    private static final long   serialVersionUID = 2L;
    private static final double TINY_NEAR_ZERO   = 1.0E-12;
    /**
     * Matrix is an array of double (rows,cols). I've had no success making this a template.
     */
    private double[][]          matrix;
    private static final Random RND              = new Random();

    /**
     * default and specific constructors
     */
    public Matrix( int rows ) {
        this( rows, 1 );
    }
    
    public Matrix(double[][] matrix){
    	this.matrix = matrix;
    }

    public Matrix( int rows, int cols ) {
        setMatrix( rows, cols );
    }

    /**
     * copy constructor
     * 
     * @param clone
     */
    public Matrix( Matrix clone ) {
        this( clone.getRows(), clone.getCols() );
        int rows = clone.getRows();
        for( int row = 0; row < rows; ++row ) {
            System.arraycopy( clone.matrix[row], 0, matrix[row], 0, matrix[row].length );
        }
    }

    /**
     * make a matrix with random data between lowerBound and upperBound
     * 
     * @param rows
     *            number rows
     * @param cols
     *            number rows
     * @param lowerBound
     *            see above
     * @param upperBound
     *            see above
     * @return non-null matrix as above
     */
    public static Matrix randomMatrix( int rows, int cols, double lowerBound, double upperBound ) { // SLewis
        double del = upperBound - lowerBound;
        Matrix ret = new Matrix( rows, cols );
        for( int i = 0; i < rows; i++ ) {
            for( int j = 0; j < cols; j++ ) {
                double rand = lowerBound + RND.nextDouble() * del;
                ret.setValue( i, j, rand );
            }
        }
        return ret;
    }

    /**
     * make a square matrix with 1s on the diagonal
     * 
     * @param size
     *            number rows and cols
     * @return non-null matrix as above
     */
    public static Matrix identityMatrix( int size ) { // SLewis
        Matrix ret = new Matrix( size, size );
        for( int i = 0; i < size; i++ ) {
            ret.setValue( i, i, 1 );
        }
        return ret;
    }

    /**
     * make a square matrix with supplied values along the diagonal
     * 
     * @param values
     *            - values for the diagonal
     * @return non-null matrix as above
     */
    public static Matrix diagonalMatrix( double[] values ) { // SLewis
        int size = values.length;
        Matrix ret = new Matrix( size, size );
        for( int i = 0; i < size; i++ ) {
            ret.setValue( i, i, values[i] );
        }
        return ret;
    }

    /*
     * Create the matrix - allocate memory
     */
    private void setMatrix( int rows, int cols ) {
        matrix = new double[rows][];
        for( int row = 0; row < rows; ++row ) {
            matrix[row] = new double[cols];
        }
    }

    /*
     * getters and setters for rows and columns.
     */
    public int getRows() { // SLewis
        return matrix.length;
    }

    public int getCols() {
        return matrix[0].length; // SLewis
    }

    /**
     * get the value of this component. SLewis - throws instead of default response
     * 
     * @param row
     * @param col
     * @return
     */
    public double getValue( int row, int col ) throws ArrayIndexOutOfBoundsException {
        return matrix[row][col];
    }

    /**
     * set the value at this position. SLewis - throws instead of default response
     * 
     * @param row
     * @param col
     * @param value
     * @return success if the value was set.
     */
    public void setValue( int row, int col, double value ) throws ArrayIndexOutOfBoundsException {
        matrix[row][col] = value;
    }

    /*
     * Allocate memory for the response matrix for simple add, subtract, etc. operations. This requires that both
     * matrices are of the same dimensions. This could be modified to account for operations where a minimum set could
     * be returned instead.
     */
    private Matrix getSimpleResponseMatrix( Matrix matrix2 ) {
        Matrix response = null;
        if( sameSize( matrix2 ) ) {
            response = new Matrix( getRows(), getCols() );
        }
        return response;
    }

    /**
     * true if the two matrices are the same size
     * 
     * @param matrix2
     *            non-null matrix to compare
     * @return as above
     */
    public boolean sameSize( Matrix matrix2 ) { // SLewis
        return (getRows() == matrix2.getRows() && getCols() == matrix2.getCols());
    }

    /**
     * Add one matrix to another. The original is unchanged and a new matrix is generated and returned.
     * 
     * @param matrix2
     * @return the result of the operation or `null` if the operation is disallowed.
     */
    public Matrix add( Matrix matrix2 ) {
        Matrix response = getSimpleResponseMatrix( matrix2 );
        if( null != response ) {
            for( int row = 0; row < response.getRows(); ++row ) {
                for( int col = 0; col < response.getCols(); ++col ) {
                    response.setValue( row, col, getValue( row, col ) + matrix2.getValue( row, col ) );
                }
            }
        }
        return response;
    }

    /**
     * Subtract the parameter matrix from the current and return the result as a new matrix.
     * 
     * @param matrix2
     * @return the result of the subtraction or `null` if the operation is disallowed.
     */
    public Matrix subtract( Matrix matrix2 ) {
        Matrix response = getSimpleResponseMatrix( matrix2 );
        if( null != response ) {
            for( int row = 0; row < response.getRows(); ++row ) {
                for( int col = 0; col < response.getCols(); ++col ) {
                    response.setValue( row, col, getValue( row, col ) - matrix2.getValue( row, col ) );
                }
            }
        }
        return response;
    }

    /**
     * Create an identity matrix that matches the size of the supplied matrix.
     * 
     * @return the new identity matrix that matches the size of the current matrix, this may involve reducing the
     *         dimensions of the current one to make it a square matrix.
     */
    public Matrix identity() {
        int size = Math.min( getRows(), getCols() );
        return identity( size );
    }

    /**
     * Create an identity matrix of the supplied size.
     * 
     * @param size
     * @return a matrix of the supplied size.
     */
    public static Matrix identity( int size ) {
        size = size > 0 ? size : 1;
        Matrix response = new Matrix( size, size );
        for( int diag = 0; diag < size; ++diag ) {
            response.setValue( diag, diag, 1.0 );
        }
        return response;
    }

    /**
     * Transpose the matrix. The matrix does not have to be square.
     * 
     * @return a copy of the original matrix with rows and columns transposed.
     */
    public Matrix transpose() {
        Matrix response = new Matrix( getCols(), getRows() );
        for( int row = 0; row < getRows(); ++row ) {
            for( int col = 0; col < getCols(); ++col ) {
                response.setValue( col, row, getValue( row, col ) );
            }
        }
        return response;
    }

    /**
     * Create a string representation of the matrix for display purposes.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for( int row = 0; row < getRows(); ++row ) {
            for( int col = 0; col < getCols(); ++col ) {
                buffer.append( "\t" + getValue( row, col ) );
            }
            buffer.append( "\n" );
        }
        return buffer.toString();
    }

    /**
     * Check the two objects to determine if they contain equivalent values. Each row and column value is checked to see
     * if they are within the tolerances supplied. SLewis - renamed equivalent from equals and also removed hashCode
     * 
     * @param obj
     *            other matrix
     * @param tiny
     *            the amount of `play` before declaring them of different value
     * @return success or failure for the comparison
     */
    public boolean equivalent( Object obj, double tiny ) {
        if( this == obj )
            return true;
        if( (null == obj) || obj.getClass() != this.getClass() )
            return false;
        Matrix right = (Matrix) obj;
        if( this.getRows() != right.getRows() )
            return false;
        if( this.getCols() != right.getCols() )
            return false;
        for( int row = 0; row < this.getRows(); ++row ) {
            for( int col = 0; col < this.getCols(); ++col ) {
                if( Math.abs( this.getValue( row, col ) - right.getValue( row, col ) ) > tiny )
                    return false;
            }
        }
        return true;
    }

    /**
     * Perform the dot product of the two matrices or vectors. Each row,column item is multiplied into the row,column
     * item of the other matrix or vector and added to the sum. This operation is usually employed with vectors.
     * 
     * @param matrix2
     * @return the sum of the products
     */
    public double dot( Matrix matrix2 ) {
        double response = 0.0;
        // TODO throw exception if not the same size (later)
        int dotRows = Math.min( getRows(), matrix2.getRows() );
        int dotCols = Math.min( getCols(), matrix2.getCols() );
        for( int row = 0; row < dotRows; ++row ) {
            for( int col = 0; col < dotCols; ++col ) {
                response += this.getValue( row, col ) * matrix2.getValue( row, col );
            }
        }
        return response;
    }

    /**
     * Add this value to each component of the matrix
     * 
     * @param p
     * @return a new matrix with the value added to each component
     */
    public Matrix plusScalar( double p ) {
        Matrix response = new Matrix( this.getRows(), this.getCols() );
        for( int row = 0; row < this.getRows(); ++row ) {
            for( int col = 0; col < this.getCols(); ++col ) {
                response.setValue( row, col, p + getValue( row, col ) );
            }
        }
        return response;
    }

    /**
     * Multiply this value to each component of the matrix
     * 
     * @param d
     * @return a new matrix with the value multiplied to each component
     */
    public Matrix timesScalar( double d ) {
        Matrix response = new Matrix( this.getRows(), this.getCols() );
        for( int row = 0; row < this.getRows(); ++row ) {
            for( int col = 0; col < this.getCols(); ++col ) {
                response.setValue( row, col, d * getValue( row, col ) );
            }
        }
        return response;
    }

    /**
     * Matrix multiplication. Information may be lost if the left matrix column count does not match the right matrix
     * rows. The minimum of these two values is used.
     * 
     * @param matrix2
     * @return a new matrix constructed from the product of the two matrices
     */
    public Matrix times( Matrix matrix2 ) {
        int timesInner = Math.min( getCols(), matrix2.getRows() );
        return times( matrix2, timesInner );
    }

    /*
     * This routine performs the matrix multiplication. The final matrix size is taken from the rows of the left matrix
     * and the columns of the right matrix. The timesInner is the minimum of the left columns and the right rows.
     */
    private Matrix times( Matrix matrix2, int timesInner ) {
        int timesRows = getRows();
        int timesCols = matrix2.getCols();
        Matrix response = new Matrix( timesRows, timesCols );
        for( int row = 0; row < timesRows; ++row ) {
            for( int col = 0; col < timesCols; ++col ) {
                for( int inner = 0; inner < timesInner; ++inner ) {
                    response.setValue( row, col, this.getValue( row, inner ) * matrix2.getValue( inner, col )
                            + response.getValue( row, col ) );
                }
            }
        }
        return response;
    }

    /**
     * Make a copy of this matrix.
     * 
     * @return an exact copy of this matrix
     */
    public Matrix duplicate() {
        Matrix response = new Matrix( getRows(), getCols() );
        for( int row = 0; row < getRows(); ++row ) {
            for( int col = 0; col < getCols(); ++col ) {
                response.setValue( row, col, getValue( row, col ) );
            }
        }
        return response;
    }

    /**
     * Change the sign of all components of this matrix.
     * 
     * @return a new matrix with changed sign for all components.
     */
    public Matrix negate() {
        Matrix response = new Matrix( getRows(), getCols() );
        for( int row = 0; row < getRows(); ++row ) {
            for( int col = 0; col < getCols(); ++col ) {
                response.setValue( row, col, -getValue( row, col ) );
            }
        }
        return response;
    }

    /**
     * Mathematical operator to reduce the bandwidth of a matrix. The matrix must be a square matrix or no operations
     * are performed.
     * 
     * This method reduces a sparse matrix and returns the numbering of nodes to achieve this banding. It may be
     * advantageous to run this twice, though sample cases haven't shown the need. Rows are numbered beginning with 0.
     * The return matrix is a vector with what should be used as the new numbering to achieve minimum banding.
     * 
     * Each node in a typical finite-element grid is connected to surrounding nodes which are connected back to this
     * node. This routine was designed with this requirement in mind. It may work where a node is connected to an
     * adjacent node that is not connected back to this node... and this is quite possible when the grid is on a sphere
     * and the connections are determined based on initial headings or bearings.
     * 
     * @return the vector indicating the numbering required to achieve a minimum banding
     */
    public Matrix reduce() {
        Matrix response = new Matrix( getRows(), 1 );
        for( int row = 0; row < getRows(); ++row ) {
            response.setValue( row, 0, row );
        }
        return getRows() == getCols() ? reduce( response ) : response;
    }

    private Matrix reduce( Matrix response ) {
        if( getRows() == getCols() ) {
            // pass one (descending the diagonal):
            for( int col = 0; col < getCols() - 1; ++col ) {
                for( int rowData = getRows() - 1; rowData > col; --rowData ) {
                    if( 0 != this.getValue( rowData, col ) ) {
                        for( int rowEmpty = rowData - 1; rowEmpty > col; --rowEmpty ) {
                            if( 0 == this.getValue( rowEmpty, col ) ) {
                                if( canSwapRows( rowData, rowEmpty, col ) ) {
                                    swapRows( rowData, rowEmpty );
                                    swapCols( rowData, rowEmpty );
                                    response.swapRows( rowData, rowEmpty );
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            // second pass (ascending the diagonal):
            for( int row = getRows() - 1; row > 0; --row ) {
                for( int colData = 0; colData < row - 1; ++colData ) {
                    if( 0 != this.getValue( row, colData ) ) {
                        for( int colEmpty = colData + 1; colEmpty < row; ++colEmpty ) {
                            if( 0 == this.getValue( row, colEmpty ) ) {
                                if( canSwapCols( colData, colEmpty, row ) ) {
                                    swapRows( colData, colEmpty );
                                    swapCols( colData, colEmpty );
                                    response.swapRows( colData, colEmpty );
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return response;
    }

    /*
     * Check to see if a non-zero and a zero value in the rows leading up to this column can be swapped. This is part of
     * the bandwidth reduction algorithm.
     */
    private boolean canSwapRows( int row1, int row2, int col1 ) {
        boolean response = true;
        for( int col = 0; col < col1; ++col ) {
            if( 0 == this.getValue( row1, col ) ) {
                if( 0 != this.getValue( row2, col ) ) {
                    response = false;
                    break;
                }
            }
        }
        return response;
    }

    /*
     * Check to see if columns can be swapped - part of the bandwidth reduction algorithm.
     */
    private boolean canSwapCols( int col1, int col2, int row1 ) {
        boolean response = true;
        for( int row = row1 + 1; row < getRows(); ++row ) {
            if( 0 == this.getValue( row, col1 ) ) {
                if( 0 != this.getValue( row, col2 ) ) {
                    response = false;
                    break;
                }
            }
        }
        return response;
    }

    /*
     * Swap the values in the two rows.
     */
    private void swapRows( int row1, int row2 ) {
        double[] temp = this.matrix[row1];
        this.matrix[row1] = this.matrix[row2];
        this.matrix[row2] = temp;
    }

    /*
     * Swap components in the two columns (not as easy as swapping rows).
     */
    private void swapCols( int col1, int col2 ) {
        double temp;
        for( int row = 0; row < getRows(); ++row ) {
            temp = this.getValue( row, col1 );
            this.setValue( row, col1, this.getValue( row, col2 ) );
            this.setValue( row, col2, temp );
        }
    }

    /**
     * Matrix inversion is the reason this Class exists - this method creates a generalized matrix inverse of the
     * current matrix. The result is returned in a new matrix. A copy of the current matrix is created because the
     * operation is destructive. A second method below is supplied which is destructive, if the original isn't needed.
     * 
     * Matrices may be square, non-square, or singular. The operations will be identical. If the matrix has a single
     * possible inverse, there will be no arbitrariness to the solution. The method here was suggested to me by John
     * Jones Jr. at AFIT in the 1980s, and this algorithm is an original creation of mine to implement his method.
     * 
     * A matrix inverse has some properties described here. Let A be the original matrix. Let the inverse, as calculated
     * here be A12 (an inverse with properties 1 and 2 - being left side inverse and right side inverse). An inverse
     * times the original matrix should yield an identity matrix. A x = b is the usual equation where A is the matrix, x
     * is a vector of the unknowns and b is a vector of the constant values:
     * 
     * Given these equations: C x + D y + E z = b1 F x + G y + H z = b1
     * 
     * (The usual programs available don't handle more unknowns than equations, and will stop at this point)
     * 
     * The A matrix is: | C D E | | F G H |
     * 
     * The x vector is: | x | | y | | z |
     * 
     * The b vector is: | b1 | | b2 |
     * 
     * A * x = b
     * 
     * The generalized inverse A12 in this case will be of size (3,2): | J K | | L M | | N P |
     * 
     * The left-hand inverse is defined such that the product of the (generalized) inverse times the original matrix
     * times the (generalized) inverse will yield the (generalized) inverse again: A12 * (A * A12) = A12
     * 
     * The right-hand inverse is defined similarly: (A * A12) * A = A
     * 
     * If a matrix (A12) meets these criteria, it's considered to be a generalized inverse of the A matrix, even though
     * it may not be square, or the product of A * A12 or A12 * A may not be the identity matrix! (Won't be if the input
     * A matrix is non-square or singular)
     * 
     * In the case of (A * A12) being the identity matrix, the product of (A12 * A) will also be the identity matrix,
     * and the solution will be unique: A12 will be the exact and only solution to the equation.
     * 
     * It's probably a lot easier to refer to my http://aktzin.com/Inverse.jsp page for the best description.
     * 
     * @return a generalized matrix inverse (possibly not unique)
     */
    public Matrix inverse() {
        Matrix s = new Matrix( getCols(), getCols() ).identity();
        Matrix t = new Matrix( getRows(), getRows() ).identity();
        int maxDiag = Math.min( getRows(), getCols() );
        // this is because the operations are destructive to the input matrix:
        return this.duplicate().inverse( maxDiag, s, t );
    }
    
    public static DoubleMatrix2D inverse(DoubleMatrix2D matrix){
    	int numberOfRows = matrix.rows();
    	int numberOfColumns = matrix.columns();
        Matrix s = new Matrix(numberOfColumns,numberOfColumns).identity();
        Matrix t = new Matrix(numberOfRows,numberOfRows).identity();
        int maxDiag = Math.min(numberOfRows,numberOfColumns);
        double[][] matrixArray = matrix.toArray();
        Matrix duplicateMatrix = new Matrix(matrixArray);
        Matrix inverseMatrix = duplicateMatrix.inverse(maxDiag,s,t);
        
        double[][] inverseMatrixArray = inverseMatrix.getData();
        
        return new DenseDoubleMatrix2D(inverseMatrixArray);
    }

    private double[][] getData() {
		return matrix;
	}

	/**
     * Destructive version of the inverse() method.
     * 
     * @return a generalized matrix inverse (possibly not unique)
     */
    public Matrix inverseD2() {
        Matrix s = new Matrix( getCols(), getCols() ).identity();
        Matrix t = new Matrix( getRows(), getRows() ).identity();
        int maxDiag = Math.min( getRows(), getCols() );
        return this.inverse( maxDiag, s, t );
    }

    /*
     * column change matrices only taking into account the sections that contain useful data to produce the generalized
     * inverse.
     */
    private Matrix inverse( int maxDiag, Matrix s, Matrix t ) {
        int diag = 0;
        while( diag < maxDiag ) {
            // get the largest value for the pivot
            swapPivot( diag, s, t );
            if( 0 == getValue( diag, diag ) )
                break;
            // divide through to make pivot identity
            double divisor = getValue( diag, diag );
            if( Math.abs( divisor ) < TINY_NEAR_ZERO ) {
                setValue( diag, diag, 0.0 );
                break;
            }
            divideRowBy( diag, diag, divisor );
            t.divideRowBy( diag, 0, divisor );
            setValue( diag, diag, 1.0 );
            // remove values down remaining rows
            for( int row = diag + 1; row < getRows(); ++row ) {
                double factor = getValue( row, diag );
                if( 0 != factor ) {
                    addRowTimes( diag, diag, row, factor );
                    t.addRowTimes( diag, 0, row, factor );
                    setValue( row, diag, 0 );
                }
            }
            // remove values across remaining cols - some optimization could
            // be done here because the changes to the original matrix at this
            // point only touch the current diag column
            for( int col = diag + 1; col < getCols(); ++col ) {
                double factor = getValue( diag, col );
                if( 0 != factor ) {
                    addColTimes( diag, diag, col, factor );
                    s.addColTimes( diag, 0, col, factor );
                    setValue( diag, col, 0 );
                }
            }
            ++diag;
        }
        return s.times( t, diag );
    }

    /*
     * Add a factor times one column to another column
     */
    private void addColTimes( int diag, int fromRow, int col, double factor ) {
        for( int row = fromRow; row < getRows(); ++row ) {
            setValue( row, col, getValue( row, col ) - factor * getValue( row, diag ) );
        }
    }

    /*
     * Add a factor times one row to another row
     */
    private void addRowTimes( int diag, int fromCol, int row, double factor ) {
        for( int col = fromCol; col < getCols(); ++col ) {
            setValue( row, col, getValue( row, col ) - factor * getValue( diag, col ) );
        }
    }

    /*
     * Divide the row from this column position by this value
     */
    private void divideRowBy( int aRow, int fromCol, double value ) {
        for( int col = fromCol; col < getCols(); ++col ) {
            setValue( aRow, col, getValue( aRow, col ) / value );
        }
    }

    /*
     * Swap the matrices so that the largest value is on the pivot
     */
    private void swapPivot( int diag, Matrix s, Matrix t ) {
        // get swap row and col
        int swapRow = diag;
        int swapCol = diag;
        double maxValue = Math.abs( getValue( diag, diag ) );
        for( int row = diag; row < getRows(); ++row ) {
            for( int col = diag; col < getCols(); ++col ) {
                if( Math.abs( getValue( row, col ) ) > maxValue ) {
                    maxValue = Math.abs( getValue( row, col ) );
                    swapRow = row;
                    swapCol = col;
                }
            }
        }
        // swap rows and columns
        if( swapRow != diag ) {
            swapRows( swapRow, diag );
            t.swapRows( swapRow, diag );
        }
        if( swapCol != diag ) {
            swapCols( swapCol, diag );
            s.swapCols( swapCol, diag );
        }
    }

    /**
     * Calculate the arbitrariness of the solution. This is a way to find out how unique the existing inverse is. The
     * equation is here: A * x = b And the solution is: x = A12 * b + an arbitrariness which could be infinite, but will
     * follow a general pattern. For instance, if the solution is a line, it could be anchored in the Y at any arbitrary
     * distance. If the solution is a plane it could be arbitrarily set to any place in perhaps two different
     * dimensions.
     * 
     * The arbitrariness is calculated by taking the difference between the complete inverse times the original and
     * subtracting the generalized inverse times the original matrix. That's the idea, here's the formula:
     * 
     * x = A12 * b + (I - (A12 * A)) * z The z is a completely arbitrary vector (you decide that one). The product (A12
     * * A) could be the Identity matrix, if the solution is unique, in which case the right side drops out: (I - I) * z
     * 
     * Again, it's a lot easier to refer to the http://aktzin.com/ site for the description and a different way to get
     * this information.
     * 
     * @return the matrix (I - (A12 * A))
     */
    public Matrix arbitrariness( Matrix inverse ) {
        Matrix intermediate = inverse.times( this );
        return intermediate.identity().subtract( intermediate );
    }
}
