package org.knime.knip.noveltydetection.knfst.alternative;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.knime.core.node.BufferedDataTable;

public class OneClassKNFST extends KNFST {

        public OneClassKNFST() {

        }

        public OneClassKNFST(final KernelCalculator kernel) {
                super(kernel);
                // get number of training samples
                final RealMatrix kernelMatrix = m_kernel.kernelize();
                final int n = kernelMatrix.getRowDimension();

                // include dot products of training samples and the origin in feature space (these dot products are always zero!)
                final RealMatrix k = MatrixFunctions.concatVertically(
                                MatrixFunctions.concatHorizontally(kernelMatrix, MatrixUtils.createRealMatrix(kernelMatrix.getRowDimension(), 1)),
                                MatrixUtils.createRealMatrix(1, kernelMatrix.getColumnDimension() + 1));

                // create one-class labels + a different label for the origin
                final String[] labels = new String[n + 1];
                for (int l = 0; l <= n; l++)
                        labels[l] = (l == n) ? "0" : "1";

                // get model parameters
                final RealMatrix projection = projection(k, labels);
                final int[] indices = new int[n];
                for (int i = 0; i < n; i++)
                        indices[i] = i;
                this.m_targetPoints = MatrixUtils.createRowRealMatrix(MatrixFunctions.columnMeans(
                                k.getSubMatrix(0, n, 0, k.getColumnDimension() - 1).multiply(projection)).toArray());
                this.m_projection = projection.getSubMatrix(0, n - 1, 0, projection.getColumnDimension() - 1);
        }

        @Override
        public double[] scoreTestData(final BufferedDataTable test) {

                final RealMatrix kernelMatrix = m_kernel.kernelize(test);

                return score(kernelMatrix);
        }

        @Override
        public double[] scoreTestData(final double[][] test) {
                final RealMatrix kernelMatrix = m_kernel.kernelize(test);

                return score(kernelMatrix);
        }

        private double[] score(final RealMatrix kernelMatrix) {
                // projected test samples:
                final RealMatrix projectionVectors = kernelMatrix.transpose().multiply(m_projection);

                // differences to the target value:
                final RealMatrix diff = projectionVectors.subtract(MatrixFunctions.ones(kernelMatrix.getColumnDimension(), 1)
                                .multiply(m_targetPoints));

                // distances to the target value:
                final RealVector scoresVector = MatrixFunctions.sqrt(MatrixFunctions.rowSums(MatrixFunctions.multiplyElementWise(diff, diff)));

                return scoresVector.toArray();
        }
}
