package org.knime.knip.noveltydetection.nodes.localnoveltyscorer;

import java.util.Comparator;
import java.util.PriorityQueue;

/*
 * A simple pair class for a double and an int value
 */
public class ValueIndexPair {

        private double m_value;
        private int m_index;

        public ValueIndexPair(double value, int index) {
                m_value = value;
                m_index = index;
        }

        public double getValue() {
                return m_value;
        }

        public int getIndex() {
                return m_index;
        }

        public boolean equals(Object object) {
                if (object instanceof ValueIndexPair && ((ValueIndexPair) object).getValue() == this.getValue()
                                && ((ValueIndexPair) object).getIndex() == this.getIndex()) {
                        return true;
                } else {
                        return false;
                }
        }

        public String toString() {
                return "(" + m_value + ", " + m_index + ")";
        }

        /*
         * Transforms the input array to an array of ValueIndexPairs
         * Parameters: array: Double array that should be converted to ValueIndexPair array
         * Output: ValueIndexPair array containing the pairs of the input array 
         */
        public static ValueIndexPair[] transformArray2ValueIndexPairArray(double[] array) {
                ValueIndexPair[] result = new ValueIndexPair[array.length];

                for (int i = 0; i < array.length; i++) {
                        result[i] = new ValueIndexPair(array[i], i);
                }
                return result;
        }

        /*
         * Gets first k elements of array k depending on the ordering induced by the comparator
         * Parameters:  array: ValueIndexPair array
         *              k : Number of elements to be selected
         *              comparator: Comparator for the ValueIndexPair class
         * Output: ValueIndexPair array with k elements
         */
        public static ValueIndexPair[] getK(ValueIndexPair[] array, int k, Comparator<ValueIndexPair> comparator) {
                if (k < 1) {
                        throw new IllegalArgumentException("k must be greater than zero!");
                }
                if (k > array.length) {
                        throw new IllegalArgumentException("k must be smaller or equal to the length of the array!");
                }

                PriorityQueue<ValueIndexPair> heap = new PriorityQueue<ValueIndexPair>(k, comparator);

                for (int i = 0; i < array.length; i++) {
                        if (i < k - 1) {
                                heap.add(array[i]);
                        } else {
                                if (comparator.compare(array[i], heap.peek()) == 1) {
                                        heap.poll();
                                        heap.add(array[i]);
                                }
                        }

                }

                return heap.toArray(new ValueIndexPair[heap.size()]);

        }

}
