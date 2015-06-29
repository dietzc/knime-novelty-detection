package org.knime.knip.noveltydetection.nodes.knfstlearner;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.data.util.NonClosableOutputStream;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

public class KNFSTPortObjectSpec implements PortObjectSpec {

        private List<String> m_compatibleFeatures;

        public KNFSTPortObjectSpec(List<String> compatibleFeatures) {
                m_compatibleFeatures = compatibleFeatures;
        }

        @Override
        public JComponent[] getViews() {
                // TODO Auto-generated method stub
                return null;
        }

        public List<String> getCompatibleFeatures() {
                return m_compatibleFeatures;
        }

        public static PortObjectSpecSerializer<KNFSTPortObjectSpec> getPortObjectSpecSerializer() {
                return new PortObjectSpecSerializer<KNFSTPortObjectSpec>() {

                        @Override
                        public void savePortObjectSpec(KNFSTPortObjectSpec portObjectSpec, PortObjectSpecZipOutputStream out) throws IOException {
                                // TODO Auto-generated method stub
                                portObjectSpec.save(out);

                        }

                        @Override
                        public KNFSTPortObjectSpec loadPortObjectSpec(PortObjectSpecZipInputStream in) throws IOException {
                                // TODO Auto-generated method stub
                                return load(in);
                        }

                };
        }

        private void save(PortObjectSpecZipOutputStream out) {
                ObjectOutputStream oo = null;

                try {
                        out.putNextEntry(new ZipEntry("compatibleFeatures.objectout"));
                        oo = new ObjectOutputStream(new NonClosableOutputStream.Zip(out));
                        oo.writeUTF(m_compatibleFeatures.getClass().getName());
                        oo.writeInt(m_compatibleFeatures.size());
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

        private static KNFSTPortObjectSpec load(PortObjectSpecZipInputStream in) {
                ObjectInputStream oi = null;
                List<String> compatibleFeatures = null;

                try {
                        // load classifier
                        ZipEntry zentry = in.getNextEntry();
                        assert zentry.getName().equals("compatibleFeatures.objectout");
                        oi = new ObjectInputStream(new NonClosableInputStream.Zip(in));
                        compatibleFeatures = (List<String>) Class.forName(oi.readUTF()).newInstance();
                        for (int i = 0; i < oi.readInt(); i++)
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

                return new KNFSTPortObjectSpec(compatibleFeatures);

        }
}
