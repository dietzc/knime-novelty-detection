package org.knime.knip.noveltydetection.knfst.alternative;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class Test {

        static void test_parallelKernelCalculation() {
                double[][] training = new double[100][100];
                double[][] test = new double[100][100];

                for (int r = 0; r < training.length; r++) {
                        for (int c = 0; c < training.length; c++) {
                                training[r][c] = Math.random();
                                test[r][c] = Math.random();
                        }
                }

                KernelCalculator kernelCalculator = new KernelCalculator(new HIKKernel());

                RealMatrix multithread = kernelCalculator.calculateKernelMatrix(training, test);
                RealMatrix singlethread = kernelCalculator.calculateKernelMatrix_singleThread(training, test);

                boolean equal = multithread.equals(singlethread);
                System.out.println(equal);
        }

        public static void main(String[] args) {
                /*
                double[] elements = {1, 1, 1, 2, 2, 2, 3, 3, 3};
                DoubleMatrix matrix = new DoubleMatrix(3, 3, elements);
                printMatrix(matrix);
                DoubleMatrix[] svd = Singular.fullSVD(matrix);
                printMatrix(svd[0]);
                printMatrix(svd[1]);
                printMatrix(svd[2]);
                

                double[][] elements = { {2, 3, 3, 4, 6, 8, 8, 10, 10}, {3, 5, 4, 6, 9, 10, 9, 13, 12}, {3, 4, 5, 6, 9, 14, 15, 17, 18},
                                {4, 6, 6, 8, 12, 16, 16, 20, 20}, {6, 8, 9, 12, 18, 24, 24, 30, 30}, {8, 10, 14, 16, 24, 40, 44, 48, 52},
                                {8, 9, 15, 16, 24, 44, 50, 52, 58}, {10, 13, 17, 20, 30, 48, 52, 58, 62}, {10, 12, 18, 20, 30, 52, 58, 62, 68}};
                String[] labels = {"1", "1", "1", "1", "1", "0", "0", "0", "0"};

                RealMatrix kOriginal = MatrixUtils.createRealMatrix(elements);
                RealMatrix kernelMatrix = MatrixFunctions.pow(kOriginal, 20);

                double[][] training = { {1, 2, 3}, {2, 3, 4}, {12, 235, 13}, {12, 234, 13}, {222, 1, 213}};
                String[] labelsTraining = {"1", "1", "2", "2", "3"};

                KernelFunction kernel = new EXPHIKKernel();
                KernelCalculator kernelCalc = new KernelCalculator(training, kernel);

                KNFST oneclass = new OneClassKNFST(kernelCalc);
                KNFST multiclass = new MultiClassKNFST(kernelCalc, labelsTraining);

                double[][] test = { {123, 245, 12}, {3455, 33, 90}};
                NoveltyScores novScoresOneClass = oneclass.scoreTestData(test);
                NoveltyScores novScoresMultiClass = multiclass.scoreTestData(test);
                double[] scoreOneclass = novScoresOneClass.getScores();
                double[] scoreMulticlass = novScoresMultiClass.getScores();

                for (int i = 0; i < scoreOneclass.length; i++) {
                        System.out.println("Test sample " + i + ":");
                        System.out.println("oneClass: " + scoreOneclass[i]);
                        System.out.println("multiClass: " + scoreMulticlass[i]);
                }

                System.out.println("done");
                /*
                                RealMatrix proj = KNFST.projection(kernelMatrix, labels);

                                printMatrix(proj);

                                for (int i = 0; i < 9; i++) {
                                        RealMatrix x = kernelMatrix.getRowMatrix(i);
                                        RealMatrix t = x.multiply(proj);

                                        System.out.println("t" + i + ":");
                                        printMatrix(t);
                                }

                                double[][] testElements = { {13}, {21}, {18}, {26}, {39}, {46}, {43}, {59}, {56}};
                                RealMatrix test = MatrixFunctions.pow(MatrixUtils.createRealMatrix(testElements), 20);

                                RealMatrix projectionVector = test.transpose().multiply(proj);
                                System.out.println("Projection of test sample:");
                                printMatrix(projectionVector);
                */

                double[][] elements = { {1, 2, 3, 4}, {1, 2, 3, 4}, {1, 2, 3, 4}, {1, 2, 3, 4}};
                RealMatrix matrix = MatrixUtils.createRealMatrix(elements);
        }

        public static void printMatrix(RealMatrix matrix) {
                System.out.print("(");
                double[][] data = matrix.getData();
                for (int r = 0; r < matrix.getRowDimension(); r++) {
                        for (int c = 0; c < matrix.getColumnDimension(); c++)
                                System.out.print((c == matrix.getColumnDimension() - 1) ? data[r][c] : data[r][c] + " ");
                        String end = (r == matrix.getRowDimension() - 1) ? ")\n" : "\n";
                        System.out.println(end);
                }
        }

        public static boolean matricesAreEqual(RealMatrix matrix1, RealMatrix matrix2) {
                if (matrix1.getRowDimension() != matrix2.getRowDimension() || matrix1.getColumnDimension() != matrix2.getColumnDimension()) {
                        return false;
                }

                for (int r = 0; r < matrix1.getRowDimension(); r++) {
                        for (int c = 0; c < matrix1.getColumnDimension(); c++) {
                                if (Math.abs(matrix1.getEntry(r, c) - matrix2.getEntry(r, c)) > 1e-12) {
                                        return false;
                                }
                        }
                }
                return true;
        }
}
