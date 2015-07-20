package org.knime.knip.noveltydetection.knfst.alternative;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;

public class RBFKernel implements KernelFunction {

        private double m_sigma;

        // Default constructor for Externalizable Interface
        public RBFKernel() {

        }

        public RBFKernel(double sigma) {
                m_sigma = sigma;
        }

        @Override
        public void readExternal(ObjectInput arg0) throws IOException, ClassNotFoundException {
                // read sigma
                m_sigma = arg0.readDouble();

        }

        @Override
        public void writeExternal(ObjectOutput arg0) throws IOException {
                // write sigma
                arg0.writeDouble(m_sigma);

        }

        @Override
        public double calculate(DataRow sample1, DataRow sample2) {

                double squaredEuclideanDistance = 0;

                final Iterator<DataCell> c1 = sample1.iterator();
                for (DataCell c2 : sample2) {
                        double val1 = ((DoubleValue) c1.next()).getDoubleValue();
                        double val2 = ((DoubleValue) c2).getDoubleValue();
                        squaredEuclideanDistance += (val1 - val2) * (val1 - val2);
                }

                final double rbf = Math.exp(-squaredEuclideanDistance / (2 * m_sigma * m_sigma));

                return rbf;
        }

        @Override
        public double calculate(double[] sample1, DataRow sample2) {

                double squaredEuclideanDistance = 0;

                int c1 = 0;
                for (DataCell c2 : sample2) {
                        double val1 = sample1[c1];
                        double val2 = ((DoubleValue) c2).getDoubleValue();
                        squaredEuclideanDistance += (val1 - val2) * (val1 - val2);
                }

                final double rbf = Math.exp(-squaredEuclideanDistance / (2 * m_sigma * m_sigma));

                return rbf;
        }

        @Override
        public double calculate(double[] sample1, double[] sample2) {

                double squaredEuclideanDistance = 0;

                int c1 = 0;
                for (double c2 : sample2) {
                        double val1 = sample1[c1];
                        double val2 = c2;
                        squaredEuclideanDistance += (val1 - val2) * (val1 - val2);
                }

                final double rbf = Math.exp(-squaredEuclideanDistance / (2 * m_sigma * m_sigma));

                return rbf;
        }

}
