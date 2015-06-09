package org.knime.knip.noveltydetection.knfst;

import org.knime.core.data.DataRow;

public class EXPHIKKernel extends KernelFunction {

        @Override
        public double calculate(DataRow sample1, DataRow sample2) {
                HIKKernel hik = new HIKKernel();

                return Math.exp(hik.calculate(sample1, sample2) - hik.calculate(sample1, sample1) - hik.calculate(sample2, sample2));
        }

}
