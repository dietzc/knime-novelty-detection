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
package org.knime.knip.noveltydetection.nodes.knfstlearner;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.NominalValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.sort.BufferedDataTableSorter;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.knip.noveltydetection.knfst.alternative.EXPHIKKernel;
import org.knime.knip.noveltydetection.knfst.alternative.HIKKernel;
import org.knime.knip.noveltydetection.knfst.alternative.KNFST;
import org.knime.knip.noveltydetection.knfst.alternative.KernelCalculator;
import org.knime.knip.noveltydetection.knfst.alternative.KernelFunction;
import org.knime.knip.noveltydetection.knfst.alternative.MultiClassKNFST;
import org.knime.knip.noveltydetection.knfst.alternative.OneClassKNFST;
import org.knime.knip.noveltydetection.knfst.alternative.RBFKernel;

/**
 * Learns a Kernel Null Foley-Sammon model that can be utilized for Novelty
 * Detection
 *
 * @author <a href="mailto:adrian.nembach@uni-konstanz.de">Adrian Nembach</a>
 * 
 * @param <L>
 */
public class KNFSTLearnerNodeModel<L extends Comparable<L>> extends NodeModel {

        static final int DATA_INPORT = 0;
        static final String[] AVAILABLE_KERNELS = {"HIK", "EXPHIK", "RBF"};
        static final String DEFAULT_KERNEL = AVAILABLE_KERNELS[0];
        static final boolean DEFAULT_SORT_TABLES = false;

        /**
         * Helper
         *
         * @return SettingsModel to store img column
         */
        static SettingsModelString createKernelFunctionSelectionModel() {
                return new SettingsModelString("kernelFunction", DEFAULT_KERNEL);
        }

        static SettingsModelFilterString createColumnSelectionModel() {
                return new SettingsModelFilterString("Column Filter");
        }

        static SettingsModelString createClassColumnSelectionModel() {
                return new SettingsModelString("Class", "");
        }

        static SettingsModelBoolean createSortTableModel() {
                return new SettingsModelBoolean("SortTables", DEFAULT_SORT_TABLES);
        }

        /* SettingsModels */
        private SettingsModelString m_kernelFunctionModel = createKernelFunctionSelectionModel();
        private SettingsModelFilterString m_columnSelection = createColumnSelectionModel();
        private SettingsModelString m_classColumn = createClassColumnSelectionModel();
        private SettingsModelBoolean m_sortTable = createSortTableModel();

        private List<String> m_compatibleFeatures;

        /* Resulting PortObject */
        private KNFSTPortObject m_knfstPortObject;

        /**
         * Constructor SegementCropperNodeModel
         */
        public KNFSTLearnerNodeModel() {
                super(new PortType[] {BufferedDataTable.TYPE}, new PortType[] {KNFSTPortObject.TYPE, BufferedDataTable.TYPE});
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

                DataTableSpec dataSpec = (DataTableSpec) inSpecs[DATA_INPORT];

                // Check class
                DataColumnSpec colSpec = dataSpec.getColumnSpec(m_classColumn.getStringValue());
                if (colSpec == null || !colSpec.getType().isCompatible(NominalValue.class)) {
                        for (int i = dataSpec.getNumColumns() - 1; i >= 0; i--) {
                                if (dataSpec.getColumnSpec(i).getType().isCompatible(NominalValue.class)) {
                                        m_classColumn.setStringValue(dataSpec.getColumnSpec(i).getName());
                                        break;
                                } else if (i == 0)
                                        throw new InvalidSettingsException("Table contains no nominal" + " attribute for classification.");
                        }
                }

                /*
                // Check input columns later used for training
                for (int i = 0; i < dataSpec.getNumColumns(); i++) {
                        if (!(dataSpec.getColumnSpec(i).getType().isCompatible(DoubleValue.class)
                                        || dataSpec.getColumnSpec(i).getType().isCompatible(IntValue.class) || dataSpec.getColumnSpec(i).getType()
                                        .isCompatible(LongValue.class))) {
                                throw new InvalidSettingsException("The features used for training need to be numeric");
                        }
                }
                */
                List<String> featureNameList = m_columnSelection.getIncludeList();
                List<String> compatibleFeatures = new LinkedList<String>();
                for (String feature : featureNameList) {
                        DataColumnSpec featureSpec = dataSpec.getColumnSpec(feature);
                        if (featureSpec.getType().isCompatible(DoubleValue.class)) {
                                compatibleFeatures.add(feature);
                        }
                }
                m_compatibleFeatures = compatibleFeatures;

                return new PortObjectSpec[] {new KNFSTPortObjectSpec(compatibleFeatures), null};
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings({})
        protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

                BufferedDataTable data = (BufferedDataTable) inData[0];
                String kernelFunctionName = m_kernelFunctionModel.getStringValue();

                final int classColIdx = data.getSpec().findColumnIndex(m_classColumn.getStringValue());

                // sort table if necessary
                if (m_sortTable.getBooleanValue()) {
                        BufferedDataTableSorter sorter = new BufferedDataTableSorter(data, new Comparator<DataRow>() {

                                @Override
                                public int compare(DataRow arg0, DataRow arg1) {
                                        String c1 = ((StringCell) arg0.getCell(classColIdx)).getStringValue();
                                        String c2 = ((StringCell) arg1.getCell(classColIdx)).getStringValue();
                                        return c1.compareTo(c2);
                                }

                        });
                        data = sorter.sort(exec);
                }

                String[] labels = new String[data.getRowCount()];
                int l = 0;
                boolean oneClass = true;
                String currentClass = null;
                for (DataRow row : data) {
                        StringValue label = (StringValue) row.getCell(classColIdx);
                        if (currentClass == null) {
                                currentClass = label.getStringValue();
                        } else if (!currentClass.equals(label.getStringValue())) {
                                oneClass = false;
                        }
                        labels[l++] = label.getStringValue();
                }

                List<String> includedColumns = m_columnSelection.getIncludeList();
                DataTableSpec tableSpec = data.getDataTableSpec();

                ColumnRearranger cr = new ColumnRearranger(tableSpec);
                cr.keepOnly(includedColumns.toArray(new String[includedColumns.size()]));

                final BufferedDataTable training = exec.createColumnRearrangeTable(data, cr, exec);

                KNFST knfst = null;

                KernelFunction kernelFunction = null;
                switch (kernelFunctionName) {
                case "HIK":
                        kernelFunction = new HIKKernel();
                        break;
                case "EXPHIK":
                        kernelFunction = new EXPHIKKernel();
                        break;
                case "RBF":
                        // Sigma is defaulted to 0.1 for testing
                        kernelFunction = new RBFKernel(1);
                        break;
                default:
                        kernelFunction = new HIKKernel();
                }

                final KernelCalculator kernelCalculator = new KernelCalculator(training, kernelFunction);

                if (oneClass) {
                        try {
                                knfst = new OneClassKNFST(kernelCalculator);
                        } catch (Exception e) {
                                e.printStackTrace();
                                throw e;
                        }
                } else {
                        knfst = new MultiClassKNFST(kernelCalculator, labels);
                }

                m_knfstPortObject = new KNFSTPortObject(knfst, m_compatibleFeatures);

                // Write target points into table
                String[] uniqueLabels = new HashSet<String>(Arrays.asList(labels)).toArray(new String[0]);
                Arrays.sort(uniqueLabels, new Comparator<String>() {
                        public int compare(String s1, String s2) {
                                return s1.compareTo(s2);
                        }
                });
                double[][] targetPoints = knfst.getTargetPoints();
                DataColumnSpec[] colSpecs = new DataColumnSpec[targetPoints[0].length + 1];
                for (int i = 0; i < colSpecs.length; i++) {
                        if (i < colSpecs.length - 1) {
                                colSpecs[i] = new DataColumnSpecCreator("Dim" + i, DoubleCell.TYPE).createSpec();
                        } else {
                                colSpecs[i] = new DataColumnSpecCreator("Class", StringCell.TYPE).createSpec();
                        }
                }

                final DataTableSpec spec = new DataTableSpec(colSpecs);
                final BufferedDataContainer container = exec.createDataContainer(spec);
                for (int r = 0; r < targetPoints.length; r++) {
                        DataCell[] cells = new DataCell[targetPoints[r].length + 1];
                        for (int d = 0; d < targetPoints[r].length; d++) {
                                cells[d] = new DoubleCell(targetPoints[r][d]);
                        }
                        cells[cells.length - 1] = new StringCell(uniqueLabels[r]);
                        container.addRowToTable(new DefaultRow(new RowKey("tar_" + r), cells));
                }
                container.close();

                return new PortObject[] {m_knfstPortObject, container.getTable()};
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
                m_sortTable.saveSettingsTo(settings);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
                m_kernelFunctionModel.validateSettings(settings);
                m_columnSelection.validateSettings(settings);
                m_classColumn.validateSettings(settings);
                m_sortTable.validateSettings(settings);
        }
}
