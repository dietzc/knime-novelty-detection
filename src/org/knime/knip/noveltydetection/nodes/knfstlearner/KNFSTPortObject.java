package org.knime.knip.noveltydetection.nodes.knfstlearner;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.JComponent;

import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.knip.noveltydetection.knfst.KNFST;

public class KNFSTPortObject implements PortObject, Externalizable {

        private static final String SUMMARY = "Kernel Null Foley Sammon Transformation Object for novelty scoring";

        private KNFST m_knfstModel;

        public KNFSTPortObject(KNFST knfst) {
                m_knfstModel = knfst;
        }

        @Override
        public String getSummary() {
                return SUMMARY;
        }

        @Override
        public PortObjectSpec getSpec() {
                // TODO Auto-generated method stub
                return null;
        }

        @Override
        public JComponent[] getViews() {
                // TODO Auto-generated method stub
                return null;
        }

        @Override
        public void readExternal(ObjectInput arg0) throws IOException, ClassNotFoundException {
                try {
                        m_knfstModel = (KNFST) Class.forName(arg0.readUTF()).newInstance();
                        m_knfstModel.readExternal(arg0);
                } catch (InstantiationException | IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }

        }

        @Override
        public void writeExternal(ObjectOutput arg0) throws IOException {
                arg0.writeUTF(m_knfstModel.getClass().getName());
                m_knfstModel.writeExternal(arg0);

        }

}
