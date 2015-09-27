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
import java.util.Comparator;
import java.util.List;

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
import org.knime.core.data.def.StringCell;
import org.knime.core.data.sort.BufferedDataTableSorter;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.BufferedDataTableHolder;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.knip.noveltydetection.knfst.alternative.EXPHIKKernel;
import org.knime.knip.noveltydetection.knfst.alternative.HIKKernel;
import org.knime.knip.noveltydetection.knfst.alternative.KernelCalculator;
import org.knime.knip.noveltydetection.knfst.alternative.KernelFunction;
import org.knime.knip.noveltydetection.knfst.alternative.RBFKernel;

/**
 * Crop BitMasks or parts of images according to a Labeling
 *
 * @author <a href="mailto:adrian.nembach@uni-konstanz.de">Adrian Nembach</a>
 * 
 * @param <L>
 */
public class LocalNoveltyScorerNodeModel<L extends Comparable<L>> extends NodeModel implements BufferedDataTableHolder {

        private static final int DEFAULT_NUMBER_OF_NEIGHBORS = 100;
        static final String[] AVAILABLE_KERNELS = {"HIK", "EXPHIK", "RBF"};
        private static final boolean DEFAULT_SORT_TABLE = false;

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

        static SettingsModelBoolean createSortTableModel() {
                return new SettingsModelBoolean("SortTable", DEFAULT_SORT_TABLE);
        }

        /* SettingsModels */
        private SettingsModelInteger m_numberOfNeighborsModel = createNumberOfNeighborsModel();
        private SettingsModelString m_kernelFunctionModel = createKernelFunctionSelectionModel();
        private SettingsModelFilterString m_columnSelection = createColumnSelectionModel();
        private SettingsModelString m_classColumn = createClassColumnSelectionModel();
        private SettingsModelBoolean m_sortTable = createSortTableModel();

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

                BufferedDataTable trainingIn = inData[0];
                final BufferedDataTable testIn = inData[1];
                final BufferedDataContainer container = exec.createDataContainer(createOutSpec(testIn.getDataTableSpec())[0]);

                ColumnRearranger trainingRearranger = new ColumnRearranger(trainingIn.getDataTableSpec());
                ColumnRearranger testRearranger = new ColumnRearranger(testIn.getDataTableSpec());
                List<String> includedCols = m_columnSelection.getIncludeList();
                int numberOfNeighbors = m_numberOfNeighborsModel.getIntValue();
                final int classColIdx = trainingIn.getDataTableSpec().findColumnIndex(m_classColumn.getStringValue());

                if (m_sortTable.getBooleanValue()) {
                        BufferedDataTableSorter sorter = new BufferedDataTableSorter(trainingIn, new Comparator<DataRow>() {

                                @Override
                                public int compare(DataRow arg0, DataRow arg1) {
                                        String c1 = ((StringCell) arg0.getCell(classColIdx)).getStringValue();
                                        String c2 = ((StringCell) arg1.getCell(classColIdx)).getStringValue();
                                        return c1.compareTo(c2);
                                }

                        });
                        trainingIn = sorter.sort(exec);
                }

                if (numberOfNeighbors > trainingIn.getRowCount()) {
                        numberOfNeighbors = trainingIn.getRowCount();
                }

                trainingRearranger.keepOnly(includedCols.toArray(new String[includedCols.size()]));
                testRearranger.keepOnly(includedCols.toArray(new String[includedCols.size()]));

                double[][] trainingData = KernelCalculator.readBufferedDataTable(exec
                                .createColumnRearrangeTable(trainingIn, trainingRearranger, exec));
                double[][] testData = KernelCalculator.readBufferedDataTable(exec.createColumnRearrangeTable(testIn, testRearranger, exec));

                // Get labels for training Data
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
                RealMatrix globalKernelMatrix = kernelCalculator.kernelize(trainingData, testData);

                // Get training KernelMatrix
                RealMatrix trainingKernelMatrix = kernelCalculator.kernelize(trainingData, trainingData);

                // Calculate Local model for each row in the test table
                int currentRowIdx = 0;
                final int rowCount = testIn.getRowCount();

                ThreadController threadController = new ThreadController(exec, globalKernelMatrix, trainingKernelMatrix, labels, numberOfNeighbors);
                double[] noveltyScores = threadController.process();

                for (DataRow row : testIn) {

                        // Get nearest neighbors according to distance to current sample in kernel feature space
                        /*
                        // Sort training samples according to distance to current sample in kernel feature space

                        ValueIndexPair[] distances = ValueIndexPair.transformArray2ValueIndexPairArray(globalKernelMatrix.getColumn(currentRowIdx));
                        ValueIndexPair[] neighbors = ValueIndexPair.getK(distances, numberOfNeighbors, new Comparator<ValueIndexPair>() {
                                public int compare(ValueIndexPair o1, ValueIndexPair o2) {
                                        double v1 = o1.getValue();
                                        double v2 = o2.getValue();
                                        int res = 0;
                                        if (v1 < v2)
                                                res = 1;
                                        if (v1 > v2)
                                                res = -1;
                                        return res;
                                }
                        });

                        // Sort neighbors according to class
                        // NOTE: Since the instances are ordered by class in the original table
                        //      sorting by indices is equivalent
                        Arrays.sort(neighbors, new Comparator<ValueIndexPair>() {
                                public int compare(ValueIndexPair o1, ValueIndexPair o2) {
                                        int res = o1.getIndex() - o2.getIndex();
                                        if (res < 0)
                                                res = -1;
                                        if (res > 0)
                                                res = 1;
                                        return res;
                                }
                        });

                        // get local labels and check for one class setting
                        boolean oneClass = true;
                        String[] localLabels = new String[numberOfNeighbors];
                        int[] trainingMatrixIndices = new int[numberOfNeighbors];
                        String currentLabel = labels[neighbors[0].getIndex()];
                        for (int i = 0; i < localLabels.length; i++) {
                                String label = labels[neighbors[i].getIndex()];
                                if (!currentLabel.equals(label)) {
                                        oneClass = false;
                                }
                                localLabels[i] = label;
                                trainingMatrixIndices[i] = neighbors[i].getIndex();
                        }
                        // get kernel matrix of local training data
                        RealMatrix localTrainingKernelMatrix = trainingKernelMatrix.getSubMatrix(trainingMatrixIndices, trainingMatrixIndices);

                        double score = 0;
                        KNFST localModel = null;

                        // train model
                        if (oneClass) {
                                localModel = new OneClassKNFST(localTrainingKernelMatrix);
                        } else {
                                localModel = new MultiClassKNFST(localTrainingKernelMatrix, localLabels);
                        }

                        double normalizer = getMin(localModel.getBetweenClassDistances());
                        score = localModel.scoreTestData(
                                        globalKernelMatrix.getColumnMatrix(currentRowIdx).getSubMatrix(trainingMatrixIndices, new int[] {0}))
                                        .getScores()[0]
                                        / normalizer;
                                         */

                        DataCell[] cells = new DataCell[row.getNumCells() + 1];
                        for (int c = 0; c < cells.length; c++) {
                                cells[c] = (c < cells.length - 1) ? row.getCell(c) : new DoubleCell(noveltyScores[currentRowIdx]);
                        }
                        container.addRowToTable(new DefaultRow(row.getKey(), cells));

                        exec.checkCanceled();
                        exec.setProgress((double) currentRowIdx / rowCount);
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
                m_sortTable.loadSettingsFrom(settings);
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
                m_sortTable.saveSettingsTo(settings);
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
                m_sortTable.validateSettings(settings);
        }
}
