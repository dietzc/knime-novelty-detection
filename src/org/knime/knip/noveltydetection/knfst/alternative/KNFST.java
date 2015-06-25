package org.knime.knip.noveltydetection.knfst.alternative;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.knime.core.node.BufferedDataTable;

public abstract class KNFST implements Externalizable {
        protected KernelCalculator m_kernel;
        protected RealMatrix m_projection;
        protected RealMatrix m_targetPoints;

        public KNFST(KernelCalculator kernel) {
                m_kernel = kernel;
        }

        public abstract double[] scoreTestData(BufferedDataTable test);

        public static RealMatrix projection(final RealMatrix kernelMatrix, final String[] labels) {

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
                final RealMatrix centeredK = centerKernelMatrix(kernelMatrix);

                EigenDecomposition eig = new EigenDecomposition(centeredK);
                //final RealMatrix[] eig = Eigen.symmetricEigenvectors(centeredK);

                //System.out.println("eig:");
                //test.printMatrix(eig[0]);
                //test.printMatrix(eig[1]);
                final double[] basisValues = eig.getRealEigenvalues();

                // get number and position of nonzero basis values
                final ArrayList<Integer> indices = new ArrayList<Integer>();
                for (int i = 0; i < basisValues.length; i++) {
                        if (basisValues[i] > 1e-12) {
                                indices.add(i);
                        }
                }

                // convert ArrayList<Integer> indices into int[] intIndices
                // create Array with nonzero resized basis values
                final int[] colIndices = new int[indices.size()];
                final double[] nonzeroBasisValues = new double[indices.size()];
                for (int i = 0; i < indices.size(); i++) {
                        nonzeroBasisValues[i] = 1 / Math.sqrt(basisValues[indices.get(i)]);
                        colIndices[i] = indices.get(i);
                }
                final int[] rowIndices = new int[eig.getV().getRowDimension()];
                for (int i = 0; i < eig.getV().getRowDimension(); i++)
                        rowIndices[i] = i;

                // get basis vectors with nonzero basis values
                RealMatrix basisvecs = eig.getV().getSubMatrix(rowIndices, colIndices);

                // create diagonal matrix with nonzero basis values
                final RealMatrix basisvecsValues = MatrixUtils.createRealDiagonalMatrix(nonzeroBasisValues);

                //test.printMatrix(basisvecs);
                //test.printMatrix(basisvecsValues);

                basisvecs = basisvecs.multiply(basisvecsValues);

                // calculate transformation T of within class scatter Sw:
                // T= B'*K*(I-L) and L a block matrix
                RealMatrix L = kernelMatrix.createMatrix(kernelMatrix.getRowDimension(), kernelMatrix.getColumnDimension());
                int l = 0;
                int count = 0;
                int start = 0;
                for (int k = 0; k < classes.size(); k++) {
                        for (; l < labels.length && labels[l].equals(classes.get(k).getName()); l++) {
                                count++;
                        }
                        L.setSubMatrix(MatrixFunctions.ones(count, count).scalarMultiply(1.0 / (double) classes.get(k).getCount()).getData(), start,
                                        start);
                        start = count;
                        count = 0;
                }

                // need Matrix M with all entries 1/m to modify basisvecs which allows usage of 
                // uncentered kernel values (eye(size(M)).M)*basisvecs
                RealMatrix M = MatrixFunctions.ones(kernelMatrix.getColumnDimension(), kernelMatrix.getColumnDimension()).scalarMultiply(
                                1.0 / kernelMatrix.getColumnDimension());

                // compute helper matrix H
                RealMatrix H = MatrixUtils.createRealIdentityMatrix(M.getColumnDimension()).subtract(M).multiply(basisvecs).transpose();
                RealMatrix K = kernelMatrix.multiply(MatrixUtils.createRealIdentityMatrix(kernelMatrix.getColumnDimension()).subtract(L));
                H = H.multiply(K);

                // T = H*H' = B'*Sw*B with B=basisvecs
                RealMatrix T = H.multiply(H.transpose());

                //calculate weights for null space
                RealMatrix eigenvecs = MatrixFunctions.nullspace(T);

                if (eigenvecs.getColumnDimension() < 1) {
                        EigenDecomposition eigenComp = new EigenDecomposition(T);
                        double[] eigenvals = eigenComp.getRealEigenvalues();
                        eigenvecs = eigenComp.getV();
                        int minId = MatrixFunctions.argmin(MatrixFunctions.abs(eigenvals));
                        double[][] eigenvecsData = {eigenvecs.getColumn(minId)};
                        eigenvecs = MatrixUtils.createRealMatrix(eigenvecsData);
                }

                //System.out.println("eigenvecs:");
                //test.printMatrix(eigenvecs);

                // calculate null space projection
                //DoubleMatrix proj = DoubleMatrix.eye(M.getColumns()).sub(M).mmul(basisvecs);
                RealMatrix h1 = MatrixUtils.createRealIdentityMatrix(M.getColumnDimension()).subtract(M);
                //System.out.println("h1:");
                //test.printMatrix(h1);
                //System.out.println("basisvecs:");
                //test.printMatrix(basisvecs);
                RealMatrix proj = h1.multiply(basisvecs);
                //System.out.println("proj:");
                //test.printMatrix(proj);
                proj = proj.multiply(eigenvecs);

                return proj;
        }

        private static RealMatrix centerKernelMatrix(RealMatrix kernelMatrix) {
                // get size of kernelMatrix
                int n = kernelMatrix.getRowDimension();

                // get mean values for each row/column
                RealVector columnMeans = MatrixFunctions.columnMeans(kernelMatrix);
                double matrixMean = MatrixFunctions.mean(kernelMatrix);

                RealMatrix centeredKernelMatrix = kernelMatrix;

                for (int k = 0; k < n; k++) {
                        centeredKernelMatrix.setRowVector(k, centeredKernelMatrix.getRowVector(k).subtract(columnMeans));
                        centeredKernelMatrix.setColumnVector(k, centeredKernelMatrix.getColumnVector(k).subtract(columnMeans));
                }

                centeredKernelMatrix = centeredKernelMatrix.scalarAdd(matrixMean);

                return centeredKernelMatrix;
        }

        // Methods of Externalizable interface

        public void readExternal(ObjectInput arg0) throws IOException, ClassNotFoundException {

                try {
                        // read kernel
                        m_kernel = (KernelCalculator) Class.forName(arg0.readUTF()).newInstance();
                        m_kernel.readExternal(arg0);

                        // read projection
                        // rows
                        final int rowsProj = arg0.readInt();
                        // columns
                        final int colsProj = arg0.readInt();
                        // data
                        final double[][] projData = new double[rowsProj][colsProj];
                        for (int r = 0; r < rowsProj; r++)
                                for (int c = 0; c < colsProj; c++)
                                        projData[r][c] = arg0.readDouble();
                        // Matrix construction
                        m_targetPoints = MatrixUtils.createRealMatrix(projData);

                        // read targetPoints
                        // rows
                        final int rowsTar = arg0.readInt();
                        // columns
                        final int colsTar = arg0.readInt();
                        // data
                        final double[][] tarData = new double[rowsTar][colsTar];
                        for (int r = 0; r < rowsTar; r++)
                                for (int c = 0; c < colsTar; c++)
                                        tarData[r][c] = arg0.readDouble();
                        // Matrix construction
                        m_targetPoints = MatrixUtils.createRealMatrix(tarData);

                } catch (InstantiationException | IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
        }

        public void writeExternal(ObjectOutput arg0) throws IOException {
                // write kernel
                arg0.writeUTF(m_kernel.getClass().getName());
                m_kernel.writeExternal(arg0);

                // write projection
                // rows
                arg0.writeInt(m_projection.getRowDimension());
                // columns
                arg0.writeInt(m_projection.getColumnDimension());
                // data
                final double[][] projData = m_projection.getData();
                for (double[] row : projData)
                        for (double cell : row)
                                arg0.writeDouble(cell);

                // write targetPoints
                // rows
                arg0.writeInt(m_targetPoints.getRowDimension());
                //columns
                arg0.writeInt(m_targetPoints.getColumnDimension());
                // data
                final double[][] tarData = m_targetPoints.getData();
                for (double[] row : tarData)
                        for (double cell : row)
                                arg0.writeDouble(cell);
        }

        public static void main(String[] args) {
                double[][] data = { {1, 2, 3, 4}, {1, 2, 3, 4}, {1, 2, 3, 4}, {1, 2, 3, 4}};
                RealMatrix A = MatrixUtils.createRealMatrix(data);
                EigenDecomposition eig = new EigenDecomposition(A);
                RealMatrix V = eig.getV();
                RealMatrix D = eig.getD();
                SingularValueDecomposition svd = new SingularValueDecomposition(A);
                RealMatrix svdV = svd.getV();
                RealMatrix svdS = svd.getS();
                int rank = svd.getRank();
                RealMatrix ones = MatrixFunctions.ones(3, 4);
                System.out.println("done");
                /*String[] labels = {"A", "A", "A", "B", "B", "C"};
                ArrayList<ClassWrapper> classes = ClassWrapper.classes(labels);
                double[] elements = {1, 1, 1, 2, 2, 2, 3, 3, 3};
                RealMatrix A = new DoubleMatrix(3, 3, elements);
                RealMatrix L = DoubleMatrix.zeros(6, 6);
                RealMatrix Z = nullspace(A);
                for (int i = 0; i < 9; i++)
                        System.out.println(A.get(i));

                Test.printMatrix(A);
                */
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
