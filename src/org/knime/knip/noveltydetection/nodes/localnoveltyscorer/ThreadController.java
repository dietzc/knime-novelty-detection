package org.knime.knip.noveltydetection.nodes.localnoveltyscorer;

import org.apache.commons.math3.linear.RealMatrix;
import org.knime.core.node.ExecutionContext;

public class ThreadController {
        private ExecutionContext m_exec;
        private RealMatrix m_globalKernelMatrix;
        private RealMatrix m_trainingKernelMatrix;
        private String[] m_labels;
        private int m_numNeighbors;
        private double[] m_noveltyScores;
        private int m_currentIndex;

        public ThreadController(final ExecutionContext exec, final RealMatrix globalKernelMatrix, final RealMatrix trainingKernelMatrix,
                        final String[] labels, final int numNeighbors) {
                m_exec = exec;
                m_globalKernelMatrix = globalKernelMatrix;
                m_trainingKernelMatrix = trainingKernelMatrix;
                m_labels = labels;
                m_numNeighbors = numNeighbors;
                m_noveltyScores = new double[globalKernelMatrix.getColumnDimension()];
        }

        public double[] process() {
                // get number of available cores
                int numCores = Runtime.getRuntime().availableProcessors();

                Thread[] threads = new Thread[numCores];
                for (int i = 0; i < threads.length; i++) {
                        threads[i] = new Thread(new ExecutionThread(this, m_globalKernelMatrix, m_trainingKernelMatrix, m_labels, m_numNeighbors));
                        threads[i].start();
                }
                for (int i = 0; i < threads.length; i++) {
                        try {
                                threads[i].join();
                        } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }
                }

                return m_noveltyScores;
        }

        public synchronized void saveNoveltyScore(int index, double score) {
                m_noveltyScores[index] = score;
                m_exec.setProgress((double) index / m_noveltyScores.length);
        }

        public synchronized int getNextIndex() {
                if (m_currentIndex < m_globalKernelMatrix.getColumnDimension()) {
                        return m_currentIndex++;
                } else {
                        return -1;
                }
        }
}
