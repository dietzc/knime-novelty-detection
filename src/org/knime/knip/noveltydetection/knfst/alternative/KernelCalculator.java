package org.knime.knip.noveltydetection.knfst.alternative;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.BufferedDataTable;

public class KernelCalculator implements Externalizable {
        static final int DEFAULT_NUM_CORES = 4;

        // Holds the training data
        private double[][] m_trainingData;
        private int m_rowCount;
        private int m_colCount;

        private KernelFunction m_kernelFunction;

        public KernelCalculator() {

        }

        public KernelCalculator(BufferedDataTable trainingData, KernelFunction kernelFunction) {
                m_trainingData = readBufferedDataTable(trainingData);
                m_rowCount = trainingData.getRowCount();
                m_colCount = trainingData.getDataTableSpec().getNumColumns();
                m_kernelFunction = kernelFunction;
        }

        public KernelCalculator(double[][] trainingData, KernelFunction kernelFunction) {
                m_trainingData = trainingData;
                m_rowCount = trainingData.length;
                m_colCount = trainingData[0].length;
                m_kernelFunction = kernelFunction;
        }

        public KernelCalculator(KernelFunction kernelFunction) {
                m_kernelFunction = kernelFunction;
        }

        /* Returns kernel matrix containing similarities of the training data
         * Output:  mxm matrix containing similarities of the training data
         */
        public RealMatrix kernelize() {
                return calculateKernelMatrix(m_trainingData, m_trainingData);
        }

        public RealMatrix kernelize(BufferedDataTable trainingData, BufferedDataTable testData) {
                return calculateKernelMatrix(readBufferedDataTable(trainingData), readBufferedDataTable(testData));
        }

        /* Returns kernel matrix containing similarities of test data with training data
         * Parameters:  testData:   BufferedDataTable containing the test data
         * Output:  nxm matrix containing the similarities of n test samples with m training samples
         */
        public RealMatrix kernelize(BufferedDataTable testData) {
                return calculateKernelMatrix(m_trainingData, readBufferedDataTable(testData));
        }

        public RealMatrix kernelize(double[][] testData) {
                return calculateKernelMatrix(m_trainingData, testData);
        }

        public RealMatrix kernelize(double[][] trainingData, double[][] testData) {

                double[][] result = new double[trainingData.length][testData.length];
                // determine number of cores
                int numCores = Runtime.getRuntime().availableProcessors();

                // start threads
                KernelCalculationThread[] threads = new KernelCalculationThread[numCores];
                int colsPerThread = testData.length / numCores;
                for (int t = 0; t < numCores; t++) {
                        int min = t * colsPerThread;
                        int max = (t == numCores - 1) ? testData.length : (t + 1) * colsPerThread - 1;
                        threads[t] = new KernelCalculationThread(Integer.toString(t), m_kernelFunction, trainingData, testData, result, min, max);
                        threads[t].start();
                }

                // wait for threads to finish
                for (int t = 0; t < threads.length; t++) {
                        try {
                                threads[t].join();
                        } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }
                }

                // assemble and return KernelMatrix
                return MatrixUtils.createRealMatrix(result);
        }

        public int getNumTrainingSamples() {
                return m_rowCount;
        }

        /* Private helper function to calculate the kernel matrix for of two data tables
         * Parameters:  training:   KNIME BufferedDataTable containing the training data
         *              test:       KNIME BufferedDataTable containing the test data
         * Output: The kernel matrix for the test and training data
         */
        private RealMatrix calculateKernelMatrix(BufferedDataTable training, BufferedDataTable test) {
                final RealMatrix kernelMatrix = MatrixUtils.createRealMatrix(training.getRowCount(), test.getRowCount());
                Iterator<DataRow> trainingIterator = training.iterator();

                for (int r = 0; r < training.getRowCount(); r++) {
                        Iterator<DataRow> testIterator = test.iterator();
                        for (int c = 0; c < test.getRowCount(); c++) {
                                kernelMatrix.setEntry(r, c, m_kernelFunction.calculate(trainingIterator.next(), testIterator.next()));
                        }
                }

                return kernelMatrix;
        }

        private RealMatrix calculateKernelMatrix(double[][] training, double[][] test) {
                double[][] result = new double[training.length][test.length];
                // determine number of cores
                int numCores = Runtime.getRuntime().availableProcessors();

                // start threads
                KernelCalculationThread[] threads = new KernelCalculationThread[numCores];
                int colsPerThread = test.length / numCores;
                for (int t = 0; t < numCores; t++) {
                        int min = t * colsPerThread;
                        int max = (t == numCores - 1) ? test.length : (t + 1) * colsPerThread;
                        threads[t] = new KernelCalculationThread(Integer.toString(t), m_kernelFunction, training, test, result, min, max);
                        threads[t].start();
                }

                // wait for threads to finish
                for (int t = 0; t < threads.length; t++) {
                        try {
                                threads[t].join();
                        } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }
                }

                // assemble and return KernelMatrix
                return MatrixUtils.createRealMatrix(result);
        }

        private RealMatrix calculateKernelMatrix(double[][] training, BufferedDataTable test) {
                final RealMatrix kernelMatrix = MatrixUtils.createRealMatrix(training.length, test.getRowCount());

                for (int r = 0; r < training.length; r++) {
                        Iterator<DataRow> testIterator = test.iterator();
                        for (int c = 0; c < test.getRowCount(); c++) {
                                kernelMatrix.setEntry(r, c, m_kernelFunction.calculate(training[r], testIterator.next()));
                        }
                }

                return kernelMatrix;
        }

        public static double[][] readBufferedDataTable(final BufferedDataTable table) {
                final double[][] data = new double[table.getRowCount()][table.getDataTableSpec().getNumColumns()];

                final Iterator<DataRow> iterator = table.iterator();

                for (int r = 0; iterator.hasNext(); r++) {
                        DataRow next = iterator.next();
                        for (int c = 0; c < table.getDataTableSpec().getNumColumns(); c++) {
                                data[r][c] = ((DoubleValue) next.getCell(c)).getDoubleValue();
                        }
                }

                return data;
        }

        /************ Externalizable methods *****************/
        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
                try {
                        // read kernelFunction
                        m_kernelFunction = (KernelFunction) Class.forName(in.readUTF()).newInstance();
                        m_kernelFunction.readExternal(in);

                        // read trainingData
                        // rows
                        m_rowCount = in.readInt();
                        // columns
                        m_colCount = in.readInt();
                        // data
                        m_trainingData = new double[m_rowCount][m_colCount];
                        for (int r = 0; r < m_rowCount; r++)
                                for (int c = 0; c < m_colCount; c++)
                                        m_trainingData[r][c] = in.readDouble();

                } catch (InstantiationException | IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }

        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
                // write kernelFunction
                out.writeUTF(m_kernelFunction.getClass().getName());
                m_kernelFunction.writeExternal(out);

                // write trainingData
                // rows
                out.writeInt(m_rowCount);
                // columns
                out.writeInt(m_colCount);
                // data
                for (double[] row : m_trainingData)
                        for (double col : row)
                                out.writeDouble(col);

        }
}
