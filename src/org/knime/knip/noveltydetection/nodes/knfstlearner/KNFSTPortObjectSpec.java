package org.knime.knip.noveltydetection.nodes.knfstlearner;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.data.util.NonClosableOutputStream;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

public class KNFSTPortObjectSpec extends AbstractSimplePortObjectSpec {

        private String[] m_compatibleFeatures;

        private static final String CFGKEY_COMPATIBLEFEATURES = "CompatibleFeatures";

        public KNFSTPortObjectSpec(List<String> compatibleFeatures) {
                m_compatibleFeatures = compatibleFeatures.toArray(new String[compatibleFeatures.size()]);
        }

        public KNFSTPortObjectSpec() {
        }

        @Override
        public JComponent[] getViews() {
                // TODO Auto-generated method stub
                return null;
        }

        public List<String> getCompatibleFeatures() {
                return Arrays.asList(m_compatibleFeatures);
        }

        private void save(PortObjectSpecZipOutputStream out) {
                ObjectOutputStream oo = null;

                try {
                        out.putNextEntry(new ZipEntry("compatibleFeatures.objectout"));
                        oo = new ObjectOutputStream(new NonClosableOutputStream.Zip(out));
                        oo.writeInt(m_compatibleFeatures.length);
                        for (String feature : m_compatibleFeatures)
                                oo.writeUTF(feature);

                } catch (IOException e) {

                } finally {
                        if (oo != null) {
                                try {
                                        oo.close();
                                } catch (Exception e) {

                                }
                        }
                }

        }

        @Override
        protected void save(ModelContentWO model) {
                model.addStringArray(CFGKEY_COMPATIBLEFEATURES, m_compatibleFeatures);

        }

        @SuppressWarnings("unchecked")
        private static KNFSTPortObjectSpec load(PortObjectSpecZipInputStream in) {
                ObjectInputStream oi = null;
                List<String> compatibleFeatures = null;

                try {
                        // load classifier
                        ZipEntry zentry = in.getNextEntry();
                        assert zentry.getName().equals("compatibleFeatures.objectout");
                        oi = new ObjectInputStream(new NonClosableInputStream.Zip(in));
                        compatibleFeatures = new ArrayList<String>();
                        int size = oi.readInt();
                        for (int i = 0; i < size; i++)
                                compatibleFeatures.add(oi.readUTF());

                } catch (IOException ioe) {

                } finally {
                        if (oi != null) {
                                try {
                                        oi.close();
                                } catch (Exception e) {

                                }
                        }
                }

                return new KNFSTPortObjectSpec(compatibleFeatures);

        }

        @Override
        protected void load(ModelContentRO model) throws InvalidSettingsException {
                m_compatibleFeatures = model.getStringArray(CFGKEY_COMPATIBLEFEATURES);

        }

        @Override
        public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((m_compatibleFeatures == null) ? 0 : m_compatibleFeatures.hashCode());
                return result;
        }

        @Override
        public boolean equals(Object obj) {
                if (this == obj) {
                        return true;
                }
                if (obj == null) {
                        return false;
                }
                if (!(obj instanceof KNFSTPortObjectSpec)) {
                        return false;
                }
                KNFSTPortObjectSpec other = (KNFSTPortObjectSpec) obj;
                if (m_compatibleFeatures == null) {
                        if (other.m_compatibleFeatures != null) {
                                return false;
                        }
                } else if (!m_compatibleFeatures.equals(other.m_compatibleFeatures)) {
                        return false;
                }
                return true;
        }

        @Override
        public String toString() {
                final int maxLen = 10;
                return "KNFSTPortObjectSpec [m_compatibleFeatures="
                                + (m_compatibleFeatures != null ? Arrays.asList(m_compatibleFeatures).subList(0,
                                                Math.min(m_compatibleFeatures.length, maxLen)) : null) + "]";
        }

}
