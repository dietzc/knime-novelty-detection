package org.knime.knip.noveltydetection.knfst.alternative;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.knime.core.node.BufferedDataTable;

public class MultiClassKNFST extends KNFST {

        private String[] m_labels;

        public MultiClassKNFST(KernelCalculator kernel, String[] labels) {
                super(kernel);
                m_labels = labels;

                RealMatrix kernelMatrix = kernel.kernelize();

                // obtain unique class labels
                ArrayList<ClassWrapper> classes = ClassWrapper.classes(labels);

                // calculate projection of KNFST
                this.m_projection = projection(kernelMatrix, labels);

                // calculate target points ( = projections of training data into the null space)
                m_targetPoints = MatrixUtils.createRealMatrix(classes.size(), m_projection.getColumnDimension());
                int n = 0;
                int nOld = 0;
                for (int c = 0; c < classes.size(); c++) {
                        n += classes.get(c).getCount();
                        m_targetPoints.setRowVector(c, MatrixFunctions.columnMeans(kernelMatrix.getSubMatrix(nOld, n - 1, 0,
                                        kernelMatrix.getColumnDimension() - 1).multiply(m_projection)));
                        nOld = n;
                }
        }

        @Override
        public double[] scoreTestData(BufferedDataTable test) {
                // calculate nxm kernel matrix containing similarities between n training samples and m test samples
                RealMatrix kernelMatrix = m_kernel.kernelize(test);

                return score(kernelMatrix);
        }

        @Override
        public double[] scoreTestData(double[][] test) {
                // calculate nxm kernel matrix containing similarities between n training samples and m test samples
                RealMatrix kernelMatrix = m_kernel.kernelize(test);

                return score(kernelMatrix);
        }

        private double[] score(final RealMatrix kernelMatrix) {
                // projected test samples:
                final RealMatrix projectionVectors = kernelMatrix.transpose().multiply(m_projection);

                //squared euclidean distances to target points:
                final RealMatrix squared_distances = squared_euclidean_distances(projectionVectors, m_targetPoints);

                // novelty scores as minimum distance to one of the target points
                final RealVector scoreVector = MatrixFunctions.sqrt(MatrixFunctions.rowMins(squared_distances));

                return scoreVector.toArray();
        }

        private RealMatrix squared_euclidean_distances(RealMatrix x, RealMatrix y) {
                RealMatrix distmat = MatrixUtils.createRealMatrix(x.getRowDimension(), y.getRowDimension());

                for (int i = 0; i < x.getRowDimension(); i++) {
                        for (int j = 0; j < y.getRowDimension(); j++) {
                                RealMatrix buff = x.getRowMatrix(i).subtract(y.getRowMatrix(j));
                                distmat.setSubMatrix(buff.multiply(buff.transpose()).getData(), i, j);
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
