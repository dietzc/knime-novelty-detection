package org.knime.knip.noveltydetection.nodes.localnoveltyscorer;

public class Tools {

        public Tools() {
                // TODO Auto-generated constructor stub
        }

        public static double getMin(double[] array) {
                if (array.length == 0) {
                        throw new IllegalArgumentException("Array must contain at least one element!");
                }

                double min = array[0];

                for (double d : array) {
                        if (d < min) {
                                min = d;
                        }
                }
                return min;
        }
}
