package ibis.ipl.impl.net.multi;

import ibis.ipl.impl.net.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public interface MultiPlugin {

        public String getSubContext(boolean		isOutgoing,
                                    NetIbisIdentifier  	localId,
                                    NetIbisIdentifier  	remoteId,
                                    ObjectOutputStream	oos,
                                    ObjectInputStream 	ois) throws NetIbisException;
}

