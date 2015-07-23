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
import java.util.List;

import net.imglib2.type.numeric.RealType;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.knip.noveltydetection.knfst.alternative.KNFST;
import org.knime.knip.noveltydetection.nodes.knfstlearner.KNFSTPortObject;
import org.knime.knip.noveltydetection.nodes.knfstlearner.KNFSTPortObjectSpec;

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
@SuppressWarnings("deprecation")
public class LocalNoveltyScorerNodeModel<L extends Comparable<L>, T extends RealType<T>> extends NodeModel implements BufferedDataTableHolder {

        /**
         * Helper
         *
         * @return SettingsModel to store img column
         */

        DataTableSpec m_inTableSpec;

        /* SettingsModels */

        /* Resulting BufferedDataTable */
        private BufferedDataTable m_data;

        /**
         * Constructor SegementCropperNodeModel
         */
        public LocalNoveltyScorerNodeModel() {
                super(new PortType[] {KNFSTPortObject.TYPE, BufferedDataTable.TYPE}, new PortType[] {BufferedDataTable.TYPE});
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected DataTableSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
                // TODO check inspec for img value column

                m_inTableSpec = (DataTableSpec) inSpecs[1];
                KNFSTPortObjectSpec knfstSpec = (KNFSTPortObjectSpec) inSpecs[0];
                List<String> compatibleFeatures = knfstSpec.getCompatibleFeatures();

                for (String feature : compatibleFeatures) {
                        if (!m_inTableSpec.containsName(feature))
                                throw new InvalidSettingsException(
                                                "The input table does not contain the necessary columns needed by the KNFST model.");
                }

                return createOutSpec(m_inTableSpec);
        }

        private DataTableSpec[] createOutSpec(final DataTableSpec inTableSpec) {
                DataColumnSpec scoreSpec = new DataColumnSpecCreator("Novelty Score", DoubleCell.TYPE).createSpec();

                return new DataTableSpec[] {new DataTableSpec(inTableSpec, new DataTableSpec(scoreSpec))};
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings({"unchecked"})
        protected BufferedDataTable[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

                final BufferedDataContainer container = exec.createDataContainer(createOutSpec(m_inTableSpec)[0]);

                final KNFST knfst = ((KNFSTPortObject) inData[0]).getKNFST();
                final BufferedDataTable data = (BufferedDataTable) inData[1];
                final DataTableSpec tableSpec = data.getDataTableSpec();

                final KNFSTPortObjectSpec knfstSpec = (KNFSTPortObjectSpec) ((KNFSTPortObject) inData[0]).getSpec();
                List<String> includedFeatures = knfstSpec.getCompatibleFeatures();

                final ColumnRearranger cr = new ColumnRearranger(tableSpec);

                for (DataColumnSpec colSpec : tableSpec) {
                        if (!includedFeatures.contains(colSpec.getName())) {
                                cr.remove(colSpec.getName());
                        }
                }

                //final BufferedDataTable testData = exec.createColumnRearrangeTable(data, cr, exec);

                final BufferedDataTable testData = data;

                System.out.println(testData.getDataTableSpec().getNumColumns());

                double[] scores = knfst.scoreTestData(testData);

                // add options for different normalizations
                double normalizer = getMin(knfst.getBetweenClassDistances());

                // normalize scores
                for (int i = 0; i < scores.length; i++) {
                        scores[i] = scores[i] / normalizer;
                }

                int scoreIterator = 0;

                for (DataRow row : data) {
                        DataCell[] cells = new DataCell[row.getNumCells() + 1];
                        for (int c = 0; c < cells.length; c++) {
                                cells[c] = (c < cells.length - 1) ? row.getCell(c) : new DoubleCell(scores[scoreIterator++]);
                        }
                        container.addRowToTable(new DefaultRow(row.getKey(), cells));
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
