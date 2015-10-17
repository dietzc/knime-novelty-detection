package org.knime.knip.noveltydetection.nodes.knfstlearner;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.data.util.NonClosableOutputStream;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.AbstractPortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.knip.noveltydetection.knfst.KNFST;

public class KNFSTPortObject extends AbstractPortObject {

        /**
         * Define port type of objects of this class when used as PortObjects.
         */
        public static final PortType TYPE = new PortType(KNFSTPortObject.class);

        private static final String SUMMARY = "Kernel Null Foley Sammon Transformation Object for novelty scoring";

        private KNFST m_knfstModel;
        private KNFSTPortObjectSpec m_spec;

        public KNFSTPortObject() {
        }

        public KNFSTPortObject(KNFST knfst, KNFSTPortObjectSpec spec) {
                m_knfstModel = knfst;
                m_spec = spec;
        }

        public KNFST getKNFST() {
                return m_knfstModel;
        }

        //        private static class KNFSTPortObjectSerializer extends PortObjectSerializer<KNFSTPortObject> {
        //
        //                @Override
        //                public void savePortObject(KNFSTPortObject portObject, PortObjectZipOutputStream out, ExecutionMonitor exec) throws IOException,
        //                                CanceledExecutionException {
        //                        // TODO Auto-generated method stub
        //                        
        //                }
        //
        //                @Override
        //                public KNFSTPortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec) throws IOException,
        //                                CanceledExecutionException {
        //                        // TODO Auto-generated method stub
        //                        return null;
        //                }
        //                
        //        }

        @Override
        protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec) throws IOException, CanceledExecutionException {
                ObjectOutputStream oo = null;
                try {
                        out.putNextEntry(new ZipEntry("knfst.objectout"));
                        oo = new ObjectOutputStream(new NonClosableOutputStream.Zip(out));
                        oo.writeUTF(m_knfstModel.getClass().getName());
                        m_knfstModel.writeExternal(oo);
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

        @Override
        protected void load(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec) throws IOException, CanceledExecutionException {
                ObjectInputStream oi = null;
                KNFST knfst = null;
                try {
                        // load classifier
                        ZipEntry zentry = in.getNextEntry();
                        assert zentry.getName().equals("knfst.objectout");
                        oi = new ObjectInputStream(new NonClosableInputStream.Zip(in));
                        knfst = (KNFST) Class.forName(oi.readUTF()).newInstance();
                        knfst.readExternal(oi);
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
                m_knfstModel = knfst;
                m_spec = (KNFSTPortObjectSpec) spec;
        }

        @Override
        public String getSummary() {
                return SUMMARY;
        }

        @Override
        public PortObjectSpec getSpec() {
                // TODO Auto-generated method stub
                return m_spec;
        }

        @Override
        public JComponent[] getViews() {
                // TODO Auto-generated method stub
                return new JComponent[] {};
        }

        @Override
        public int hashCode() {
                final int prime = 31;
                int result = 1;
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
                return "KNFSTPortObject [m_knfstModel=" + m_knfstModel + "]";
        }

}
