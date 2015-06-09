package org.knime.knip.noveltydetection.knfst;

import org.knime.core.data.DataRow;

public abstract class KernelFunction {

        /* Calculates the kernel value for two samples
         * Parameters:  sample1:    KNIME DataRow containing the first sample
         *              sample2:    KNIME DataRow containing the second sample
         * Output: Kernel value for the two samples
         */
        abstract public double calculate(DataRow sample1, DataRow sample2);
}
