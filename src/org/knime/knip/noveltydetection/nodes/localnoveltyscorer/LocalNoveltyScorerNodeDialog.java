package org.knime.knip.noveltydetection.nodes.localnoveltyscorer;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.knip.noveltydetection.kernel.KernelCalculator.KernelType;

public class LocalNoveltyScorerNodeDialog extends DefaultNodeSettingsPane {

        /**
         * Default Constructor
         */
        @SuppressWarnings("unchecked")
        public LocalNoveltyScorerNodeDialog() {

                addDialogComponent(new DialogComponentColumnFilter(LocalNoveltyScorerNodeModel.createColumnSelectionModel(), 0, true,
                                DoubleValue.class));

                addDialogComponent(new DialogComponentColumnNameSelection(LocalNoveltyScorerNodeModel.createClassColumnSelectionModel(),
                                "Select class column", 0, StringValue.class));

                final SettingsModelString kernelType = LocalNoveltyScorerNodeModel.createKernelFunctionSelectionModel();

                KernelType[] availableKernels = KernelType.values();
                String[] kernelNames = new String[availableKernels.length];
                for (int i = 0; i < availableKernels.length; i++) {
                        kernelNames[i] = availableKernels[i].toString();
                }

                addDialogComponent(new DialogComponentStringSelection(kernelType, "Kernel", kernelNames));

                final SettingsModelDouble rbfSigma = LocalNoveltyScorerNodeModel.createRBFSigmaModel();
                final SettingsModelDouble polynomialGamma = LocalNoveltyScorerNodeModel.createPolynomialGammaModel();
                final SettingsModelDouble polynomialBias = LocalNoveltyScorerNodeModel.createPolynomialBiasModel();
                final SettingsModelDouble polynomialPower = LocalNoveltyScorerNodeModel.createPolynomialPower();

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

                addDialogComponent(new DialogComponentNumberEdit(rbfSigma, "Sigma: "));

                addDialogComponent(new DialogComponentNumberEdit(polynomialGamma, "Gamma: "));

                addDialogComponent(new DialogComponentNumberEdit(polynomialBias, "Bias: "));

                addDialogComponent(new DialogComponentNumberEdit(polynomialPower, "Power: "));

                addDialogComponent(new DialogComponentNumber(LocalNoveltyScorerNodeModel.createNumberOfNeighborsModel(), "Number of Neighbors", 1));

                addDialogComponent(new DialogComponentBoolean(LocalNoveltyScorerNodeModel.createNormalizeModel(), "Normalize Novelty Scores"));

                addDialogComponent(new DialogComponentBoolean(LocalNoveltyScorerNodeModel.createSortTableModel(),
                                "Sort Training Table (only select if not already sorted by class)"));
        }
}
