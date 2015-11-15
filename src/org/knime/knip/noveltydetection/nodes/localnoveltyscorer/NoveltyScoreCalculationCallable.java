package org.knime.knip.noveltydetection.nodes.localnoveltyscorer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Callable;

import org.apache.commons.math3.linear.RealMatrix;
import org.eclipse.core.runtime.internal.adaptor.Semaphore;
import org.knime.knip.noveltydetection.knfst.KNFST;
import org.knime.knip.noveltydetection.knfst.MultiClassKNFST;
import org.knime.knip.noveltydetection.knfst.OneClassKNFST;

public class NoveltyScoreCalculationCallable implements Callable<Double> {

        private final int m_index;
        private final Semaphore m_semaphore;
        private final int m_numNeighbors;
        private final RealMatrix m_trainingKernelMatrix;
        private final RealMatrix m_globalKernelMatrix;
        private final String[] m_labels;
        private final boolean m_normalize;

        private Exception m_exception;

        public NoveltyScoreCalculationCallable(int index, Semaphore semaphore, int numNeighbors, RealMatrix trainingKernelMatrix,
                        RealMatrix globalKernelMatrix, String[] labels, boolean normalize) {
                m_index = index;
                m_semaphore = semaphore;
                m_numNeighbors = numNeighbors;
                m_trainingKernelMatrix = trainingKernelMatrix;
                m_globalKernelMatrix = globalKernelMatrix;
                m_labels = labels;
                m_normalize = normalize;
        }

        @Override
        public Double call() throws Exception {
                // Sort training samples according to distance to current sample in kernel feature space

                ValueIndexPair[] distances = ValueIndexPair.transformArray2ValueIndexPairArray(m_globalKernelMatrix.getColumn(m_index));
                Arrays.sort(distances, new Comparator<ValueIndexPair>() {
                        public int compare(ValueIndexPair o1, ValueIndexPair o2) {
                                double v1 = o1.getValue();
                                double v2 = o2.getValue();
                                int res = 0;
                                if (v1 < v2)
                                        res = 1;
                                if (v1 > v2)
                                        res = -1;
                                return res;
                        }
                });

                // get nearest neighbors
                ValueIndexPair[] neighbors = new ValueIndexPair[m_numNeighbors];
                for (int i = 0; i < neighbors.length; i++) {
                        neighbors[i] = distances[i];
                }

                // Sort neighbors according to class
                // NOTE: Since the instances are ordered by class in the original table
                //      sorting by indices is equivalent
                Arrays.sort(neighbors, new Comparator<ValueIndexPair>() {
                        public int compare(ValueIndexPair o1, ValueIndexPair o2) {
                                int res = o1.getIndex() - o2.getIndex();
                                if (res < 0)
                                        res = -1;
                                if (res > 0)
                                        res = 1;
                                return res;
                        }
                });

                // get local labels and check for one class setting
                boolean oneClass = true;
                String[] localLabels = new String[m_numNeighbors];
                int[] trainingMatrixIndices = new int[m_numNeighbors];
                String currentLabel = m_labels[neighbors[0].getIndex()];
                for (int i = 0; i < localLabels.length; i++) {
                        String label = m_labels[neighbors[i].getIndex()];
                        if (!currentLabel.equals(label)) {
                                oneClass = false;
                        }
                        localLabels[i] = label;
                        trainingMatrixIndices[i] = neighbors[i].getIndex();
                }
                RealMatrix localTrainingKernelMatrix = m_trainingKernelMatrix.getSubMatrix(trainingMatrixIndices, trainingMatrixIndices);

                double score = 0;
                KNFST localModel = null;

                try {
                        if (oneClass) {
                                localModel = new OneClassKNFST(localTrainingKernelMatrix);
                        } else {
                                localModel = new MultiClassKNFST(localTrainingKernelMatrix, localLabels);
                        }

                        score = localModel.scoreTestData(
                                        m_globalKernelMatrix.getColumnMatrix(m_index).getSubMatrix(trainingMatrixIndices, new int[] {0})).getScores()[0];

                        // normalize novelty score
                        if (m_normalize) {
                                double normalizer = Tools.getMin(localModel.getBetweenClassDistances());
                                score = score / normalizer;
                        }
                } catch (Exception e) {
                        m_exception = e;
                } finally {
                        m_semaphore.release();
                }
                return score;
        }

        public void ok() throws Exception {
                if (m_exception != null) {
                        throw m_exception;
                }
        }
}
