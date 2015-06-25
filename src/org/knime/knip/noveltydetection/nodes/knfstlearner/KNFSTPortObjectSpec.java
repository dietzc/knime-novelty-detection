package org.knime.knip.noveltydetection.nodes.knfstlearner;

import java.io.IOException;

import javax.swing.JComponent;

import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

public class KNFSTPortObjectSpec implements PortObjectSpec {

        @Override
        public JComponent[] getViews() {
                // TODO Auto-generated method stub
                return null;
        }

        public PortObjectSpecSerializer<KNFSTPortObjectSpec> getPortObjectSpecSerializer() {
                return new PortObjectSpecSerializer<KNFSTPortObjectSpec>() {

                        @Override
                        public void savePortObjectSpec(KNFSTPortObjectSpec portObjectSpec, PortObjectSpecZipOutputStream out) throws IOException {
                                // TODO Auto-generated method stub

                        }

                        @Override
                        public KNFSTPortObjectSpec loadPortObjectSpec(PortObjectSpecZipInputStream in) throws IOException {
                                // TODO Auto-generated method stub
                                return new KNFSTPortObjectSpec();
                        }

                };
        }
}
