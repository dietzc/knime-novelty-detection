package org.knime.knip.noveltydetection.knfst;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;

public class HIKKernel implements KernelFunction {

        public double calculate(DataRow sample1, DataRow sample2) {
                double hik = 0.0;

                final Iterator<DataCell> featureCellIterator1 = sample1.iterator();

                for (DataCell featureCell2 : sample2) {
                        final double feature1 = ((DoubleValue) featureCellIterator1.next()).getDoubleValue();
                        final double feature2 = ((DoubleValue) featureCell2).getDoubleValue();

                        hik += (feature1 < feature2) ? feature1 : feature2;
                }

                return hik;
        }

        @Override
        public void readExternal(ObjectInput arg0) throws IOException, ClassNotFoundException {
                // nothing to do here
        }

        @Override
        public void writeExternal(ObjectOutput arg0) throws IOException {
                // nothing to do here
        }
}
