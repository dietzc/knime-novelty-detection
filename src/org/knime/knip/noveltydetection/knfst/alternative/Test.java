package org.knime.knip.noveltydetection.knfst.alternative;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

public class Test {

        public static void main(String[] args) {
                /*
                double[] elements = {1, 1, 1, 2, 2, 2, 3, 3, 3};
                DoubleMatrix matrix = new DoubleMatrix(3, 3, elements);
                printMatrix(matrix);
                DoubleMatrix[] svd = Singular.fullSVD(matrix);
                printMatrix(svd[0]);
                printMatrix(svd[1]);
                printMatrix(svd[2]);
                */

                double[] elements = {2, 3, 3, 4, 6, 8, 8, 10, 10, 3, 5, 4, 6, 9, 10, 9, 13, 12, 3, 4, 5, 6, 9, 14, 15, 17, 18, 4, 6, 6, 8, 12, 16,
                                16, 20, 20, 6, 8, 9, 12, 18, 24, 24, 30, 30, 8, 10, 14, 16, 24, 40, 44, 48, 52, 8, 9, 15, 16, 24, 44, 50, 52, 58, 10,
                                13, 17, 20, 30, 48, 52, 58, 62, 10, 12, 18, 20, 30, 52, 58, 62, 68};
                String[] labels = {"1", "1", "1", "1", "1", "0", "0", "0", "0"};

                DoubleMatrix kOriginal = new DoubleMatrix(9, 9, elements);
                DoubleMatrix kernelMatrix = MatrixFunctions.pow(kOriginal, 20);

                double[][] training = { {1, 2, 3}, {2, 3, 4}, {12, 235, 13}};
                String[] labelsTraining = {"1", "1", "2"};

                KernelFunction kernel = new HIKKernel();
                KernelCalculator kernelCalc = new KernelCalculator(training, kernel);

                KNFST oneclass = new OneClassKNFST(kernelCalc);
                KNFST multyclass = new MultiClassKNFST(kernelCalc, labelsTraining);

                System.out.println("done");

                /*
                 * DoubleMatrix proj = KNFST.projection(kernelMatrix, labels);
                 

                printMatrix(proj);

                for (int i = 0; i < 9; i++) {
                        DoubleMatrix x = kernelMatrix.getRow(i);
                        DoubleMatrix t = x.mmul(proj);

                        System.out.println("t" + i + ":");
                        printMatrix(t);
                }

                double[] testElements = {13, 21, 18, 26, 39, 46, 43, 59, 56};
                DoubleMatrix test = MatrixFunctions.pow(new DoubleMatrix(9, 1, testElements), 20);

                DoubleMatrix projectionVector = test.transpose().mmul(proj);
                System.out.println("Projection of test sample:");
                printMatrix(projectionVector); */
        }

        public static void printMatrix(DoubleMatrix matrix) {
                System.out.print("(");
                for (int r = 0; r < matrix.getRows(); r++) {
                        for (int c = 0; c < matrix.getColumns(); c++)
                                System.out.print((c == matrix.getColumns() - 1) ? matrix.get(r, c) : matrix.get(r, c) + " ");
                        String end = (r == matrix.getRows() - 1) ? ")\n" : "\n";
                        System.out.println(end);
                }
        }
}
