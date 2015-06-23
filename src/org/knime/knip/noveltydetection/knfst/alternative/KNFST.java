package org.knime.knip.noveltydetection.knfst.alternative;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;
import org.jblas.Eigen;
import org.jblas.MatrixFunctions;
import org.jblas.Singular;
import org.jblas.ranges.IntervalRange;
import org.knime.core.node.BufferedDataTable;

public abstract class KNFST implements Externalizable {
        protected KernelCalculator m_kernel;
        protected DoubleMatrix m_projection;
        protected DoubleMatrix m_targetPoints;

        public KNFST(KernelCalculator kernel) {
                m_kernel = kernel;
        }

        public abstract double[] scoreTestData(BufferedDataTable test);

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
                        IntervalRange rrange = new IntervalRange(l - count, l);
                        IntervalRange crange = new IntervalRange(l - count, l);
                        L = L.put(rrange, crange, DoubleMatrix.ones(count, count).mul(1.0 / (double) classes.get(k).getCount()));
                        count = 0;
                }

                // need Matrix M with all entries 1/m to modify basisvecs which allows usage of 
                // uncentered kernel values (eye(size(M)).M)*basisvecs
                DoubleMatrix M = DoubleMatrix.ones(kernelMatrix.columns, kernelMatrix.columns).mul(1.0 / kernelMatrix.columns);

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
                for (; rank < singularvalues.length && singularvalues[rank] > 1e-12; rank++)
                        ;
                int[] cindices = new int[svd[2].columns - rank];
                for (int i = 0; i < svd[2].columns - rank; i++)
                        cindices[i] = rank + i;
                DoubleMatrix basis = svd[2].getColumns(cindices);
                return basis;
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
                        final double[] projData = new double[arg0.readInt()];
                        for (int d = 0; d < projData.length; d++)
                                projData[d] = arg0.readDouble();
                        // Matrix construction
                        m_projection = new DoubleMatrix(rowsProj, colsProj, projData);

                        // read targetPoints
                        // rows
                        final int rowsTar = arg0.readInt();
                        // columns
                        final int colsTar = arg0.readInt();
                        // data
                        final double[] tarData = new double[arg0.readInt()];
                        for (int d = 0; d < tarData.length; d++)
                                tarData[d] = arg0.readDouble();
                        // Matrix construction
                        m_targetPoints = new DoubleMatrix(rowsTar, colsTar, tarData);

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
                arg0.writeInt(m_projection.getRows());
                // columns
                arg0.writeInt(m_projection.getColumns());
                // data
                arg0.writeInt(m_projection.getLength());
                final double[] projData = m_projection.toArray();
                for (double d : projData)
                        arg0.writeDouble(d);

                // write targetPoints
                // rows
                arg0.writeInt(m_targetPoints.getRows());
                // columns
                arg0.writeInt(m_targetPoints.getColumns());
                // data
                arg0.writeInt(m_targetPoints.getLength());
                final double[] tarData = m_targetPoints.toArray();
                for (double d : tarData)
                        arg0.writeDouble(d);
        }

        public static void main(String[] args) {
                String[] labels = {"A", "A", "A", "B", "B", "C"};
                ArrayList<ClassWrapper> classes = ClassWrapper.classes(labels);
                double[] elements = {1, 1, 1, 2, 2, 2, 3, 3, 3};
                DoubleMatrix A = new DoubleMatrix(3, 3, elements);
                DoubleMatrix L = DoubleMatrix.zeros(6, 6);
                DoubleMatrix Z = nullspace(A);
                for (int i = 0; i < 9; i++)
                        System.out.println(A.get(i));

                Test.printMatrix(A);
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
