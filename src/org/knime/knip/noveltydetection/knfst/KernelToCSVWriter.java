package org.knime.knip.noveltydetection.knfst;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/*
 * This class is only used to provide Kernel values for testing the knfst projection against
 * the Matlab implementation of the paper
 */
public class KernelToCSVWriter {

        public static ArrayList<String[]> readCSVfile(String path, String separator) {
                BufferedReader reader = null;
                String line = "";
                ArrayList<String[]> result = new ArrayList<String[]>();

                try {
                        reader = new BufferedReader(new FileReader(path));

                        while ((line = reader.readLine()) != null) {
                                result.add(line.split(separator));
                        }
                        reader.close();

                } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }

                return result;
        }

        public static void writeCSVFile(String[][] table, String path, String separator) {
                try {
                        FileWriter writer = new FileWriter(path);

                        for (String[] row : table) {
                                for (int c = 0; c < row.length; c++) {
                                        writer.append(row[c]);
                                        if (c == row.length - 1) {
                                                writer.append('\n');
                                        } else {
                                                writer.append(separator);
                                        }
                                }
                        }

                        writer.flush();
                        writer.close();

                } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
        }

        public static void main(String[] args) {
                String pathTraining = "C:\\Informatik-Studium\\6. Semester\\Bachelor Projekt\\Data\\trainingData.csv";
                String pathTest = "C:\\Informatik-Studium\\6. Semester\\Bachelor Projekt\\Data\\testData.csv";
                String separator = ",";

                ArrayList<String[]> inputTraining = readCSVfile(pathTraining, separator);
                System.out.println("training is successfully read");
                double[][] trainingData = new double[inputTraining.size()][inputTraining.get(0).length - 1];
                int rowIterator = 0;
                for (String[] row : inputTraining) {
                        for (int c = 1; c < row.length; c++) {
                                trainingData[rowIterator][c - 1] = Double.parseDouble(row[c]);
                        }
                        rowIterator++;
                }

                KernelCalculator kernelCalculator = new KernelCalculator(trainingData, new HIKKernel());

                double[][] trainingKernel = kernelCalculator.kernelize().getData();
                String[][] trainingKernelString = new String[trainingKernel.length][trainingKernel[0].length];
                for (int r = 0; r < trainingKernel.length; r++) {
                        for (int c = 0; c < trainingKernel[r].length; c++) {
                                trainingKernelString[r][c] = Double.toString(trainingKernel[r][c]);
                        }
                }

                //writeCSVFile(trainingKernelString, "C:\\Informatik-Studium\\6. Semester\\Bachelor Projekt\\Data\\trainingKernel.csv", separator);
                System.out.println("TrainingKernel is successfully written.");

                ArrayList<String[]> inputTest = readCSVfile(pathTest, separator);
                System.out.println("Test is successfully read.");
                double[][] testData = new double[inputTest.size()][inputTest.get(0).length - 1];
                rowIterator = 0;
                for (String[] row : inputTest) {
                        for (int c = 1; c < row.length; c++) {
                                testData[rowIterator][c - 1] = Double.parseDouble(row[c]);
                        }
                        rowIterator++;
                }

                double[][] testKernel = kernelCalculator.kernelize(testData).getData();
                String[][] testKernelString = new String[testKernel.length][testKernel[0].length];
                for (int r = 0; r < testKernel.length; r++) {
                        for (int c = 0; c < testKernel[r].length; c++) {
                                testKernelString[r][c] = Double.toString(testKernel[r][c]);
                        }
                }

                writeCSVFile(testKernelString, "C:\\Informatik-Studium\\6. Semester\\Bachelor Projekt\\Data\\testKernel.csv", separator);
                System.out.println("Test is successfully written.");
        }
}
