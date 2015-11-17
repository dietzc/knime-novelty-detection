package org.knime.knip.noveltydetection.nodes.localnoveltyscorer;

import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import org.apache.commons.math3.linear.RealMatrix;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.util.KNIMETimer;
import org.knime.core.util.ThreadPool;

public class LocalNoveltyScorer {

        private ExecutionMonitor m_exec;
        private RealMatrix m_globalKernelMatrix;
        private RealMatrix m_trainingKernelMatrix;
        private String[] m_labels;
        private int m_numNeighbors;
        private boolean m_normalize;

        private double[] m_noveltyScores;
        private int m_currentIndex;

        public LocalNoveltyScorer(ExecutionMonitor executionMonitor, RealMatrix m_globalKernelMatrix, RealMatrix m_trainingKernelMatrix,
                        String[] m_labels, int m_numNeighbors, boolean m_normalize) {
                super();
                this.m_exec = executionMonitor;
                this.m_globalKernelMatrix = m_globalKernelMatrix;
                this.m_trainingKernelMatrix = m_trainingKernelMatrix;
                this.m_labels = m_labels;
                this.m_numNeighbors = m_numNeighbors;
                this.m_normalize = m_normalize;
        }

        public double[] calculateNoveltyScores() throws Exception {

                final ThreadPool pool = KNIMEConstants.GLOBAL_THREAD_POOL;
                final int procCount = (int) (Runtime.getRuntime().availableProcessors() * (2.0 / 3));
                final Semaphore semaphore = new Semaphore(procCount);

                Callable<double[]> schedulingCallable = new Callable<double[]>() {

                        @Override
                        public double[] call() throws Exception {
                                int numTestSamples = m_globalKernelMatrix.getColumnDimension();
                                NoveltyScoreCalculationCallable[] nct = new NoveltyScoreCalculationCallable[numTestSamples];
                                for (int i = 0; i < numTestSamples; i++) {
                                        nct[i] = new NoveltyScoreCalculationCallable(i, semaphore, m_numNeighbors, m_trainingKernelMatrix,
                                                        m_globalKernelMatrix, m_labels, m_normalize);
                                }
                                final Future<?>[] scores = new Future<?>[numTestSamples];
                                KNIMETimer timer = KNIMETimer.getInstance();
                                TimerTask timerTask = new TimerTask() {
                                        @Override
                                        public void run() {
                                                try {
                                                        m_exec.checkCanceled();
                                                } catch (final CanceledExecutionException ce) {
                                                        for (int i = 0; i < scores.length; i++) {
                                                                if (scores[i] != null) {
                                                                        scores[i].cancel(true);
                                                                }
                                                        }
                                                        super.cancel();
                                                }

                                        }
                                };
                                timer.scheduleAtFixedRate(timerTask, 0, 3000);
                                for (int i = 0; i < numTestSamples; i++) {
                                        semaphore.acquire();
                                        scores[i] = pool.enqueue(nct[i]);
                                }
                                double[] result = new double[numTestSamples];
                                for (int i = 0; i < numTestSamples; i++) {
                                        semaphore.acquire();
                                        result[i] = (Double) scores[i].get();
                                        m_exec.setProgress(((double) i) / numTestSamples,
                                                        "Local novelty score calculated (" + i + "/" + numTestSamples + ")");
                                        semaphore.release();
                                }

                                return result;
                        }

                };

                final int numTestSamples = m_globalKernelMatrix.getColumnDimension();
                final NoveltyScoreCalculationCallable[] nct = new NoveltyScoreCalculationCallable[numTestSamples];
                for (int i = 0; i < numTestSamples; i++) {
                        nct[i] = new NoveltyScoreCalculationCallable(i, semaphore, m_numNeighbors, m_trainingKernelMatrix, m_globalKernelMatrix,
                                        m_labels, m_normalize);
                }
                final Future<?>[] scores = new Future<?>[numTestSamples];
                KNIMETimer timer = KNIMETimer.getInstance();
                TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                                try {
                                        m_exec.checkCanceled();
                                } catch (final CanceledExecutionException ce) {
                                        for (int i = 0; i < scores.length; i++) {
                                                if (scores[i] != null) {
                                                        scores[i].cancel(true);
                                                }
                                        }
                                        super.cancel();
                                }

                        }
                };
                timer.scheduleAtFixedRate(timerTask, 0, 3000);
                double progCounter = 0;
                for (int i = 0; i < numTestSamples; i++) {
                        try {
                                m_exec.checkCanceled();
                        } catch (Exception e) {
                                for (int j = 0; j < i; j++) {
                                        if (scores[j] != null) {
                                                scores[j].cancel(true);
                                        }
                                }
                                timerTask.cancel();
                                throw e;
                        }
                        semaphore.acquire();
                        scores[i] = pool.enqueue(nct[i]);
                        m_exec.setProgress(progCounter / (2 * numTestSamples),
                                        "Local novelty score calculation started (" + i + "/" + numTestSamples + ")");
                        progCounter += 1;
                }
                final double[] result = new double[numTestSamples];

                for (int i = 0; i < numTestSamples; i++) {
                        semaphore.acquire();
                        try {
                                m_exec.checkCanceled();
                                result[i] = (Double) scores[i].get();
                                nct[i].ok();
                        } catch (Exception e) {
                                for (int j = 0; j < scores.length; i++) {
                                        scores[i].cancel(true);
                                }
                                timerTask.cancel();
                                throw e;

                        }
                        m_exec.setProgress(progCounter / (2 * numTestSamples), "Local novelty score calculated (" + i + "/" + numTestSamples + ")");
                        progCounter += 1;
                        semaphore.release();
                }

                timerTask.cancel();

                return result;
        }

}
