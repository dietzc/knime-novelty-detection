package knfst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;
import org.jblas.Eigen;
import org.jblas.MatrixFunctions;
import org.jblas.Singular;
import org.jblas.ranges.IntervalRange;
import org.jblas.ranges.Range;

public class KNFST {
	private String[] labels;
	private DoubleMatrix kernelMatrix;
	private DoubleMatrix projection;
	private DoubleMatrix targetPoints;
	
	// wrapper for multiple classes (for convenience and understandability)
	public KNFST learn_multiClassNovelty_knfst(DoubleMatrix kernelMatrix, String[] labels) {
		return new KNFST(kernelMatrix, labels);
	}
	
	// constructor for multiple classes
	private KNFST(DoubleMatrix kernelMatrix, String[] labels) {
		this.labels = labels;
		this.kernelMatrix = kernelMatrix;
		
		// obtain unique class labels
		ArrayList<ClassWrapper> classes = ClassWrapper.classes(labels);
		
		// calculate projection of KNFST
		this.projection = projection(kernelMatrix, labels);
		
		// calculate target points ( = projections of training data into the null space)
		DoubleMatrix targetPoints = DoubleMatrix.zeros(classes.size(), this.projection.getColumns());
		int n = 0;
		int nOld = 0;
		for (int c = 0; c < classes.size(); c++) {
			n += classes.get(c).getCount();
			final IntervalRange interval = new IntervalRange(nOld, n-1);
			targetPoints.putRow(c, kernelMatrix.getRows(interval).mmul(projection).columnMeans());
			nOld = n;
		}
	}
	
	// wrapper for single class (for convenience and understandability)
	public KNFST learn_oneClassNovelty_knfst(DoubleMatrix kernelMatrix) {
		return new KNFST(kernelMatrix);
	}
	
	// constructor for single class
	private KNFST(final DoubleMatrix kernelMatrix) {
		this.kernelMatrix = kernelMatrix;
		// get number of training samples
		int n = kernelMatrix.getRows();
		
		// include dot products of training samples and the origin in feature space (these dot products are always zero!)
		final DoubleMatrix k = DoubleMatrix.concatVertically(DoubleMatrix.concatHorizontally(kernelMatrix, DoubleMatrix.zeros(n)), DoubleMatrix.zeros(n+1));
		
		// create one-class labels + a different label for the origin
		final String[] labels = new String[n+1];
		for (int l = 0; l < n; l++)
			labels[l] = (l == n-1)?"0":"1";
		
		// get model parameters
		final DoubleMatrix projection = projection(k, labels);
		this.targetPoints = kernelMatrix.mmul(projection).columnMeans();
		this.projection = projection.getRows(new IntervalRange(0, n-1));
		
	}
	
	public static DoubleMatrix projection(final DoubleMatrix kernelMatrix, final String[] labels) {
		
		ArrayList<ClassWrapper> classes = ClassWrapper.classes(labels);
		
		// check labels
		if (classes.size() == 1) {
			System.out.println("not able to calculate a nullspace from data of a single class using KNFST (input variable \"labels\" only contains a single value)");
			return null;
		}
		
		// check kernel matrix
		if (!kernelMatrix.isSquare()) {
			System.out.println("kernel matrix must be quadratic");
			return null;
		}
		
		// calculate weights of orthonormal basis in kernel space
		final DoubleMatrix centeredK = centerKernelMatrix(kernelMatrix);
		final DoubleMatrix[] eig = Eigen.symmetricEigenvectors(centeredK);
		
		//System.out.println("eig:");
		//test.printMatrix(eig[0]);
		//test.printMatrix(eig[1]);
		final double[] basisValues = eig[1].diag().toArray();
		
		
		// get number and position of nonzero basis values
		final ArrayList<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i < basisValues.length; i++) {
			if (basisValues[i] > 1e-12) {
				indices.add(i);
			}
		}
		
		// convert ArrayList<Integer> indices into int[] intIndices
		// create Array with nonzero resized basis values
		final int[] intIndices = new int[indices.size()];
		final double[] nonzeroBasisValues = new double[indices.size()];
		for (int i = 0; i < indices.size(); i++) {
			nonzeroBasisValues[i] = 1 / Math.sqrt(basisValues[indices.get(i)]);
			intIndices[i] = indices.get(i);
		}
		
		// get basis vectors with nonzero basis values
		DoubleMatrix basisvecs = eig[0].getColumns(intIndices);
		// create diagonal matrix with nonzero basis values
		final DoubleMatrix basisvecsValues = DoubleMatrix.diag(new DoubleMatrix(nonzeroBasisValues));
		
		//test.printMatrix(basisvecs);
		//test.printMatrix(basisvecsValues);
		
		basisvecs = basisvecs.mmul(basisvecsValues);
		
		// calculate transformation T of within class scatter Sw:
		// T= B'*K*(I-L) and L a block matrix
		DoubleMatrix L = DoubleMatrix.zeros(kernelMatrix.rows, kernelMatrix.columns);
		int l = 0;
		int count = 0;
		for (int k = 0; k < classes.size(); k++) {
			for (; l < labels.length && labels[l].equals(classes.get(k).getName()); l++) {
				count++;
			}
			IntervalRange rrange = new IntervalRange(l-count, l);
			IntervalRange crange = new IntervalRange(l-count, l);
			L = L.put(rrange, crange, DoubleMatrix.ones(count, count).mul(1.0 / (double) classes.get(k).getCount()));
			count = 0;
		}
		
		
		// need Matrix M with all entries 1/m to modify basisvecs which allows usage of 
		// uncentered kernel values (eye(size(M)).M)*basisvecs
		DoubleMatrix M = DoubleMatrix.ones(kernelMatrix.columns, kernelMatrix.columns).mul(1.0/kernelMatrix.columns);
		
		// compute helper matrix H
		DoubleMatrix H = DoubleMatrix.eye(M.columns).sub(M).mmul(basisvecs).transpose();
		DoubleMatrix K = kernelMatrix.mmul(DoubleMatrix.eye(kernelMatrix.columns).sub(L));
		H = H.mmul(K);
		
		// T = H*H' = B'*Sw*B with B=basisvecs
		DoubleMatrix T = H.mmul(H.transpose());
		
		//calculate weights for null space
		DoubleMatrix eigenvecs = nullspace(T);
		
		if (eigenvecs.getColumns() < 1) {
			ComplexDoubleMatrix[] eigenComp = Eigen.eigenvectors(T);
			DoubleMatrix eigenvals = eigenComp[1].getReal().diag();
			eigenvecs = eigenComp[0].getReal();
			int minId = MatrixFunctions.abs(eigenvals).argmin();
			eigenvecs = eigenvecs.getColumn(minId);
		}
	
		//System.out.println("eigenvecs:");
		//test.printMatrix(eigenvecs);
		
		// calculate null space projection
		//DoubleMatrix proj = DoubleMatrix.eye(M.getColumns()).sub(M).mmul(basisvecs);
		DoubleMatrix h1 = DoubleMatrix.eye(M.getColumns()).sub(M);
		//System.out.println("h1:");
		//test.printMatrix(h1);
		//System.out.println("basisvecs:");
		//test.printMatrix(basisvecs);
		DoubleMatrix proj = h1.mmul(basisvecs);
		//System.out.println("proj:");
		//test.printMatrix(proj);
		proj = proj.mmul(eigenvecs);
		
		return proj;
	}
	
	private static DoubleMatrix centerKernelMatrix(DoubleMatrix kernelMatrix) {
		// get size of kernelMatrix
		int n = kernelMatrix.rows;
		
		// get mean values for each row/column
		DoubleMatrix columnMeans = kernelMatrix.columnMeans();
		double matrixMean = kernelMatrix.mean();
		
		DoubleMatrix centeredKernelMatrix = kernelMatrix;
		
		for (int k = 0; k < n; k++) {
			centeredKernelMatrix.putRow(k, centeredKernelMatrix.getRow(k).sub(columnMeans));
			centeredKernelMatrix.putColumn(k, centeredKernelMatrix.getColumn(k).sub(columnMeans.transpose()));
		}
		
		centeredKernelMatrix = centeredKernelMatrix.add(matrixMean);
		
		return centeredKernelMatrix;
	}
	
	public static DoubleMatrix nullspace(DoubleMatrix matrix) {
		DoubleMatrix[] svd = Singular.fullSVD(matrix);
		Test.printMatrix(matrix);
		Test.printMatrix(svd[1]);
		Test.printMatrix(svd[2]);
		int rank = 0;
		double[] singularvalues = svd[1].toArray();
		for (; rank < singularvalues.length && singularvalues[rank] > 1e-12; rank++);
		int[] cindices = new int[svd[2].columns-rank];
		for (int i = 0; i < svd[2].columns - rank; i++)
			cindices[i] = rank + i;
		DoubleMatrix basis = svd[2].getColumns(cindices);
		return basis;
	}
	
	public static void main(String[] args) {
		String[] labels = {"A", "A", "A", "B", "B", "C"};
		ArrayList<ClassWrapper> classes = ClassWrapper.classes(labels);
		double[] elements = {1,1,1,2,2,2,3,3,3};
		DoubleMatrix A = new DoubleMatrix(3,3, elements);
		DoubleMatrix L = DoubleMatrix.zeros(6, 6);
		DoubleMatrix Z = nullspace(A);
		
		Test.printMatrix(Z);
		Test.printMatrix(A.mmul(Z));
		Test.printMatrix(Z.transpose().mmul(Z));
		/*
		DoubleMatrix[] eig = Eigen.symmetricEigenvectors(A);
		test.printMatrix(eig[0]);
		test.printMatrix(eig[1]);
		*/
		//System.out.println(A.get(0, 2));
		//L.columnMeans().print();
		//centerKernelMatrix(A).print();
		/*int rank = 0;
		double[] sv = A.diag().toArray();
		while (sv[rank] != 0.0)
			rank++;
		int[] cindices = new int[A.columns-rank];
		for (int i = 0; i < A.columns - rank; i++)
			cindices[i] = rank + i;
		DoubleMatrix basis = A.getColumns(cindices);
		basis.print();
		A.print();
		A.getColumn(2).print(); */
		
		
		
	}
}
