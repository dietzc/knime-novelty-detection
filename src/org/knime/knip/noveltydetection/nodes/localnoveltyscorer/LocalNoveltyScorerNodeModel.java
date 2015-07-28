/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * --------------------------------------------------------------------- *
 *
 */
package org.knime.knip.noveltydetection.nodes.localnoveltyscorer;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.imglib2.type.numeric.RealType;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.NominalValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.BufferedDataTableHolder;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.knip.noveltydetection.knfst.alternative.EXPHIKKernel;
import org.knime.knip.noveltydetection.knfst.alternative.HIKKernel;
import org.knime.knip.noveltydetection.knfst.alternative.KNFST;
import org.knime.knip.noveltydetection.knfst.alternative.KernelCalculator;
import org.knime.knip.noveltydetection.knfst.alternative.KernelFunction;
import org.knime.knip.noveltydetection.knfst.alternative.MultiClassKNFST;
import org.knime.knip.noveltydetection.knfst.alternative.OneClassKNFST;
import org.knime.knip.noveltydetection.knfst.alternative.RBFKernel;

/**
 * Crop BitMasks or parts of images according to a Labeling
 *
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael
 *         Zinsmaier</a>
 * @param <L>
 * @param <T>
 */
public class LocalNoveltyScorerNodeModel<L extends Comparable<L>, T extends RealType<T>> extends NodeModel implements BufferedDataTableHolder {

        private static final int DEFAULT_NUMBER_OF_NEIGHBORS = 5;
        static final String[] AVAILABLE_KERNELS = {"HIK", "EXPHIK", "RBF"};

        /**
         * Helper
         *
         * @return SettingsModel to store img column
         */

        static SettingsModelInteger createNumberOfNeighborsModel() {
                return new SettingsModelIntegerBounded("NumberOfNeighbors", DEFAULT_NUMBER_OF_NEIGHBORS, 1, Integer.MAX_VALUE);
        }

        static SettingsModelString createKernelFunctionSelectionModel() {
                return new SettingsModelString("kernelFunction", "HIK");
        }

        static SettingsModelFilterString createColumnSelectionModel() {
                return new SettingsModelFilterString("Column Filter Local Novelty Scorer");
        }

        static SettingsModelString createClassColumnSelectionModel() {
                return new SettingsModelString("Class", "");
        }

        /* SettingsModels */
        private SettingsModelInteger m_numberOfNeighborsModel = createNumberOfNeighborsModel();
        private SettingsModelString m_kernelFunctionModel = createKernelFunctionSelectionModel();
        private SettingsModelFilterString m_columnSelection = createColumnSelectionModel();
        private SettingsModelString m_classColumn = createClassColumnSelectionModel();

        /* Resulting BufferedDataTable */
        private BufferedDataTable m_data;

        /**
         * Constructor LocalNoveltyScorerNodeModel
         */
        public LocalNoveltyScorerNodeModel() {
                super(2, 1);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
                // TODO check inspec for img value column

                final DataTableSpec trainingTableSpec = (DataTableSpec) inSpecs[0];
                final DataTableSpec testTableSpec = (DataTableSpec) inSpecs[1];

                // Check for class column
                DataColumnSpec classColSpec = trainingTableSpec.getColumnSpec(m_classColumn.getStringValue());
                if (classColSpec == null || !classColSpec.getType().isCompatible(NominalValue.class)) {
                        for (int i = trainingTableSpec.getNumColumns() - 1; i >= 0; i--) {
                                if (trainingTableSpec.getColumnSpec(i).getType().isCompatible(NominalValue.class)) {
                                        m_classColumn.setStringValue(trainingTableSpec.getColumnSpec(i).getName());
                                        break;
                                } else if (i == 0)
                                        throw new InvalidSettingsException("Table contains no nominal attribute for classification.");
                        }
                }

                // Check if selected columns from the training table are also in the test table
                // Also check compatibility of selected columns

                List<String> includedCols = m_columnSelection.getIncludeList();
                for (String col : includedCols) {
                        if (!trainingTableSpec.getColumnSpec(col).getType().isCompatible(DoubleValue.class)) {
                                throw new InvalidSettingsException("Selected columns must be compatible with DoubleValue!");
                        }
                        if (!testTableSpec.containsName(col)) {
                                throw new InvalidSettingsException("Selected columns need also be contained in the Test Table");
                        }
                }

                return createOutSpec(testTableSpec);
        }

        private DataTableSpec[] createOutSpec(final DataTableSpec inTableSpec) {
                DataColumnSpec scoreSpec = new DataColumnSpecCreator("Novelty Score", DoubleCell.TYPE).createSpec();

                return new DataTableSpec[] {new DataTableSpec(inTableSpec, new DataTableSpec(scoreSpec))};
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings({})
        protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {

                final BufferedDataTable trainingIn = inData[0];
                final BufferedDataTable testIn = inData[1];
                final BufferedDataContainer container = exec.createDataContainer(createOutSpec(testIn.getDataTableSpec())[0]);

                ColumnRearranger trainingRearranger = new ColumnRearranger(trainingIn.getDataTableSpec());
                ColumnRearranger testRearranger = new ColumnRearranger(testIn.getDataTableSpec());
                List<String> includedCols = m_columnSelection.getIncludeList();
                int numberOfNeighbors = m_numberOfNeighborsModel.getIntValue();

                trainingRearranger.keepOnly(includedCols.toArray(new String[includedCols.size()]));
                testRearranger.keepOnly(includedCols.toArray(new String[includedCols.size()]));

                double[][] trainingData = KernelCalculator.readBufferedDataTable(exec
                                .createColumnRearrangeTable(trainingIn, trainingRearranger, exec));
                double[][] testData = KernelCalculator.readBufferedDataTable(exec.createColumnRearrangeTable(testIn, testRearranger, exec));

                // Get labels for training Data
                final int classColIdx = trainingIn.getDataTableSpec().findColumnIndex(m_classColumn.getStringValue());
                String[] labels = new String[trainingIn.getRowCount()];
                int l = 0;
                for (DataRow row : trainingIn) {
                        labels[l++] = ((StringValue) row.getCell(classColIdx)).getStringValue();
                }

                // Get KernelFunction
                KernelFunction kernelFunction = null;
                switch (m_kernelFunctionModel.getStringValue()) {
                case "HIK":
                        kernelFunction = new HIKKernel();
                        break;
                case "EXPHIK":
                        kernelFunction = new EXPHIKKernel();
                        break;
                case "RBF":
                        // Sigma is defaulted to 1 for testing
                        kernelFunction = new RBFKernel(1);
                        break;
                default:
                        kernelFunction = new HIKKernel();
                }

                // Create KernelCalculator
                KernelCalculator kernelCalculator = new KernelCalculator(trainingData, kernelFunction);

                // Get global KernelMatrix
                RealMatrix globalKernelMatrix = kernelCalculator.kernelize(testData);

                // Calculate distance Matrix
                final RealMatrix distanceMatrix = globalKernelMatrix.createMatrix(globalKernelMatrix.getRowDimension(),
                                globalKernelMatrix.getColumnDimension());
                for (int r = 0; r < globalKernelMatrix.getRowDimension(); r++) {
                        for (int c = 0; c < globalKernelMatrix.getColumnDimension(); c++) {
                                double[] training = trainingData[r];
                                double[] test = testData[c];
                                double distance = kernelFunction.calculate(test, test) + kernelFunction.calculate(training, training) - 2
                                                * globalKernelMatrix.getEntry(r, c);
                                distanceMatrix.setEntry(r, c, distance);
                        }
                }

                // Calculate Local model for each row in the test table
                int colIterator = 0;
                int currentRowIdx = 0;
                for (DataRow row : testIn) {
                        // Find k nearest neighbors
                        ValueIndexPair[] distances = ValueIndexPair.transformArray2ValueIndexPairArray(distanceMatrix.getColumn(colIterator));
                        ValueIndexPair[] neighbors = ValueIndexPair.getKMinima(distances, numberOfNeighbors);

                        // Sort neighbors according to class
                        // NOTE: Ordering by index accomplishes that because the samples were ordered in the input table
                        Arrays.sort(neighbors, new Comparator<ValueIndexPair>() {
                                public int compare(ValueIndexPair o1, ValueIndexPair o2) {
                                        return o1.getIndex() - o2.getIndex();
                                }
                        });

                        // Get local training data and labels, also check if this is a one class problem
                        double[][] localTrainingData = new double[numberOfNeighbors][trainingData[0].length];
                        String[] localLabels = new String[numberOfNeighbors];
                        boolean oneClass = true;
                        String currentLabel = labels[neighbors[0].getIndex()];
                        for (int i = 0; i < numberOfNeighbors; i++) {
                                localTrainingData[i] = trainingData[neighbors[i].getIndex()];
                                String label = labels[neighbors[i].getIndex()];
                                if (!label.equals(currentLabel)) {
                                        oneClass = false;
                                }
                                localLabels[i] = label;
                        }

                        // Calculate local kernel Matrix for training
                        RealMatrix localTrainingKernelMatrix = kernelCalculator.kernelize(localTrainingData, localTrainingData);

                        // Create KNFST model
                        KNFST localModel = null;

                        // Handle one class
                        if (oneClass) {
                                localModel = new OneClassKNFST(localTrainingKernelMatrix);

                                // Handle multiple classes  
                        } else {
                                localModel = new MultiClassKNFST(localTrainingKernelMatrix, localLabels);
                        }

                        // Get local kernel matrix for testing from global kernel matrix
                        double[] localTestKernelMatrixData = new double[numberOfNeighbors];
                        for (int i = 0; i < numberOfNeighbors; i++) {
                                localTestKernelMatrixData[i] = globalKernelMatrix.getEntry(neighbors[i].getIndex(), currentRowIdx);
                        }
                        RealMatrix localTestKernelMatrix = MatrixUtils.createColumnRealMatrix(localTestKernelMatrixData);

                        // Score test sample with local model
                        double normalizer = getMin(localModel.getBetweenClassDistances());
                        double score = localModel.scoreTestData(localTestKernelMatrix)[0] / normalizer;

                        // Write result into data table
                        DataCell[] cells = new DataCell[row.getNumCells() + 1];
                        for (int c = 0; c < cells.length; c++) {
                                cells[c] = (c < cells.length - 1) ? row.getCell(c) : new DoubleCell(score);
                        }
                        container.addRowToTable(new DefaultRow(row.getKey(), cells));

                        currentRowIdx++;
                }

                container.close();
                m_data = container.getTable();
                return new BufferedDataTable[] {m_data};
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BufferedDataTable[] getInternalTables() {
                return new BufferedDataTable[] {m_data};
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) {
                //
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
                m_kernelFunctionModel.loadSettingsFrom(settings);
                m_columnSelection.loadSettingsFrom(settings);
                m_classColumn.loadSettingsFrom(settings);
                m_numberOfNeighborsModel.loadSettingsFrom(settings);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void reset() {
                //
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) {
                //
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void saveSettingsTo(final NodeSettingsWO settings) {
                m_kernelFunctionModel.saveSettingsTo(settings);
                m_columnSelection.saveSettingsTo(settings);
                m_classColumn.saveSettingsTo(settings);
                m_numberOfNeighborsModel.saveSettingsTo(settings);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setInternalTables(final BufferedDataTable[] tables) {
                m_data = tables[0];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
                m_kernelFunctionModel.validateSettings(settings);
                m_columnSelection.validateSettings(settings);
                m_classColumn.validateSettings(settings);
                m_numberOfNeighborsModel.validateSettings(settings);
        }

        /****************** Private helper methods *************************************************/

        private static double getMin(double[] array) {
                if (array.length == 0) {
                        throw new IllegalArgumentException("Array must contain at least one element!");
                }

                double min = array[0];

                for (double d : array) {
                        if (d < min) {
                                min = d;
                        }
                }
                return min;
        }
}
