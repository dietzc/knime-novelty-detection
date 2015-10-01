package org.knime.knip.noveltydetection.nodes.knfstnoveltyscorer;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;

public class KNFSTNoveltyScorerNodeDialog<L extends Comparable<L>> extends DefaultNodeSettingsPane {

        /**
         * Default Constructor
         */
        public KNFSTNoveltyScorerNodeDialog() {
                // Checkbox for appending Novelty Score
                addDialogComponent(new DialogComponentBoolean(KNFSTNoveltyScorerNodeModel.createAppendNoveltyScoreModel(), "Append Novelty Score"));

                // Checkbox for normalizing novelty score
                addDialogComponent(new DialogComponentBoolean(KNFSTNoveltyScorerNodeModel.createNormalizeModel(), "Normalize Novelty Score"));

                // Checkbox for appending Nullspace Coordinates
                addDialogComponent(new DialogComponentBoolean(KNFSTNoveltyScorerNodeModel.createAppendNullspaceCoordinates(),
                                "Append Nullspace Coordinates"));

        }
}
