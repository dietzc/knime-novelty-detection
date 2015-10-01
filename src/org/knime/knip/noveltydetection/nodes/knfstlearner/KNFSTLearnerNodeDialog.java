package org.knime.knip.noveltydetection.nodes.knfstlearner;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;

public class KNFSTLearnerNodeDialog<L extends Comparable<L>> extends DefaultNodeSettingsPane {

        /**
         * Default Constructor
         */
        @SuppressWarnings("unchecked")
        public KNFSTLearnerNodeDialog() {
                addDialogComponent(new DialogComponentColumnFilter(KNFSTLearnerNodeModel.createColumnSelectionModel(),
                                KNFSTLearnerNodeModel.DATA_INPORT, false, DoubleValue.class));

                addDialogComponent(new DialogComponentColumnNameSelection(KNFSTLearnerNodeModel.createClassColumnSelectionModel(),
                                "Select class column", 0, StringValue.class));

                addDialogComponent(new DialogComponentStringSelection(KNFSTLearnerNodeModel.createKernelFunctionSelectionModel(), "Kernel",
                                KNFSTLearnerNodeModel.AVAILABLE_KERNELS));

                addDialogComponent(new DialogComponentBoolean(KNFSTLearnerNodeModel.createSortTableModel(),
                                "Sort Table (select only if table is not already sorted by class)"));
        }
}
