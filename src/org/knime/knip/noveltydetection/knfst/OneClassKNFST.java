package org.knime.knip.noveltydetection.knfst;

import java.util.Arrays;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.knime.core.data.DataRow;
import org.knime.core.node.ExecutionMonitor;
import org.knime.knip.noveltydetection.kernel.KernelCalculator;

public class OneClassKNFST extends KNFST {

        public OneClassKNFST() {

        }

        public OneClassKNFST(final KernelCalculator kernel, ExecutionMonitor progMon) throws KNFSTException {
                super(kernel);

                ExecutionMonitor kernelProgMon = progMon.createSubProgress(0.3);
                ExecutionMonitor nullspaceProgMon = progMon.createSubProgress(0.7);

                // get number of training samples
                final RealMatrix kernelMatrix = m_kernel.kernelize(kernelProgMon);
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
                nullspaceProgMon.setProgress(1.0, "Finished calculating nullspace");
                final int[] indices = new int[n];
                for (int i = 0; i < n; i++)
                        indices[i] = i;
                RealMatrix projectionTraining = k.getSubMatrix(0, n - 1, 0, k.getColumnDimension() - 1).multiply(projection);
                this.m_targetPoints = MatrixUtils.createRowRealMatrix(MatrixFunctions.columnMeans(
                                k.getSubMatrix(0, n - 1, 0, k.getColumnDimension() - 1).multiply(projection)).toArray());
                this.m_projection = projection.getSubMatrix(0, n - 1, 0, projection.getColumnDimension() - 1);
                this.m_betweenClassDistances = new double[] {Math.abs(m_targetPoints.getEntry(0, 0))};
        }

        public OneClassKNFST(final RealMatrix kernelMatrix) throws KNFSTException {
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
                RealMatrix projectionTraining = k.getSubMatrix(0, n - 1, 0, k.getColumnDimension() - 1).multiply(projection);
                this.m_targetPoints = MatrixUtils.createRowRealMatrix(MatrixFunctions.columnMeans(
                                k.getSubMatrix(0, n - 1, 0, k.getColumnDimension() - 1).multiply(projection)).toArray());
                this.m_projection = projection.getSubMatrix(0, n - 1, 0, projection.getColumnDimension() - 1);
                this.m_betweenClassDistances = new double[] {Math.abs(m_targetPoints.getEntry(0, 0))};
        }

        //        @Override
        //        public NoveltyScores scoreTestData(final BufferedDataTable test) {
        //
        //                final RealMatrix kernelMatrix = m_kernel.kernelize(test);
        //
        //                return score(kernelMatrix);
        //        }
        //
        //        @Override
        //        public NoveltyScores scoreTestData(final double[][] test) {
        //                final RealMatrix kernelMatrix = m_kernel.kernelize(test);
        //
        //                return score(kernelMatrix);
        //        }

        @Override
        public NoveltyScores scoreTestData(RealMatrix kernelMatrix) {
                return score(kernelMatrix);
        }

        @Override
        public NoveltyScores scoreTestData(DataRow testInstance) {
                final RealMatrix kernelMatrix = m_kernel.kernelize(testInstance);
                return score(kernelMatrix);
        }

        private NoveltyScores score(final RealMatrix kernelMatrix) {
                // projected test samples:
                final RealMatrix projectionVectors = kernelMatrix.transpose().multiply(m_projection);

                // differences to the target value:
                final RealMatrix diff = projectionVectors.subtract(MatrixFunctions.ones(kernelMatrix.getColumnDimension(), 1).scalarMultiply(
                                m_targetPoints.getEntry(0, 0)));

                // distances to the target value:
                final RealVector scoresVector = MatrixFunctions.sqrt(MatrixFunctions.rowSums(MatrixFunctions.multiplyElementWise(diff, diff)));

                return new NoveltyScores(scoresVector.toArray(), projectionVectors);
        }

        @Override
        public String toString() {
                final int maxLen = 10;
                return "OneClassKNFST [m_kernel="
                                + m_kernel
                                + ", m_projection="
                                + m_projection
                                + ", m_targetPoints="
                                + m_targetPoints
                                + ", m_betweenClassDistances="
                                + (m_betweenClassDistances != null ? Arrays.toString(Arrays.copyOf(m_betweenClassDistances,
                                                Math.min(m_betweenClassDistances.length, maxLen))) : null) + "]";
        }
}
