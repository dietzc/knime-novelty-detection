package org.knime.knip.noveltydetection.knfst.alternative;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

public class MatrixFunctionsTest {

        RealMatrix matrixA = MatrixUtils.createRealMatrix(new double[][] { {1, 2, 3}, {1, 2, 3}, {1, 2, 3}});
        RealMatrix matrixI = MatrixUtils.createRealIdentityMatrix(3);
        double[] testArray = new double[] {-1, 1, 0, -2000};

        @Test
        public void testColumnMeans() {
                RealVector expected = MatrixUtils.createRealVector(new double[] {1, 2, 3});
                assertEquals(expected, MatrixFunctions.columnMeans(matrixA));
        }

        @Test
        public void testMean() {
                double expected = 2;
                assertEquals(expected, MatrixFunctions.mean(matrixA), 0);
        }

        @Test
        public void testOnes() {
                RealMatrix expected1 = MatrixUtils.createRealMatrix(new double[][] { {1, 1, 1}, {1, 1, 1}, {1, 1, 1}});
                assertEquals(expected1, MatrixFunctions.ones(3, 3));
                RealMatrix expected2 = MatrixUtils.createRealMatrix(new double[][] { {1, 1}, {1, 1}, {1, 1}});
                assertEquals(expected2, MatrixFunctions.ones(3, 2));
                RealMatrix expected3 = MatrixUtils.createRealMatrix(new double[][] { {1, 1, 1}, {1, 1, 1}});
                assertEquals(expected3, MatrixFunctions.ones(2, 3));
        }

        @Test
        public void testNullspace() {
                double expected = 0;
                RealMatrix nullspace = MatrixFunctions.nullspace(matrixA);
                RealMatrix result = matrixA.multiply(nullspace);
                double[][] resultData = result.getData();
                for (double[] row : resultData) {
                        for (double cell : row) {
                                assertEquals(expected, cell, 1e-12);
                        }
                }

                assertNull(MatrixFunctions.nullspace(matrixI));
        }

        @Test
        public void testAbs() {
                double[] expected = new double[] {1, 1, 0, 2000};
                assertArrayEquals(expected, MatrixFunctions.abs(testArray), 0);
        }

        @Test
        public void testArgmin() {
                int expected = 3;
                assertEquals(expected, MatrixFunctions.argmin(testArray));
        }

        @Test
        public void testRowMins() {
                RealVector expected = MatrixUtils.createRealVector(new double[] {1, 1, 1});
                assertEquals(expected, MatrixFunctions.rowMins(matrixA));
        }

        @Test
        public void testSqrtMatrix() {

        }

}
