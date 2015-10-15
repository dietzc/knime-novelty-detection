package org.knime.knip.noveltydetection.knfst;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.knime.core.node.BufferedDataTable;

public abstract class KNFST implements Externalizable {
        protected KernelCalculator m_kernel;
        protected RealMatrix m_projection;
        protected RealMatrix m_targetPoints;
        protected double[] m_betweenClassDistances;

        public KNFST() {

        }

        public KNFST(KernelCalculator kernel) {
                m_kernel = kernel;
        }

        public abstract NoveltyScores scoreTestData(BufferedDataTable test);

        public abstract NoveltyScores scoreTestData(double[][] test);

        public abstract NoveltyScores scoreTestData(RealMatrix kernelMatrix);

        public static RealMatrix projection(final RealMatrix kernelMatrix, final String[] labels) {

                ClassWrapper[] classes = ClassWrapper.classes(labels);

                // check labels
                if (classes.length == 1) {
                        throw new IllegalArgumentException(
                                        "not able to calculate a nullspace from data of a single class using KNFST (input variable \"labels\" only contains a single value)");
                }

                // check kernel matrix
                if (!kernelMatrix.isSquare()) {
                        throw new IllegalArgumentException("The KernelMatrix must be quadratic!");
                }

                // calculate weights of orthonormal basis in kernel space
                final RealMatrix centeredK = centerKernelMatrix(kernelMatrix);

                /* Test for equality
                RealMatrix matrixI = MatrixUtils.createRealIdentityMatrix(kernelMatrix.getColumnDimension());
                RealMatrix matrixM = MatrixFunctions.ones(kernelMatrix.getColumnDimension(), kernelMatrix.getColumnDimension()).scalarMultiply(
                                1 / kernelMatrix.getColumnDimension());
                RealMatrix matrixImM = matrixI.subtract(matrixM);
                final RealMatrix centeredK = (matrixImM.transpose()).multiply(kernelMatrix).multiply(matrixImM);
                */

                /*
                EigenDecomposition eig = new EigenDecomposition(centeredK);
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
                 */
                EigenDecomposition eig = new EigenDecomposition(centeredK);
                double[] eigVals = eig.getRealEigenvalues();
                ArrayList<Integer> nonZeroEigValIndices = new ArrayList<Integer>();
                for (int i = 0; i < eigVals.length; i++) {
                        if (eigVals[i] > 1e-12) {
                                nonZeroEigValIndices.add(i);
                        }
                }
                double[] nonZeroEigVals = new double[nonZeroEigValIndices.size()];

                int eigIterator = 0;
                RealMatrix eigVecs = eig.getV();
                RealMatrix basisvecs = MatrixUtils.createRealMatrix(eigVecs.getRowDimension(), nonZeroEigValIndices.size());
                for (Integer index : nonZeroEigValIndices) {
                        double normalizer = 1 / Math.sqrt(eigVals[index]);
                        RealVector basisVec = eigVecs.getColumnVector(eigIterator).mapMultiply(normalizer);
                        basisvecs.setColumnVector(eigIterator++, basisVec);
                }

                // calculate transformation T of within class scatter Sw:
                // T= B'*K*(I-L) and L a block matrix
                RealMatrix L = kernelMatrix.createMatrix(kernelMatrix.getRowDimension(), kernelMatrix.getColumnDimension());
                int start = 0;
                for (ClassWrapper cl : classes) {
                        int count = cl.getCount();
                        L.setSubMatrix(MatrixFunctions.ones(count, count).scalarMultiply(1.0 / (double) count).getData(), start, start);
                        start += count;
                }

                // need Matrix M with all entries 1/m to modify basisvecs which allows usage of 
                // uncentered kernel values (eye(size(M)).M)*basisvecs
                RealMatrix M = MatrixFunctions.ones(kernelMatrix.getColumnDimension(), kernelMatrix.getColumnDimension()).scalarMultiply(
                                1.0 / kernelMatrix.getColumnDimension());
                RealMatrix I = MatrixUtils.createRealIdentityMatrix(M.getColumnDimension());

                // compute helper matrix H
                RealMatrix H = ((I.subtract(M)).multiply(basisvecs)).transpose().multiply(kernelMatrix).multiply(I.subtract(L));

                // T = H*H' = B'*Sw*B with B=basisvecs
                RealMatrix T = H.multiply(H.transpose());

                //calculate weights for null space
                RealMatrix eigenvecs = MatrixFunctions.nullspace(T);

                if (eigenvecs == null) {
                        System.out.println("No Nullspace!");
                        EigenDecomposition eigenComp = new EigenDecomposition(T);
                        double[] eigenvals = eigenComp.getRealEigenvalues();
                        eigenvecs = eigenComp.getV();
                        int minId = MatrixFunctions.argmin(MatrixFunctions.abs(eigenvals));
                        double[] eigenvecsData = eigenvecs.getColumn(minId);
                        eigenvecs = MatrixUtils.createColumnRealMatrix(eigenvecsData);
                }

                //System.out.println("eigenvecs:");
                //test.printMatrix(eigenvecs);

                // calculate null space projection
                //DoubleMatrix proj = DoubleMatrix.eye(M.getColumns()).sub(M).mmul(basisvecs);
                RealMatrix proj = ((I.subtract(M)).multiply(basisvecs)).multiply(eigenvecs);

                return proj;
        }

        private static RealMatrix centerKernelMatrix(final RealMatrix kernelMatrix) {
                // get size of kernelMatrix
                int n = kernelMatrix.getRowDimension();

                // get mean values for each row/column
                RealVector columnMeans = MatrixFunctions.columnMeans(kernelMatrix);
                double matrixMean = MatrixFunctions.mean(kernelMatrix);

                RealMatrix centeredKernelMatrix = kernelMatrix.copy();

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
                        m_projection = MatrixUtils.createRealMatrix(projData);

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

                        // read betweenClassDistances
                        final double[] betweenClassDistances = new double[arg0.readInt()];
                        for (int i = 0; i < betweenClassDistances.length; i++) {
                                betweenClassDistances[i] = arg0.readDouble();
                        }
                        m_betweenClassDistances = betweenClassDistances;

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

                // write betweenClassDistances
                // length
                arg0.writeInt(m_betweenClassDistances.length);
                // data
                for (double dist : m_betweenClassDistances) {
                        arg0.writeDouble(dist);
                }
        }

        public double[] getBetweenClassDistances() {
                return m_betweenClassDistances;
        }

        public KernelCalculator getKernel() {
                return m_kernel;
        }

        public RealMatrix getProjection() {
                return m_projection;
        }

        public double[][] getTargetPoints() {
                return m_targetPoints.getData();
        }

        public int getNullspaceDimension() {
                return m_targetPoints.getColumnDimension();
        }

        @Override
        public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + Arrays.hashCode(m_betweenClassDistances);
                result = prime * result + ((m_kernel == null) ? 0 : m_kernel.hashCode());
                result = prime * result + ((m_projection == null) ? 0 : m_projection.hashCode());
                result = prime * result + ((m_targetPoints == null) ? 0 : m_targetPoints.hashCode());
                return result;
        }

        @Override
        public boolean equals(Object obj) {
                if (this == obj) {
                        return true;
                }
                if (obj == null) {
                        return false;
                }
                if (!(obj instanceof KNFST)) {
                        return false;
                }
                KNFST other = (KNFST) obj;
                if (!Arrays.equals(m_betweenClassDistances, other.m_betweenClassDistances)) {
                        return false;
                }
                if (m_kernel == null) {
                        if (other.m_kernel != null) {
                                return false;
                        }
                } else if (!m_kernel.equals(other.m_kernel)) {
                        return false;
                }
                if (m_projection == null) {
                        if (other.m_projection != null) {
                                return false;
                        }
                } else if (!m_projection.equals(other.m_projection)) {
                        return false;
                }
                if (m_targetPoints == null) {
                        if (other.m_targetPoints != null) {
                                return false;
                        }
                } else if (!m_targetPoints.equals(other.m_targetPoints)) {
                        return false;
                }
                return true;
        }

        @Override
        public String toString() {
                return "KNFST [m_kernel=" + m_kernel + ", m_projection=" + m_projection + ", m_targetPoints=" + m_targetPoints
                                + ", m_betweenClassDistances=" + Arrays.toString(m_betweenClassDistances) + "]";
        }
}
