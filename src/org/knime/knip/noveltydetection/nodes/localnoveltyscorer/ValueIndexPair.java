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
         * Output: ValueIndexPair array with k elements (NOTE: The array is NOT in order)
         */
        public static ValueIndexPair[] getK(ValueIndexPair[] array, int k, final Comparator<ValueIndexPair> comparator) {
                if (k < 1) {
                        throw new IllegalArgumentException("k must be greater than zero!");
                }
                if (k > array.length) {
                        throw new IllegalArgumentException("k must be smaller or equal to the length of the array!");
                }

                // heapComp induces the opposite ordering to comparator
                Comparator<ValueIndexPair> heapComp = new Comparator<ValueIndexPair>() {
                        public int compare(ValueIndexPair o1, ValueIndexPair o2) {
                                return -comparator.compare(o1, o2);
                        }
                };

                // heap structure to keep first k elements
                PriorityQueue<ValueIndexPair> heap = new PriorityQueue<ValueIndexPair>(k, heapComp);

                for (int i = 0; i < array.length; i++) {
                        // fill heap
                        if (i < k) {
                                heap.add(array[i]);
                        } else {
                                // check if head of heap is larger than new element
                                if (comparator.compare(array[i], heap.peek()) == -1) {
                                        // remove head
                                        heap.poll();
                                        // add new element and restore heap structure
                                        heap.add(array[i]);
                                }
                        }

                }

                return heap.toArray(new ValueIndexPair[heap.size()]);

        }

}
