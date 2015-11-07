package org.knime.knip.noveltydetection.kernel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class HIKKernel implements KernelFunction {

        //        public double calculate(final DataRow sample1, final DataRow sample2) {
        //                double hik = 0.0;
        //
        //                final Iterator<DataCell> featureCellIterator1 = sample1.iterator();
        //
        //                for (DataCell featureCell2 : sample2) {
        //                        final double feature1 = ((DoubleValue) featureCellIterator1.next()).getDoubleValue();
        //                        final double feature2 = ((DoubleValue) featureCell2).getDoubleValue();
        //
        //                        hik += (feature1 < feature2) ? feature1 : feature2;
        //                }
        //
        //                return hik;
        //        }
        //
        //        @Override
        //        public double calculate(final double[] sample1, final DataRow sample2) {
        //                double hik = 0.0;
        //
        //                int featureIdx1 = 0;
        //
        //                for (DataCell featureCell2 : sample2) {
        //                        final double feature1 = sample1[featureIdx1++];
        //                        final double feature2 = ((DoubleValue) featureCell2).getDoubleValue();
        //
        //                        hik += (feature1 < feature2) ? feature1 : feature2;
        //                }
        //
        //                return hik;
        //        }

        @Override
        public double calculate(final double[] sample1, final double[] sample2) {
                if (sample1.length != sample2.length) {
                        throw new IllegalArgumentException("The arrays (vectors) must be of the same length.");
                }

                double hik = 0.0;

                for (int i = 0; i < sample1.length; i++) {
                        hik += Math.min(sample1[i], sample2[i]);
                }
                return hik;
        }

        /******* Externalizable methods ******************/
        @Override
        public void readExternal(ObjectInput arg0) throws IOException, ClassNotFoundException {
                // nothing to do here
        }

        @Override
        public void writeExternal(ObjectOutput arg0) throws IOException {
                // nothing to do here
        }

        @Override
        public String toString() {
                return "HIKKernel []";
        }

        public boolean equals(Object object) {
                if (object == null) {
                        return false;
                }
                if (object instanceof HIKKernel) {
                        return true;
                }
                return false;
        }

        @Override
        public int numParameters() {
                return 0;
        }

        @Override
        public double getParameter(int index) {
                throw new IndexOutOfBoundsException("This kernel has no parameters");
        }
}
