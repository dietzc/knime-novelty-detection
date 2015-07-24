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
                return calculateKernelMatrix(trainingData, testData);
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
                final double[][] kernelMatrixData = new double[training.length][test.length];
                for (int r1 = 0; r1 < training.length; r1++) {
                        for (int r2 = 0; r2 < test.length; r2++) {
                                kernelMatrixData[r1][r2] = m_kernelFunction.calculate(training[r1], test[r2]);
                        }
                }
                return MatrixUtils.createRealMatrix(kernelMatrixData);
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

        private static double[][] readBufferedDataTable(final BufferedDataTable table) {
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
