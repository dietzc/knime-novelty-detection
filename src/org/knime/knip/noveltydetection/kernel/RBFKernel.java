package org.knime.knip.noveltydetection.kernel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class RBFKernel implements KernelFunction {

        private double m_sigma;

        // Framework constructor for loading
        // do not use for anything else!
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

        //        @Override
        //        public double calculate(DataRow sample1, DataRow sample2) {
        //
        //                double squaredEuclideanDistance = 0;
        //
        //                final Iterator<DataCell> c1 = sample1.iterator();
        //                for (DataCell c2 : sample2) {
        //                        double val1 = ((DoubleValue) c1.next()).getDoubleValue();
        //                        double val2 = ((DoubleValue) c2).getDoubleValue();
        //                        squaredEuclideanDistance += (val1 - val2) * (val1 - val2);
        //                }
        //
        //                final double rbf = Math.exp(-squaredEuclideanDistance / (2 * m_sigma * m_sigma));
        //
        //                return rbf;
        //        }
        //
        //        @Override
        //        public double calculate(double[] sample1, DataRow sample2) {
        //
        //                double squaredEuclideanDistance = 0;
        //
        //                int c1 = 0;
        //                for (DataCell c2 : sample2) {
        //                        double val1 = sample1[c1];
        //                        double val2 = ((DoubleValue) c2).getDoubleValue();
        //                        squaredEuclideanDistance += (val1 - val2) * (val1 - val2);
        //                }
        //
        //                final double rbf = Math.exp(-squaredEuclideanDistance / (2 * m_sigma * m_sigma));
        //
        //                return rbf;
        //        }

        @Override
        public double calculate(double[] sample1, double[] sample2) {

                if (sample1.length != sample2.length) {
                        throw new IllegalArgumentException("The arrays (vectors) must be of the same length.");
                }

                double result = 0;
                for (int i = 0; i < sample1.length; ++i) {
                        double dif = sample1[i] - sample2[i];
                        result = result + dif * dif;
                }
                return Math.pow(Math.E, -result / 2.0 / m_sigma / m_sigma);
        }

        @Override
        public int numParameters() {
                return 1;
        }

        @Override
        public double getParameter(int index) {
                if (index != 0) {
                        throw new IndexOutOfBoundsException();
                }
                return m_sigma;
        }

        @Override
        public int hashCode() {
                final int prime = 31;
                int result = 1;
                long temp;
                temp = Double.doubleToLongBits(m_sigma);
                result = prime * result + (int) (temp ^ (temp >>> 32));
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
                if (!(obj instanceof RBFKernel)) {
                        return false;
                }
                RBFKernel other = (RBFKernel) obj;
                if (Double.doubleToLongBits(m_sigma) != Double.doubleToLongBits(other.m_sigma)) {
                        return false;
                }
                return true;
        }

        @Override
        public String toString() {
                return "RBFKernel [m_sigma=" + m_sigma + "]";
        }

}
