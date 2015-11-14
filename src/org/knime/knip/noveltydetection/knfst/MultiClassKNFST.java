package org.knime.knip.noveltydetection.knfst;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.knime.core.data.DataRow;
import org.knime.core.node.ExecutionMonitor;
import org.knime.knip.noveltydetection.kernel.KernelCalculator;

public class MultiClassKNFST extends KNFST {

        private String[] m_labels;

        public MultiClassKNFST() {

        }

        public MultiClassKNFST(KernelCalculator kernel, String[] labels, ExecutionMonitor progMon) throws KNFSTException {
                super(kernel);
                m_labels = labels;

                ExecutionMonitor kernelProgMon = progMon.createSubProgress(0.3);
                ExecutionMonitor nullspaceProgMon = progMon.createSubProgress(0.7);
                RealMatrix kernelMatrix = kernel.kernelize(kernelProgMon);

                // obtain unique class labels
                ClassWrapper[] classes = ClassWrapper.classes(labels);

                // calculate projection of KNFST
                this.m_projection = projection(kernelMatrix, labels);

                nullspaceProgMon.setProgress(1.0, "Finished calculating nullspace");

                // calculate target points ( = projections of training data into the null space)
                m_targetPoints = MatrixUtils.createRealMatrix(classes.length, m_projection.getColumnDimension());
                int n = 0;
                int nOld = 0;
                for (int c = 0; c < classes.length; c++) {
                        n += classes[c].getCount();
                        m_targetPoints.setRowVector(c, MatrixFunctions.columnMeans(kernelMatrix.getSubMatrix(nOld, n - 1, 0,
                                        kernelMatrix.getColumnDimension() - 1).multiply(m_projection)));
                        nOld = n;
                }

                // set betweenClassDistances
                m_betweenClassDistances = MatrixFunctions.calculateRowVectorDistances(m_targetPoints);

        }

        public MultiClassKNFST(RealMatrix kernelMatrix, String[] labels) throws KNFSTException {
                m_labels = labels;
                // obtain unique class labels
                ClassWrapper[] classes = ClassWrapper.classes(labels);

                // calculate projection of KNFST
                this.m_projection = projection(kernelMatrix, labels);

                // calculate target points ( = projections of training data into the null space)
                m_targetPoints = MatrixUtils.createRealMatrix(classes.length, m_projection.getColumnDimension());
                int n = 0;
                int nOld = 0;
                for (int c = 0; c < classes.length; c++) {
                        n += classes[c].getCount();
                        m_targetPoints.setRowVector(c, MatrixFunctions.columnMeans(kernelMatrix.getSubMatrix(nOld, n - 1, 0,
                                        kernelMatrix.getColumnDimension() - 1).multiply(m_projection)));
                        nOld = n;
                }

                // set betweenClassDistances
                m_betweenClassDistances = MatrixFunctions.calculateRowVectorDistances(m_targetPoints);
        }

        //                @Override
        //                public NoveltyScores scoreTestData(BufferedDataTable test, ) {
        //                        // calculate nxm kernel matrix containing similarities between n training samples and m test samples
        //                        RealMatrix kernelMatrix = m_kernel.kernelize(test);
        //        
        //                        return score(kernelMatrix);
        //                }

        //        @Override
        //        public NoveltyScores scoreTestData(double[][] test) {
        //                // calculate nxm kernel matrix containing similarities between n training samples and m test samples
        //                RealMatrix kernelMatrix = m_kernel.kernelize(test);
        //
        //                return score(kernelMatrix);
        //        }

        @Override
        public NoveltyScores scoreTestData(RealMatrix kernelMatrix) {
                // TODO Auto-generated method stub
                return score(kernelMatrix);
        }

        @Override
        public NoveltyScores scoreTestData(DataRow testInstance) {
                RealMatrix kernelMatrix = m_kernel.kernelize(testInstance);
                return score(kernelMatrix);
        }

        private NoveltyScores score(final RealMatrix kernelMatrix) {
                // projected test samples:
                long time1 = System.currentTimeMillis();
                final RealMatrix projectionVectors = kernelMatrix.transpose().multiply(m_projection);

                //squared euclidean distances to target points:
                final RealMatrix squared_distances = squared_euclidean_distances(projectionVectors, m_targetPoints);

                // novelty scores as minimum distance to one of the target points
                final RealVector scoreVector = MatrixFunctions.sqrt(MatrixFunctions.rowMins(squared_distances));
                return new NoveltyScores(scoreVector.toArray(), projectionVectors);
        }

        private RealMatrix squared_euclidean_distances(final RealMatrix x, final RealMatrix y) {
                final RealMatrix distmat = MatrixUtils.createRealMatrix(x.getRowDimension(), y.getRowDimension());

                for (int i = 0; i < x.getRowDimension(); i++) {
                        for (int j = 0; j < y.getRowDimension(); j++) {
                                final RealVector buff = x.getRowVector(i).subtract(y.getRowVector(j));
                                distmat.setEntry(i, j, buff.dotProduct(buff));
                        }
                }

                return distmat;
        }

        @Override
        public void readExternal(ObjectInput arg0) throws IOException, ClassNotFoundException {
                // call super method
                super.readExternal(arg0);

                // read labels
                m_labels = new String[arg0.readInt()];
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

        @Override
        public int hashCode() {
                final int prime = 31;
                int result = super.hashCode();
                result = prime * result + Arrays.hashCode(m_labels);
                return result;
        }

        @Override
        public boolean equals(Object obj) {
                if (this == obj) {
                        return true;
                }
                if (!super.equals(obj)) {
                        return false;
                }
                if (!(obj instanceof MultiClassKNFST)) {
                        return false;
                }
                MultiClassKNFST other = (MultiClassKNFST) obj;
                if (!Arrays.equals(m_labels, other.m_labels)) {
                        return false;
                }
                return true;
        }

        @Override
        public String toString() {
                final int maxLen = 10;
                return "MultiClassKNFST [m_labels="
                                + (m_labels != null ? Arrays.asList(m_labels).subList(0, Math.min(m_labels.length, maxLen)) : null)
                                + ", m_kernel="
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
