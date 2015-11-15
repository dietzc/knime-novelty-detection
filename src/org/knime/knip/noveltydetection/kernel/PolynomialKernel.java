package org.knime.knip.noveltydetection.kernel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class PolynomialKernel implements KernelFunction {

        private double m_gamma;
        private double m_bias;
        private double m_power;

        // Framework constructor for loading
        // do not use for anything else!
        public PolynomialKernel() {
                // TODO Auto-generated constructor stub
        }

        public PolynomialKernel(double gamma, double bias, double power) {
                m_gamma = gamma;
                m_bias = bias;
                m_power = power;
        }

        @Override
        public void readExternal(ObjectInput arg0) throws IOException, ClassNotFoundException {
                m_gamma = arg0.readDouble();
                m_bias = arg0.readDouble();
                m_power = arg0.readDouble();
        }

        @Override
        public void writeExternal(ObjectOutput arg0) throws IOException {
                arg0.writeDouble(m_gamma);
                arg0.writeDouble(m_bias);
                arg0.writeDouble(m_power);
        }

        @Override
        public double calculate(double[] sample1, double[] sample2) {
                if (sample1.length != sample2.length) {
                        throw new IllegalArgumentException("The arrays (vectors) must be of the same length.");
                }

                double result = 0;
                for (int i = 0; i < sample1.length; ++i) {
                        double oldresult = result;
                        result = oldresult + sample1[i] * sample2[i];
                }
                result = m_gamma * result;
                result = result + m_bias;
                return Math.pow(result, m_power);
        }

        @Override
        public int numParameters() {
                return 3;
        }

        @Override
        public double getParameter(int index) {
                if (index < 0 || index > 2) {
                        throw new IndexOutOfBoundsException();
                }

                if (index == 0) {
                        return m_gamma;
                } else if (index == 1) {
                        return m_bias;
                } else {
                        return m_power;
                }
        }

        @Override
        public int hashCode() {
                final int prime = 31;
                int result = 1;
                long temp;
                temp = Double.doubleToLongBits(m_bias);
                result = prime * result + (int) (temp ^ (temp >>> 32));
                temp = Double.doubleToLongBits(m_gamma);
                result = prime * result + (int) (temp ^ (temp >>> 32));
                temp = Double.doubleToLongBits(m_power);
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
                if (!(obj instanceof PolynomialKernel)) {
                        return false;
                }
                PolynomialKernel other = (PolynomialKernel) obj;
                if (Double.doubleToLongBits(m_bias) != Double.doubleToLongBits(other.m_bias)) {
                        return false;
                }
                if (Double.doubleToLongBits(m_gamma) != Double.doubleToLongBits(other.m_gamma)) {
                        return false;
                }
                if (Double.doubleToLongBits(m_power) != Double.doubleToLongBits(other.m_power)) {
                        return false;
                }
                return true;
        }

        @Override
        public String toString() {
                return "PolynomialKernel [m_gamma=" + m_gamma + ", m_bias=" + m_bias + ", m_power=" + m_power + "]";
        }

}
