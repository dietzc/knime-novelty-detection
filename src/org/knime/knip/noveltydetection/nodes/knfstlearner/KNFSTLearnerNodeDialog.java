package org.knime.knip.noveltydetection.nodes.knfstlearner;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.knip.base.data.img.ImgPlusValue;

public class KNFSTLearnerNodeDialog<L extends Comparable<L>> extends DefaultNodeSettingsPane {

        /**
         * Default Constructor
         */
        @SuppressWarnings("unchecked")
        public KNFSTLearnerNodeDialog() {
                addDialogComponent(new DialogComponentColumnNameSelection(KNFSTLearnerNodeModel.createImgColumnSelectionModel(), "Column Selection", 0,
                                ImgPlusValue.class));
                String[] patchnums = {"1", "2", "4", "8", "16", "32", "64", "128"};
                addDialogComponent(new DialogComponentStringSelection(KNFSTLearnerNodeModel.createNumPatchesSelectionModel(), "Number of Patches",
                                patchnums));

        }
}
