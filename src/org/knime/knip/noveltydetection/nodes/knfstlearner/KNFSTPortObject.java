package org.knime.knip.noveltydetection.nodes.knfstlearner;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.data.util.NonClosableOutputStream;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.knip.noveltydetection.knfst.KNFST;

public class KNFSTPortObject implements PortObject {

        /**
         * Define port type of objects of this class when used as PortObjects.
         */
        public static final PortType TYPE = new PortType(KNFSTPortObject.class);

        private static final String SUMMARY = "Kernel Null Foley Sammon Transformation Object for novelty scoring";

        private KNFST m_knfstModel;
        private List<String> m_compatibleFeatures;

        public KNFSTPortObject(KNFST knfst, List<String> compatibleFeatures) {
                m_knfstModel = knfst;
                m_compatibleFeatures = compatibleFeatures;
        }

        public KNFST getKNFST() {
                return m_knfstModel;
        }

        public static PortObjectSerializer<KNFSTPortObject> getPortObjectSerializer() {
                return new PortObjectSerializer<KNFSTPortObject>() {

                        @Override
                        public void savePortObject(KNFSTPortObject portObject, PortObjectZipOutputStream out, ExecutionMonitor exec)
                                        throws IOException, CanceledExecutionException {
                                portObject.save(out);

                        }

                        @Override
                        public KNFSTPortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
                                        throws IOException, CanceledExecutionException {
                                // TODO Auto-generated method stub
                                return load(in);
                        }

                };
        }

        private void save(final PortObjectZipOutputStream out) {
                ObjectOutputStream oo = null;
                try {
                        out.putNextEntry(new ZipEntry("knfst.objectout"));
                        oo = new ObjectOutputStream(new NonClosableOutputStream.Zip(out));
                        oo.writeUTF(m_knfstModel.getClass().getName());
                        m_knfstModel.writeExternal(oo);
                        oo.writeInt(m_compatibleFeatures.size());
                        for (String feature : m_compatibleFeatures)
                                oo.writeUTF(feature);
                } catch (IOException ioe) {

                } finally {
                        if (oo != null) {
                                try {
                                        oo.close();
                                } catch (Exception e) {

                                }
                        }
                }
        }

        private static KNFSTPortObject load(final PortObjectZipInputStream in) {
                ObjectInputStream oi = null;
                KNFST knfst = null;
                List<String> compatibleFeatures = null;

                try {
                        // load classifier
                        ZipEntry zentry = in.getNextEntry();
                        assert zentry.getName().equals("knfst.objectout");
                        oi = new ObjectInputStream(new NonClosableInputStream.Zip(in));
                        knfst = (KNFST) Class.forName(oi.readUTF()).newInstance();
                        knfst.readExternal(oi);
                        compatibleFeatures = new ArrayList<String>();
                        int size = oi.readInt();
                        for (int i = 0; i < size; i++)
                                compatibleFeatures.add(oi.readUTF());
                } catch (IOException ioe) {

                } catch (ClassNotFoundException cnf) {

                } catch (InstantiationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                } finally {
                        if (oi != null) {
                                try {
                                        oi.close();
                                } catch (Exception e) {

                                }
                        }
                }

                return new KNFSTPortObject(knfst, compatibleFeatures);

        }

        @Override
        public String getSummary() {
                return SUMMARY;
        }

        @Override
        public PortObjectSpec getSpec() {
                // TODO Auto-generated method stub
                return new KNFSTPortObjectSpec(m_compatibleFeatures);
        }

        @Override
        public JComponent[] getViews() {
                // TODO Auto-generated method stub
                return null;
        }

        @Override
        public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((m_compatibleFeatures == null) ? 0 : m_compatibleFeatures.hashCode());
                result = prime * result + ((m_knfstModel == null) ? 0 : m_knfstModel.hashCode());
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
                if (!(obj instanceof KNFSTPortObject)) {
                        return false;
                }
                KNFSTPortObject other = (KNFSTPortObject) obj;
                if (m_compatibleFeatures == null) {
                        if (other.m_compatibleFeatures != null) {
                                return false;
                        }
                } else if (!m_compatibleFeatures.equals(other.m_compatibleFeatures)) {
                        return false;
                }
                if (m_knfstModel == null) {
                        if (other.m_knfstModel != null) {
                                return false;
                        }
                } else if (!m_knfstModel.equals(other.m_knfstModel)) {
                        return false;
                }
                return true;
        }

        @Override
        public String toString() {
                return "KNFSTPortObject [m_knfstModel=" + m_knfstModel + ", m_compatibleFeatures=" + m_compatibleFeatures + "]";
        }

}
