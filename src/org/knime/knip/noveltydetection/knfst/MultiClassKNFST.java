package org.knime.knip.noveltydetection.knfst;

import java.util.ArrayList;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.jblas.ranges.IntervalRange;
import org.knime.core.node.BufferedDataTable;

public class MultiClassKNFST extends KNFST {

        private String[] m_labels;

        public MultiClassKNFST(Kernel kernel, String[] labels) {
                super(kernel);
                m_labels = labels;

                DoubleMatrix kernelMatrix = kernel.kernelize();

                // obtain unique class labels
                ArrayList<ClassWrapper> classes = ClassWrapper.classes(labels);

                // calculate projection of KNFST
                this.m_projection = projection(kernelMatrix, labels);

                // calculate target points ( = projections of training data into the null space)
                DoubleMatrix targetPoints = DoubleMatrix.zeros(classes.size(), m_projection.getColumns());
                int n = 0;
                int nOld = 0;
                for (int c = 0; c < classes.size(); c++) {
                        n += classes.get(c).getCount();
                        final IntervalRange interval = new IntervalRange(nOld, n - 1);
                        targetPoints.putRow(c, kernelMatrix.getRows(interval).mmul(m_projection).columnMeans());
                        nOld = n;
                }
        }

        @Override
        public double[] scoreTestData(BufferedDataTable test) {
                // calculate nxm kernel matrix containing similarities between n training samples and m test samples
                DoubleMatrix kernelMatrix = m_kernel.kernelize(test);

                // projected test samples:
                DoubleMatrix projectionVectors = kernelMatrix.transpose().mmul(m_projection);

                //squared euclidean distances to target points:
                DoubleMatrix squared_distances = squared_euclidean_distances(projectionVectors, m_targetPoints);

                // novelty scores as minimum distance to one of the target points
                DoubleMatrix scoreVector = MatrixFunctions.sqrt(squared_distances.rowMins());

                return scoreVector.toArray();
        }

        private DoubleMatrix squared_euclidean_distances(DoubleMatrix x, DoubleMatrix y) {
                DoubleMatrix distmat = DoubleMatrix.zeros(x.getRows(), y.getRows());

                for (int i = 0; i < x.getRows(); i++) {
                        for (int j = 0; j < y.getRows(); j++) {
                                DoubleMatrix buff = x.getRow(i).sub(y.getRow(j));
                                int[] indices = {i, j};
                                distmat.put(indices, buff.mmul(buff.transpose()));
                        }
                }

                return distmat;
        }
}
