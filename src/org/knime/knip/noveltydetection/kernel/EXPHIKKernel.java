package org.knime.knip.noveltydetection.kernel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class EXPHIKKernel implements KernelFunction {

        //        @Override
        //        public double calculate(final DataRow sample1, final DataRow sample2) {
        //                final HIKKernel hik = new HIKKernel();
        //
        //                return Math.exp(2 * hik.calculate(sample1, sample2) - hik.calculate(sample1, sample1) - hik.calculate(sample2, sample2));
        //        }
        //
        //        @Override
        //        public double calculate(final double[] sample1, final DataRow sample2) {
        //                final HIKKernel hik = new HIKKernel();
        //
        //                return Math.exp(2 * hik.calculate(sample1, sample2) - hik.calculate(sample1, sample1) - hik.calculate(sample2, sample2));
        //        }

        @Override
        public double calculate(final double[] sample1, final double[] sample2) {
                if (sample1.length != sample2.length) {
                        throw new IllegalArgumentException("The arrays (vectors) must be of the same length.");
                }
                final HIKKernel hik = new HIKKernel();

                return Math.exp(2 * hik.calculate(sample1, sample2) - hik.calculate(sample1, sample1) - hik.calculate(sample2, sample2));
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
                // nothing to do here

        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
                // nothing to do here

        }

        @Override
        public String toString() {
                return "EXPHIKKernel []";
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
