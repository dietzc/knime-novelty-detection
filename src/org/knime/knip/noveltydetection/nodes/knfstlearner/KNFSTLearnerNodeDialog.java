package org.knime.knip.noveltydetection.nodes.knfstlearner;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.knip.noveltydetection.kernel.KernelCalculator.KernelType;

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

                final SettingsModelString kernelType = KNFSTLearnerNodeModel.createKernelFunctionSelectionModel();

                KernelType[] availableKernels = KernelType.values();
                String[] kernelNames = new String[availableKernels.length];
                for (int i = 0; i < availableKernels.length; i++) {
                        kernelNames[i] = availableKernels[i].toString();
                }

                final SettingsModelDouble rbfSigma = KNFSTLearnerNodeModel.createRBFSigmaModel();
                final SettingsModelDouble polynomialGamma = KNFSTLearnerNodeModel.createPolynomialGammaModel();
                final SettingsModelDouble polynomialBias = KNFSTLearnerNodeModel.createPolynomialBiasModel();
                final SettingsModelDouble polynomialPower = KNFSTLearnerNodeModel.createPolynomialPower();

                kernelType.addChangeListener(new ChangeListener() {

                        @Override
                        public void stateChanged(ChangeEvent e) {

                                if (kernelType.getStringValue() == "RBF") {
                                        rbfSigma.setEnabled(true);
                                        polynomialBias.setEnabled(false);
                                        polynomialGamma.setEnabled(false);
                                        polynomialPower.setEnabled(false);
                                } else if (kernelType.getStringValue() == "Polynomial") {
                                        rbfSigma.setEnabled(false);
                                        polynomialBias.setEnabled(true);
                                        polynomialGamma.setEnabled(true);
                                        polynomialPower.setEnabled(true);
                                } else {
                                        rbfSigma.setEnabled(false);
                                        polynomialBias.setEnabled(false);
                                        polynomialGamma.setEnabled(false);
                                        polynomialPower.setEnabled(false);
                                }
                        }
                });

                addDialogComponent(new DialogComponentStringSelection(kernelType, "Kernel", kernelNames));

                addDialogComponent(new DialogComponentNumberEdit(rbfSigma, "Sigma: "));

                addDialogComponent(new DialogComponentNumberEdit(polynomialGamma, "Gamma: "));

                addDialogComponent(new DialogComponentNumberEdit(polynomialBias, "Bias: "));

                addDialogComponent(new DialogComponentNumberEdit(polynomialPower, "Power: "));

                addDialogComponent(new DialogComponentBoolean(KNFSTLearnerNodeModel.createSortTableModel(),
                                "Sort Table (select only if table is not already sorted by class)"));
        }
}
