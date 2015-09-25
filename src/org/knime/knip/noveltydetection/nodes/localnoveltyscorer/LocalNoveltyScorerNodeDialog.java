package org.knime.knip.noveltydetection.nodes.localnoveltyscorer;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;

public class LocalNoveltyScorerNodeDialog<L extends Comparable<L>> extends DefaultNodeSettingsPane {

        /**
         * Default Constructor
         */
        @SuppressWarnings("unchecked")
        public LocalNoveltyScorerNodeDialog() {

                addDialogComponent(new DialogComponentColumnFilter(LocalNoveltyScorerNodeModel.createColumnSelectionModel(), 0, true,
                                DoubleValue.class));

                addDialogComponent(new DialogComponentColumnNameSelection(LocalNoveltyScorerNodeModel.createClassColumnSelectionModel(),
                                "Select class column", 0, StringValue.class));

                addDialogComponent(new DialogComponentStringSelection(LocalNoveltyScorerNodeModel.createKernelFunctionSelectionModel(), "Kernel",
                                LocalNoveltyScorerNodeModel.AVAILABLE_KERNELS));

                addDialogComponent(new DialogComponentNumber(LocalNoveltyScorerNodeModel.createNumberOfNeighborsModel(), "Number of Neighbors", 1));

                addDialogComponent(new DialogComponentBoolean(LocalNoveltyScorerNodeModel.createSortTableModel(),
                                "Sort training table (only select if not already sorted by class)"));
        }
}
