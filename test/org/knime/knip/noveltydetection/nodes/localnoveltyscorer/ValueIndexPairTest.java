package org.knime.knip.noveltydetection.nodes.localnoveltyscorer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

public class ValueIndexPairTest {

        private ValueIndexPair[] testArray = new ValueIndexPair[] {new ValueIndexPair(1, 0), new ValueIndexPair(2, 1), new ValueIndexPair(3, 2),
                        new ValueIndexPair(4, 3)};
        private Comparator<ValueIndexPair> comparator = new Comparator<ValueIndexPair>() {
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

        @Test
        public void testTransformArray2ValueIndexPairArray() {
                double[] array = new double[] {1, 2, 3, 4};

                ValueIndexPair[] result = ValueIndexPair.transformArray2ValueIndexPairArray(array);
                for (int i = 0; i < result.length; i++) {
                        assertEquals(testArray[i], result[i]);
                }
        }

        @Test
        public void testGetKSimple() {
                ValueIndexPair[] expected = new ValueIndexPair[] {new ValueIndexPair(1, 0), new ValueIndexPair(2, 1)};
                ArrayList<ValueIndexPair> result = new ArrayList<ValueIndexPair>(Arrays.asList(ValueIndexPair.getK(testArray, 2, comparator)));
                for (int i = 0; i < expected.length; i++) {
                        assertTrue(result.contains(expected[i]));
                }
        }

        @Test
        public void testGetKRandomMinima() {
                double[] array = new double[100];
                for (int i = 0; i < array.length; i++) {
                        array[i] = Math.random();
                }
                ValueIndexPair[] valIndexArray = ValueIndexPair.transformArray2ValueIndexPairArray(array);
                ArrayList<ValueIndexPair> expectedFullList = new ArrayList<ValueIndexPair>(Arrays.asList(valIndexArray));

                expectedFullList.sort(comparator);
                List<ValueIndexPair> expected = expectedFullList.subList(0, 19);

                ArrayList<ValueIndexPair> result = new ArrayList<ValueIndexPair>(Arrays.asList(ValueIndexPair.getK(valIndexArray, 20, comparator)));

                for (ValueIndexPair pair : expected) {
                        assertTrue(result.contains(pair));
                }
        }

        @Test
        public void testGetKRandomMaxima() {
                double[] array = new double[100];
                for (int i = 0; i < array.length; i++) {
                        array[i] = Math.random();
                }
                ValueIndexPair[] valIndexArray = ValueIndexPair.transformArray2ValueIndexPairArray(array);
                ArrayList<ValueIndexPair> expectedFullList = new ArrayList<ValueIndexPair>(Arrays.asList(valIndexArray));

                Comparator<ValueIndexPair> comparator = new Comparator<ValueIndexPair>() {
                        public int compare(ValueIndexPair o1, ValueIndexPair o2) {
                                if (o1.getValue() < o2.getValue()) {
                                        return 1;
                                } else if (o1.getValue() > o2.getValue()) {
                                        return -1;
                                } else {
                                        return 0;
                                }
                        }
                };

                expectedFullList.sort(comparator);
                List<ValueIndexPair> expected = expectedFullList.subList(0, 19);

                ArrayList<ValueIndexPair> result = new ArrayList<ValueIndexPair>(Arrays.asList(ValueIndexPair.getK(valIndexArray, 20, comparator)));

                for (ValueIndexPair pair : expected) {
                        assertTrue(result.contains(pair));
                }
        }

        @Test(expected = IllegalArgumentException.class)
        public void testExceptionKTooSmall() {
                ValueIndexPair[] expected = new ValueIndexPair[] {new ValueIndexPair(1, 0), new ValueIndexPair(2, 1)};
                ArrayList<ValueIndexPair> result = new ArrayList<ValueIndexPair>(Arrays.asList(ValueIndexPair.getK(testArray, 0, comparator)));
        }

        @Test(expected = IllegalArgumentException.class)
        public void testExceptionKTooLarge() {
                ValueIndexPair[] expected = new ValueIndexPair[] {new ValueIndexPair(1, 0), new ValueIndexPair(2, 1)};
                ArrayList<ValueIndexPair> result = new ArrayList<ValueIndexPair>(Arrays.asList(ValueIndexPair.getK(testArray, 230, comparator)));
        }
}
