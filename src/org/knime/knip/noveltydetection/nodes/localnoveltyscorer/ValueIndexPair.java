package org.knime.knip.noveltydetection.nodes.localnoveltyscorer;

import java.util.Comparator;
import java.util.PriorityQueue;

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

        public static ValueIndexPair[] transformArray2ValueIndexPairArray(double[] array) {
                ValueIndexPair[] result = new ValueIndexPair[array.length];

                for (int i = 0; i < array.length; i++) {
                        result[i] = new ValueIndexPair(array[i], i);
                }
                return result;
        }

        public static ValueIndexPair[] getK(ValueIndexPair[] array, int k, Comparator<ValueIndexPair> comparator) {
                if (k < 1) {
                        throw new IllegalArgumentException("k must be greater than zero!");
                }
                if (k > array.length) {
                        throw new IllegalArgumentException("k must be smaller or equal to the length of the array!");
                }

                PriorityQueue<ValueIndexPair> heap = new PriorityQueue<ValueIndexPair>(comparator);

                for (int i = 0; i < array.length; i++) {
                        if (i < k) {
                                heap.add(array[i]);
                        } else {
                                if (array[i].getValue() < heap.peek().getValue()) {
                                        heap.poll();
                                        heap.add(array[i]);
                                }
                        }

                }

                return heap.toArray(new ValueIndexPair[heap.size()]);

        }

}
