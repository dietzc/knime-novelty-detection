package org.knime.knip.noveltydetection.nodes.localnoveltyscorer;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;

public class LocalNoveltyScorerNodeDialog<L extends Comparable<L>> extends DefaultNodeSettingsPane {

        /**
         * Default Constructor
         */
        @SuppressWarnings("unchecked")
        public LocalNoveltyScorerNodeDialog() {

                addDialogComponent(new DialogComponentNumber(LocalNoveltyScorerNodeModel.createNumberOfNeighborsModel(), "Number of Neighbors", 1));
        }
}
