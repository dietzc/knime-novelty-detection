package org.knime.knip.noveltydetection.nodes.localnoveltyscorer;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.math3.linear.RealMatrix;
import org.knime.knip.noveltydetection.knfst.alternative.KNFST;
import org.knime.knip.noveltydetection.knfst.alternative.MultiClassKNFST;
import org.knime.knip.noveltydetection.knfst.alternative.OneClassKNFST;

public class ExecutionThread implements Runnable {
        private RealMatrix m_globalKernelMatrix;
        private RealMatrix m_trainingKernelMatrix;
        private int m_numNeighbors;
        private String[] m_labels;
        private ThreadController m_controller;

        public ExecutionThread(ThreadController controller, RealMatrix globalKernelMatrix, RealMatrix trainingKernelMatrix, String[] labels,
                        int numNeighbors) {
                m_controller = controller;
                m_globalKernelMatrix = globalKernelMatrix;
                m_trainingKernelMatrix = trainingKernelMatrix;
                m_labels = labels;
                m_numNeighbors = numNeighbors;
        }

        @Override
        public void run() {

                int testIndex = m_controller.getNextIndex();

                while (testIndex != -1) {

                        // Sort training samples according to distance to current sample in kernel feature space

                        ValueIndexPair[] distances = ValueIndexPair.transformArray2ValueIndexPairArray(m_globalKernelMatrix.getColumn(testIndex));
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

                        if (oneClass) {
                                localModel = new OneClassKNFST(localTrainingKernelMatrix);
                        } else {
                                localModel = new MultiClassKNFST(localTrainingKernelMatrix, localLabels);
                        }

                        double normalizer = Tools.getMin(localModel.getBetweenClassDistances());
                        score = localModel.scoreTestData(
                                        m_globalKernelMatrix.getColumnMatrix(testIndex).getSubMatrix(trainingMatrixIndices, new int[] {0}))
                                        .getScores()[0]
                                        / normalizer;

                        // save novelty score
                        m_controller.saveNoveltyScore(testIndex, score);

                        // get index of next test sample to process
                        testIndex = m_controller.getNextIndex();
                }
        }

}
