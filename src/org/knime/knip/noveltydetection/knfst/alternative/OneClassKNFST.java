package org.knime.knip.noveltydetection.knfst.alternative;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.knime.core.node.BufferedDataTable;

public class OneClassKNFST extends KNFST {

        public OneClassKNFST(KernelCalculator kernel) {
                super(kernel);
                System.out.println("1");
                // get number of training samples
                int n = m_kernel.getNumTrainingSamples();
                RealMatrix kernelMatrix = m_kernel.kernelize();
                System.out.println("2");

                // include dot products of training samples and the origin in feature space (these dot products are always zero!)
                final RealMatrix k = MatrixFunctions.concatVertically(
                                MatrixFunctions.concatHorizontally(kernelMatrix, MatrixUtils.createRealMatrix(n, 1)),
                                MatrixUtils.createRealMatrix(1, n + 1));
                System.out.println("3");

                // create one-class labels + a different label for the origin
                final String[] labels = new String[n + 1];
                for (int l = 0; l <= n; l++)
                        labels[l] = (l == n) ? "0" : "1";

                // get model parameters
                final RealMatrix projection = projection(k, labels);
                System.out.println("4");
                int[] indices = new int[n];
                for (int i = 0; i < n; i++)
                        indices[i] = i;
                this.m_targetPoints = MatrixUtils.createRowRealMatrix(MatrixFunctions.columnMeans(
                                k.getSubMatrix(0, n, 0, k.getColumnDimension() - 1).multiply(projection)).toArray());
                System.out.println("5");
                this.m_projection = projection.getSubMatrix(0, n - 1, 0, projection.getColumnDimension() - 1);
        }

        @Override
        public double[] scoreTestData(BufferedDataTable test) {

                final RealMatrix kernelMatrix = m_kernel.kernelize(test);

                // projected test samples:
                final RealMatrix projectionVectors = kernelMatrix.transpose().multiply(m_projection);

                // differences to the target value:
                RealMatrix diff = projectionVectors.subtract(MatrixFunctions.ones(kernelMatrix.getColumnDimension(), 1).multiply(m_targetPoints));

                // distances to the target value:
                RealVector scoresVector = MatrixFunctions.sqrt(MatrixFunctions.rowSums(MatrixFunctions.multiplyElementWise(diff, diff)));

                return scoresVector.toArray();
        }
}
