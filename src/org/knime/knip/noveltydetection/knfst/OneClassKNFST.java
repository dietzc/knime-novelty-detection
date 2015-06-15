package org.knime.knip.noveltydetection.knfst;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.jblas.ranges.IntervalRange;
import org.knime.core.node.BufferedDataTable;

public class OneClassKNFST extends KNFST {

        public OneClassKNFST(KernelCalculator kernel) {
                super(kernel);
                // get number of training samples
                int n = m_kernel.getNumTrainingSamples();
                DoubleMatrix kernelMatrix = m_kernel.kernelize();

                // include dot products of training samples and the origin in feature space (these dot products are always zero!)
                final DoubleMatrix k = DoubleMatrix.concatVertically(DoubleMatrix.concatHorizontally(kernelMatrix, DoubleMatrix.zeros(n)),
                                DoubleMatrix.zeros(n + 1));

                // create one-class labels + a different label for the origin
                final String[] labels = new String[n + 1];
                for (int l = 0; l < n; l++)
                        labels[l] = (l == n - 1) ? "0" : "1";

                // get model parameters
                final DoubleMatrix projection = projection(k, labels);
                this.m_targetPoints = kernelMatrix.mmul(projection).columnMeans();
                this.m_projection = projection.getRows(new IntervalRange(0, n - 1));
        }

        @Override
        public double[] scoreTestData(BufferedDataTable test) {

                final DoubleMatrix kernelMatrix = m_kernel.kernelize(test);

                // projected test samples:
                final DoubleMatrix projectionVectors = kernelMatrix.transpose().mmul(m_projection);

                // differences to the target value:
                DoubleMatrix diff = projectionVectors.sub(DoubleMatrix.ones(kernelMatrix.getColumns(), 1).mmul(m_targetPoints));

                // distances to the target value:
                DoubleMatrix scoresVector = MatrixFunctions.sqrt(diff.mul(diff).rowSums());

                return scoresVector.toArray();
        }
}
