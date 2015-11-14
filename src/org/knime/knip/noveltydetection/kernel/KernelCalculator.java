package org.knime.knip.noveltydetection.kernel;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.eclipse.core.runtime.internal.adaptor.Semaphore;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.util.ThreadPool;

public class KernelCalculator implements Externalizable {
        static final int DEFAULT_NUM_CORES = 4;

        public enum KernelType {
                HIK("HIK"), EXPHIK("EXPHIK"), RBF("RBF"), POLYNOMIAL("Polynomial");

                private final String m_name;

                private KernelType(String name) {
                        m_name = name;
                }

                public String toString() {
                        return m_name;
                }
        }

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

        //        public KernelCalculator(KernelFunction kernelFunction) {
        //                m_kernelFunction = kernelFunction;
        //        }

        /* Returns kernel matrix containing similarities of the training data
         * Output:  mxm matrix containing similarities of the training data
         */
        public RealMatrix kernelize(ExecutionMonitor progMon) {
                return calculateKernelMatrix(m_trainingData, m_trainingData, progMon);
        }

        public RealMatrix kernelize(BufferedDataTable trainingData, BufferedDataTable testData, ExecutionMonitor progMon) {
                return calculateKernelMatrix(readBufferedDataTable(trainingData), readBufferedDataTable(testData), progMon);
        }

        /* Returns kernel matrix containing similarities of test data with training data
         * Parameters:  testData:   BufferedDataTable containing the test data
         * Output:  nxm matrix containing the similarities of n test samples with m training samples
         */
        public RealMatrix kernelize(BufferedDataTable testData, ExecutionMonitor progMon) {
                return calculateKernelMatrix(m_trainingData, readBufferedDataTable(testData), progMon);
        }

        public RealMatrix kernelize(DataRow testInstance) {
                return calculateKernelVector(m_trainingData, readDataRow(testInstance), m_kernelFunction);
        }

        private double[] readDataRow(DataRow row) {
                double[] data = new double[row.getNumCells()];
                for (int i = 0; i < row.getNumCells(); i++) {
                        DataCell cell = row.getCell(i);
                        if (cell.isMissing()) {
                                throw new IllegalArgumentException("Missing values are not supported.");
                        } else if (!cell.getType().isCompatible(DoubleValue.class)) {
                                throw new IllegalArgumentException("Only numerical data types are currently supported.");
                        } else {
                                data[i] = ((DoubleValue) cell).getDoubleValue();
                        }
                }
                return data;
        }

        public RealMatrix kernelize(double[][] testData, ExecutionMonitor progMon) {
                return calculateKernelMatrix(m_trainingData, testData, progMon);
        }

        public RealMatrix kernelize(double[][] trainingData, double[][] testData, ExecutionMonitor progMon) {

                return calculateKernelMatrix(trainingData, testData, progMon);
        }

        public int getNumTrainingSamples() {
                return m_rowCount;
        }

        /* Private helper function to calculate the kernel matrix for of two data tables
         * Parameters:  training:   KNIME BufferedDataTable containing the training data
         *              test:       KNIME BufferedDataTable containing the test data
         * Output: The kernel matrix for the test and training data
         */
        //        private RealMatrix calculateKernelMatrix(BufferedDataTable training, BufferedDataTable test) {
        //                final RealMatrix kernelMatrix = MatrixUtils.createRealMatrix(training.getRowCount(), test.getRowCount());
        //                Iterator<DataRow> trainingIterator = training.iterator();
        //
        //                for (int r = 0; r < training.getRowCount(); r++) {
        //                        Iterator<DataRow> testIterator = test.iterator();
        //                        for (int c = 0; c < test.getRowCount(); c++) {
        //                                kernelMatrix.setEntry(r, c, m_kernelFunction.calculate(trainingIterator.next(), testIterator.next()));
        //                        }
        //                }
        //
        //                return kernelMatrix;
        //        }

        public RealMatrix oldCalculateKernelMatrix(double[][] training, double[][] test) {
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

        public RealMatrix calculateKernelMatrix(final double[][] training, final double[][] test, final ExecutionMonitor progMon) {

                final ThreadPool pool = KNIMEConstants.GLOBAL_THREAD_POOL;
                int procCount = (int) (Runtime.getRuntime().availableProcessors() * (2.0 / 3));
                final Semaphore semaphore = new Semaphore(procCount);
                RealMatrix result = null;
                try {
                        result = pool.runInvisible(new Callable<RealMatrix>() {

                                @Override
                                public RealMatrix call() throws Exception {
                                        double[][] resultArrayMatrix = new double[training.length][test.length];
                                        CalculateKernelValuesRunnable[] kct = new CalculateKernelValuesRunnable[test.length];
                                        int numberOfRunnables = kct.length;
                                        for (int i = 0; i < numberOfRunnables; i++) {
                                                kct[i] = new CalculateKernelValuesRunnable(0, training.length, i, i + 1, training, test,
                                                                resultArrayMatrix, m_kernelFunction, semaphore);
                                        }
                                        Future<?>[] threads = new Future<?>[numberOfRunnables];
                                        double progCounter = 0;
                                        for (int i = 0; i < numberOfRunnables; i++) {
                                                semaphore.acquire();
                                                threads[i] = pool.enqueue(kct[i]);
                                                progMon.setProgress(progCounter / (2 * numberOfRunnables), "Kernel calculation started (" + i + "/"
                                                                + numberOfRunnables + ")");
                                                progCounter += 1;
                                        }
                                        for (int i = 0; i < numberOfRunnables; i++) {
                                                semaphore.acquire();
                                                threads[i].get();
                                                semaphore.release();
                                                progMon.setProgress(progCounter / (2 * numberOfRunnables), "Kernel calculation finished (" + i + "/"
                                                                + numberOfRunnables + ")");
                                                progCounter += 1;
                                        }
                                        return MatrixUtils.createRealMatrix(resultArrayMatrix);
                                }

                        });
                } catch (ExecutionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }

                return result;
        }

        private RealMatrix calculateKernelVector(final double[][] training, final double[] test, KernelFunction kernelFunction) {
                double[] result = new double[training.length];

                for (int r = 0; r < training.length; r++) {
                        result[r] = kernelFunction.calculate(training[r], test);
                }
                return MatrixUtils.createColumnRealMatrix(result);
        }

        private class CalculateKernelValuesRunnable implements Runnable {

                private int m_trainingStart;
                private int m_trainingEnd;
                private int m_testStart;
                private int m_testEnd;
                private double[][] m_training;
                private double[][] m_test;
                private double[][] m_result;
                private KernelFunction m_kernelFunction;
                private Semaphore m_semaphore;

                public CalculateKernelValuesRunnable(int trainingStart, int trainingEnd, int testStart, int testEnd, double[][] training,
                                double[][] test, double[][] result, KernelFunction kernelFunction, Semaphore semaphore) {
                        m_trainingStart = trainingStart;
                        m_trainingEnd = trainingEnd;
                        m_testStart = testStart;
                        m_testEnd = testEnd;
                        m_training = training;
                        m_test = test;
                        m_result = result;
                        m_kernelFunction = kernelFunction;
                        m_semaphore = semaphore;
                }

                @Override
                public void run() {
                        for (int r = m_trainingStart; r < m_trainingEnd; r++) {
                                for (int c = m_testStart; c < m_testEnd; c++) {
                                        m_result[r][c] = m_kernelFunction.calculate(m_training[r], m_test[c]);
                                }
                        }
                        m_semaphore.release();

                }

        }

        public RealMatrix calculateKernelMatrix_singleThread(double[][] training, double[][] test) {
                double[][] result = new double[training.length][test.length];

                for (int s1 = 0; s1 < training.length; s1++) {
                        for (int s2 = 0; s2 < test.length; s2++) {
                                result[s1][s2] = m_kernelFunction.calculate(training[s1], test[s2]);
                        }
                }

                return MatrixUtils.createRealMatrix(result);
        }

        //        private RealMatrix calculateKernelMatrix(double[][] training, BufferedDataTable test) {
        //                final RealMatrix kernelMatrix = MatrixUtils.createRealMatrix(training.length, test.getRowCount());
        //
        //                for (int r = 0; r < training.length; r++) {
        //                        Iterator<DataRow> testIterator = test.iterator();
        //                        for (int c = 0; c < test.getRowCount(); c++) {
        //                                kernelMatrix.setEntry(r, c, m_kernelFunction.calculate(training[r], testIterator.next()));
        //                        }
        //                }
        //
        //                return kernelMatrix;
        //        }

        public static double[][] readBufferedDataTable(final BufferedDataTable table) {
                final double[][] data = new double[table.getRowCount()][table.getDataTableSpec().getNumColumns()];

                final Iterator<DataRow> iterator = table.iterator();

                for (int r = 0; iterator.hasNext(); r++) {
                        DataRow next = iterator.next();
                        for (int c = 0; c < table.getDataTableSpec().getNumColumns(); c++) {
                                DataCell cell = next.getCell(c);
                                if (cell.isMissing()) {
                                        throw new IllegalArgumentException("Missing values are not supported.");
                                } else if (!cell.getType().isCompatible(DoubleValue.class)) {
                                        throw new IllegalArgumentException("Only numerical data types are currently supported.");
                                } else {
                                        data[r][c] = ((DoubleValue) cell).getDoubleValue();
                                }
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

        @Override
        public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + m_colCount;
                result = prime * result + ((m_kernelFunction == null) ? 0 : m_kernelFunction.hashCode());
                result = prime * result + m_rowCount;
                result = prime * result + Arrays.hashCode(m_trainingData);
                return result;
        }

        @Override
        public boolean equals(Object obj) {
                if (this == obj) {
                        return true;
                }
                if (obj == null) {
                        return false;
                }
                if (!(obj instanceof KernelCalculator)) {
                        return false;
                }
                KernelCalculator other = (KernelCalculator) obj;
                if (m_colCount != other.m_colCount) {
                        return false;
                }
                if (m_kernelFunction == null) {
                        if (other.m_kernelFunction != null) {
                                return false;
                        }
                } else if (!m_kernelFunction.equals(other.m_kernelFunction)) {
                        return false;
                }
                if (m_rowCount != other.m_rowCount) {
                        return false;
                }
                if (!Arrays.deepEquals(m_trainingData, other.m_trainingData)) {
                        return false;
                }
                return true;
        }

        @Override
        public String toString() {
                final int maxLen = 10;
                return "KernelCalculator [m_trainingData="
                                + (m_trainingData != null ? Arrays.asList(m_trainingData).subList(0, Math.min(m_trainingData.length, maxLen)) : null)
                                + ", m_rowCount=" + m_rowCount + ", m_colCount=" + m_colCount + ", m_kernelFunction=" + m_kernelFunction + "]";
        }
}
