package org.knime.knip.noveltydetection.nodes.knfstlearner;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;

public class KNFSTLearnerNodeDialog<L extends Comparable<L>> extends DefaultNodeSettingsPane {

        /**
         * Default Constructor
         */
        @SuppressWarnings("unchecked")
        public KNFSTLearnerNodeDialog() {
                addDialogComponent(new DialogComponentColumnFilter(KNFSTLearnerNodeModel.createColumnSelectionModel(), 0, false));

                addDialogComponent(new DialogComponentColumnNameSelection(KNFSTLearnerNodeModel.createClassColumnSelectionModel(),
                                "Select class column", 0, StringValue.class));

                addDialogComponent(new DialogComponentStringSelection(KNFSTLearnerNodeModel.createKernelFunctionSelectionModel(), "Kernel",
                                KNFSTLearnerNodeModel.AVAILABLE_KERNELS));
        }
}
