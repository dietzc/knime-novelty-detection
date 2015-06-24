package org.knime.knip.noveltydetection.knfst.alternative;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class MatrixFunctions {

        public static RealVector columnMeans(final RealMatrix matrix) {
                final double[] columnMeans = new double[matrix.getColumnDimension()];
                for (int c = 0; c < matrix.getColumnDimension(); c++) {
                        final double[] column = matrix.getColumn(c);
                        double sum = 0;
                        for (int r = 0; r < column.length; r++) {
                                sum += column[r];
                        }
                        columnMeans[c] = sum / column.length;
                }

                return MatrixUtils.createRealVector(columnMeans);
        }

        public static double mean(final RealMatrix matrix) {
                double mean = 0;
                final double[] columnMeans = columnMeans(matrix).toArray();
                for (double c : columnMeans)
                        mean += c;
                mean /= columnMeans.length;
                return mean;
        }

        public static RealMatrix ones(final int rowCount, final int columnCount) {
                final RealMatrix ones = MatrixUtils.createRealMatrix(rowCount, columnCount);
                return ones.scalarAdd(1);
        }

        public static RealMatrix nullspace(RealMatrix matrix) {
                SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
                int rank = svd.getRank();
                RealMatrix V = svd.getV();
                double[][] basisData = new double[V.getRowDimension()][V.getColumnDimension()];
                V.copySubMatrix(0, V.getRowDimension() - 1, rank, V.getColumnDimension() - 1, basisData);
                return MatrixUtils.createRealMatrix(basisData);
        }

        public static double[] abs(double[] array) {
                double[] abs = new double[array.length];
                for (int i = 0; i < array.length; i++)
                        abs[i] = Math.abs(array[i]);
                return abs;
        }

        public static int argmin(double[] array) {
                int index = 0;
                for (int i = 0; i < array.length; i++) {
                        if (array[i] < array[index]) {
                                index = i;
                        }
                }
                return index;
        }
}
