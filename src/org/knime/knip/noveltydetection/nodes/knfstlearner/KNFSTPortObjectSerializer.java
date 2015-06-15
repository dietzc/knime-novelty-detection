package org.knime.knip.noveltydetection.nodes.knfstlearner;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObject.PortObjectSerializer;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;

public class KNFSTPortObjectSerializer<T extends PortObject> extends PortObjectSerializer<T> {

        @Override
        public void savePortObject(T portObject, PortObjectZipOutputStream out, ExecutionMonitor exec) throws IOException, CanceledExecutionException {
                final KNFSTPortObject knfstPO = (KNFSTPortObject) portObject;
                ((ObjectOutput) out).writeUTF(knfstPO.getClass().getName());
                knfstPO.writeExternal((ObjectOutput) out);

        }

        @Override
        public T loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec) throws IOException,
                        CanceledExecutionException {
                try {
                        final KNFSTPortObject knfstPO = (KNFSTPortObject) Class.forName(((ObjectInput) in).readUTF()).newInstance();
                        knfstPO.readExternal((ObjectInput) in);
                        return (T) knfstPO;
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
                return null;

        }

}
