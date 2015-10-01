package org.knime.knip.noveltydetection.knfst;

public class KernelCalculationThread extends Thread {
        private final KernelFunction m_kernelFunction;
        private final double[][] m_training;
        private final double[][] m_test;
        private final double[][] m_result;
        private int m_min;
        private int m_max;

        public KernelCalculationThread(String name, KernelFunction kernelFunction, double[][] training, double[][] test, double[][] result, int min,
                        int max) {
                super(name);
                m_kernelFunction = kernelFunction;
                m_training = training;
                m_test = test;
                m_result = result;
                m_min = min;
                m_max = max;
        }

        @Override
        public void run() {
                for (int r1 = 0; r1 < m_training.length; r1++) {
                        for (int r2 = m_min; r2 < m_max; r2++) {
                                m_result[r1][r2] = m_kernelFunction.calculate(m_training[r1], m_test[r2]);
                        }
                }
        }
}
