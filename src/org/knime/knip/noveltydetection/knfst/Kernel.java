package org.knime.knip.noveltydetection.knfst;

import org.jblas.DoubleMatrix;
import org.jblas.ranges.Range;
import org.jblas.ranges.RangeUtils;
import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;

public class Kernel {

        // Holds the training data
        private BufferedDataTable m_trainingData;

        private KernelFunction m_kernelFunction;

        public Kernel(BufferedDataTable trainingData, KernelFunction kernelFunction) {
                m_trainingData = trainingData;
                m_kernelFunction = kernelFunction;
        }

        /* Returns kernel matrix containing similarities of the training data
         * Output:  mxm matrix containing similarities of the training data
         */
        public DoubleMatrix kernelize() {
                return calculateKernelMatrix(m_trainingData, m_trainingData);
        }

        /* Returns kernel matrix containing similarities of test data with training data
         * Parameters:  testData:   BufferedDataTable containing the test data
         * Output:  nxm matrix containing the similarities of n test samples with m training samples
         */
        public DoubleMatrix kernelize(BufferedDataTable testData) {
                return calculateKernelMatrix(m_trainingData, testData);
        }

        public int getNumTrainingSamples() {
                return m_trainingData.getRowCount();
        }

        /* Private helper function to calculate the kernel matrix for of two data tables
         * Parameters:  training:   KNIME BufferedDataTable containing the training data
         *              test:       KNIME BufferedDataTable containing the test data
         * Output: The kernel matrix for the test and training data
         */
        private DoubleMatrix calculateKernelMatrix(BufferedDataTable training, BufferedDataTable test) {
                final DoubleMatrix kernelMatrix = DoubleMatrix.zeros(test.getRowCount(), training.getRowCount());
                final Range index = RangeUtils.indices(kernelMatrix);

                for (DataRow sample1 : test) {
                        for (DataRow sample2 : training) {
                                kernelMatrix.put(index.value(), m_kernelFunction.calculate(sample1, sample2));
                                index.next();
                        }
                }

                return kernelMatrix;
        }

}
