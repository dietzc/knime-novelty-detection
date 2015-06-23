package org.knime.knip.noveltydetection.knfst.alternative;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.jblas.ranges.IntervalRange;
import org.knime.core.node.BufferedDataTable;

public class OneClassKNFST extends KNFST {

        public OneClassKNFST(KernelCalculator kernel) {
                super(kernel);
                System.out.println("1");
                // get number of training samples
                int n = m_kernel.getNumTrainingSamples();
                DoubleMatrix kernelMatrix = m_kernel.kernelize();
                System.out.println("2");

                // include dot products of training samples and the origin in feature space (these dot products are always zero!)
                final DoubleMatrix k = DoubleMatrix.concatVertically(DoubleMatrix.concatHorizontally(kernelMatrix, DoubleMatrix.zeros(n)),
                                DoubleMatrix.zeros(n + 1).transpose());
                System.out.println("3");

                // create one-class labels + a different label for the origin
                final String[] labels = new String[n + 1];
                for (int l = 0; l <= n; l++)
                        labels[l] = (l == n) ? "0" : "1";

                // get model parameters
                final DoubleMatrix projection = projection(k, labels);
                System.out.println("4");
                int[] indices = new int[n];
                for (int i = 0; i < n; i++)
                        indices[i] = i;
                this.m_targetPoints = k.getRows(indices).mmul(projection).columnMeans();
                System.out.println("5");
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
