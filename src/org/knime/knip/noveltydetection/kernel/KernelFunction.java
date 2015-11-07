package org.knime.knip.noveltydetection.kernel;

import java.io.Externalizable;

public interface KernelFunction extends Externalizable {

        /* Calculates the kernel value for two samples
         * Parameters:  sample1:    KNIME DataRow containing the first sample
         *              sample2:    KNIME DataRow containing the second sample
         * Output: Kernel value for the two samples
         */
        //        public double calculate(DataRow sample1, DataRow sample2);
        //
        //        public double calculate(double[] sample1, DataRow sample2);

        public double calculate(double[] sample1, double[] sample2);

        public int numParameters();

        public double getParameter(int index);

}