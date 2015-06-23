package org.knime.knip.noveltydetection.knfst.alternative;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.jblas.ranges.IntervalRange;
import org.knime.core.node.BufferedDataTable;

public class MultiClassKNFST extends KNFST {

        private String[] m_labels;

        public MultiClassKNFST(KernelCalculator kernel, String[] labels) {
                super(kernel);
                m_labels = labels;

                DoubleMatrix kernelMatrix = kernel.kernelize();

                // obtain unique class labels
                ArrayList<ClassWrapper> classes = ClassWrapper.classes(labels);

                // calculate projection of KNFST
                this.m_projection = projection(kernelMatrix, labels);

                // calculate target points ( = projections of training data into the null space)
                m_targetPoints = DoubleMatrix.zeros(classes.size(), m_projection.getColumns());
                int n = 0;
                int nOld = 0;
                for (int c = 0; c < classes.size(); c++) {
                        n += classes.get(c).getCount();
                        final IntervalRange interval = new IntervalRange(nOld, n - 1);
                        m_targetPoints.putRow(c, kernelMatrix.getRows(interval).mmul(m_projection).columnMeans());
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

        @Override
        public void readExternal(ObjectInput arg0) throws IOException, ClassNotFoundException {
                // call super method
                super.readExternal(arg0);

                // read labels
                String[] m_labels = new String[arg0.readInt()];
                for (int l = 0; l < m_labels.length; l++) {
                        m_labels[l] = arg0.readUTF();
                }

        }

        @Override
        public void writeExternal(ObjectOutput arg0) throws IOException {
                // call super method
                super.writeExternal(arg0);

                // write labels
                arg0.writeInt(m_labels.length);
                for (String label : m_labels)
                        arg0.writeUTF(label);

        }
}
