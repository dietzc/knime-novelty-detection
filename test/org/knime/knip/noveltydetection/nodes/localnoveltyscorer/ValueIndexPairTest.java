package org.knime.knip.noveltydetection.nodes.localnoveltyscorer;

import static org.junit.Assert.assertEquals;

import java.util.Comparator;

import org.junit.Test;

public class ValueIndexPairTest {

        private ValueIndexPair[] testArray = new ValueIndexPair[] {new ValueIndexPair(1, 0), new ValueIndexPair(2, 1), new ValueIndexPair(3, 2),
                        new ValueIndexPair(4, 3)};

        @Test
        public void testTransformArray2ValueIndexPairArray() {
                double[] array = new double[] {1, 2, 3, 4};

                ValueIndexPair[] result = ValueIndexPair.transformArray2ValueIndexPairArray(array);
                for (int i = 0; i < result.length; i++) {
                        assertEquals(testArray[i], result[i]);
                }
        }

        @Test
        public void testGetK() {
                ValueIndexPair[] expected = new ValueIndexPair[] {new ValueIndexPair(1, 0), new ValueIndexPair(2, 1)};
                Comparator<ValueIndexPair> comparator = new Comparator<ValueIndexPair>() {
                        public int compare(ValueIndexPair o1, ValueIndexPair o2) {
                                if (o1.getValue() < o2.getValue()) {
                                        return -1;
                                } else if (o1.getValue() > o2.getValue()) {
                                        return 1;
                                } else {
                                        return 0;
                                }
                        }
                };
                ValueIndexPair[] result = ValueIndexPair.getK(testArray, 2, comparator);
                for (int i = 0; i < result.length; i++) {
                        assertEquals(expected[i], result[i]);
                }
        }
}
