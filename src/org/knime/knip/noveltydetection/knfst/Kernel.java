package org.knime.knip.noveltydetection.knfst;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jblas.DoubleMatrix;
import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;

public class Kernel implements Externalizable {

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
                int index = 0;

                for (DataRow sample1 : test) {
                        for (DataRow sample2 : training) {
                                kernelMatrix.put(index++, m_kernelFunction.calculate(sample1, sample2));
                        }
                }

                return kernelMatrix;
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
                // TODO Auto-generated method stub

        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
                out.writeUTF(m_kernelFunction.getClass().getName());
                m_kernelFunction.writeExternal(out);

        }

}
