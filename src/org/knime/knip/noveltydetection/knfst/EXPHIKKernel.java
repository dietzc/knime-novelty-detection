package org.knime.knip.noveltydetection.knfst;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.knime.core.data.DataRow;

public class EXPHIKKernel implements KernelFunction {

        @Override
        public double calculate(DataRow sample1, DataRow sample2) {
                HIKKernel hik = new HIKKernel();

                return Math.exp(hik.calculate(sample1, sample2) - hik.calculate(sample1, sample1) - hik.calculate(sample2, sample2));
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
                // nothing to do here

        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
                // nothing to do here

        }

}
