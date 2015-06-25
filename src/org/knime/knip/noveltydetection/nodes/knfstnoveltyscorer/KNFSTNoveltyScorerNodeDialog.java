package org.knime.knip.noveltydetection.nodes.knfstnoveltyscorer;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.knip.base.data.img.ImgPlusValue;

public class KNFSTNoveltyScorerNodeDialog<L extends Comparable<L>> extends DefaultNodeSettingsPane {

        /**
         * Default Constructor
         */
        @SuppressWarnings("unchecked")
        public KNFSTNoveltyScorerNodeDialog() {
                addDialogComponent(new DialogComponentColumnNameSelection(KNFSTNoveltyScorerNodeModel.createImgColumnSelectionModel(), "Column Selection", 0,
                                ImgPlusValue.class));
                String[] patchnums = {"1", "2", "4", "8", "16", "32", "64", "128"};
                addDialogComponent(new DialogComponentStringSelection(KNFSTNoveltyScorerNodeModel.createNumPatchesSelectionModel(), "Number of Patches",
                                patchnums));

        }
}
