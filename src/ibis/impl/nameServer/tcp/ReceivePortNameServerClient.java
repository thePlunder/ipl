/* $Id$ */

package ibis.impl.nameServer.tcp;

import ibis.ipl.BindingException;
import ibis.ipl.ConnectionTimedOutException;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.ReceivePort;
import ibis.ipl.ReceivePortIdentifier;
import ibis.io.Conversion;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

class ReceivePortNameServerClient implements Protocol {

    InetAddress server;

    int port;

    InetAddress localAddress;

    private static Logger logger = ibis.util.GetLogger
            .getLogger(ReceivePortNameServerClient.class.getName());

    ReceivePortNameServerClient(InetAddress localAddress, InetAddress server,
            int port) {
        this.server = server;
        this.port = port;
        this.localAddress = localAddress;
    }

    public ReceivePortIdentifier lookup(String name, long timeout)
            throws IOException {
        ReceivePortIdentifier[] r = lookup(new String[] {name}, timeout, false);
        if (r != null && r.length != 0) {
            return r[0];
        }
        return null;
    }

    private String namesList(String[] names) {
        StringBuffer s = new StringBuffer("");
        s.append(names.length);
        s.append(" [");
        
        for (int i = 0; i < names.length; i++) {
            s.append(names[i]);
            if (i < names.length-1) {
                s.append(", ");
            }
        }
      
        s.append("]");
        
        return s.toString();
    }

    public ReceivePortIdentifier[] lookup(String[] names, long timeout, 
            boolean allowPartialResults) throws IOException {
        
        DataOutputStream out = null;
        DataInputStream in = null;
        Socket s = null;

        ReceivePortIdentifier[] ids = null;
       
        try {
            
            long time = System.currentTimeMillis();
            
            if (logger.isDebugEnabled()) {
                logger.debug("Port connecting to " + namesList(names));
            }
                       
                   
            s = NameServerClient.nsConnect(server, port, localAddress, false,
                    10);

            out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));

            // request a new Port.
            out.writeByte(PORT_LOOKUP);
            out.writeBoolean(allowPartialResults);       
            out.writeInt(names.length);
            for (int i = 0; i < names.length; i++) {
                out.writeUTF(names[i]);
            }
            out.writeLong(timeout);
            out.flush();

            in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            int result = in.readByte();

            switch (result) {
            case PORT_UNKNOWN:
                int cnt = in.readInt();
                names = new String[cnt];
                for (int i = 0; i < cnt; i++) {
                    names[i] = in.readUTF();
                }
                String list = namesList(names);
                if (logger.isDebugEnabled()) {
                    
                    long t = System.currentTimeMillis() - time;                    
                    logger.debug("Port returns after " + t + " " + list + 
                            ": PORT_UNKNOWN");
                }
                throw new ConnectionTimedOutException("could not find some ports: " + list);
            case PORT_KNOWN:
                ids = new ReceivePortIdentifier[names.length];
                if (logger.isDebugEnabled()) {
                    long t = System.currentTimeMillis() - time;
                    
                    logger.debug("Port returns after " + t + " " + 
                            namesList(names) + ": PORT_KNOWN");
                }
                for (int i = 0; i < names.length; i++) {
                    try {
                        int len = in.readInt();
                        
                        if (len == 0) {
                            ids[i] = null;                            
                        } else { 
                            byte[] b = new byte[len];
                            in.readFully(b, 0, len);
                            ids[i] = (ReceivePortIdentifier) Conversion.byte2object(b);
                        } 
                    } catch (ClassNotFoundException e) {
                        throw new IOException("Unmarshall fails " + e);
                    }
                }
                break;
            default:
                throw new StreamCorruptedException(
                        "Registry: lookup got illegal opcode " + result);
            }
        } finally {
            NameServer.closeConnection(in, out, s);
        }

        return ids;
    }

    public ReceivePortIdentifier[] query(IbisIdentifier ident) {
        /* not implemented yet */
        return new ReceivePortIdentifier[0];
    }

    public void bind(String name, ReceivePort prt) throws IOException {
        bind(name, prt.identifier());
    }

    public void bind(String name, ReceivePortIdentifier id) throws IOException {
        Socket s = null;
        DataOutputStream out = null;
        DataInputStream in = null;
        int result;

        if (logger.isDebugEnabled()) {
            logger.debug(this + ": bind \"" + name + "\" to " + id);
        }

        try {
            s = NameServerClient.nsConnect(server, port, localAddress, false,
                    10);

            out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));

            // request a new Port.
            out.writeByte(PORT_NEW);
            out.writeUTF(name);
            byte[] buf = Conversion.object2byte(id);
            out.writeInt(buf.length);
            out.write(buf);
            out.flush();

            in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            result = in.readByte();
        } finally {
            NameServer.closeConnection(in, out, s);
        }

        switch (result) {
        case PORT_REFUSED:
            throw new BindingException("Port name \"" + name
                    + "\" is not unique!");
        case PORT_ACCEPTED:
            break;
        default:
            throw new StreamCorruptedException(
                    "Registry: bind got illegal opcode " + result);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(this + ": bound \"" + name + "\" to " + id);
        }
    }

    public void rebind(String name, ReceivePortIdentifier id)
            throws IOException {
        Socket s = null;
        DataOutputStream out = null;
        DataInputStream in = null;
        int result;

        if (logger.isDebugEnabled()) {
            logger.debug(this + ": rebind \"" + name + "\" to " + id);
        }

        try {
            s = NameServerClient.nsConnect(server, port, localAddress, false,
                    10);

            out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));

            // request a rebind
            out.writeByte(PORT_REBIND);
            out.writeUTF(name);
            byte[] buf = Conversion.object2byte(id);
            out.writeInt(buf.length);
            out.write(buf);
            out.flush();

            in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            result = in.readByte();
        } finally {
            NameServer.closeConnection(in, out, s);
        }

        switch (result) {
        case PORT_ACCEPTED:
            break;
        default:
            throw new StreamCorruptedException(
                    "Registry: bind got illegal opcode " + result);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(this + ": rebound \"" + name + "\" to " + id);
        }
    }

    public void unbind(String name) {

        Socket s = null;
        DataOutputStream out = null;
        DataInputStream in = null;

        try {
            s = NameServerClient.nsConnect(server, port, localAddress, false,
                    5);

            out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));

            // request a new Port.
            out.writeByte(PORT_FREE);
            out.writeUTF(name);
            out.flush();

            in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

            byte temp = in.readByte();
            if (temp != 0) {
                throw new BindingException("Port name \"" + name
                        + "\" is not bound!");
            }
        } catch (Exception e) {
            logger.info("unbind of " + name + " failed");
        } finally {
            NameServer.closeConnection(in, out, s);
        }
    }

    public String[] list(String pattern) throws IOException {
        Socket s = null;
        DataOutputStream out = null;
        DataInputStream in = null;
        String[] result;

        try {
            s = NameServerClient.nsConnect(server, port, localAddress, false,
                    10);

            out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));

            // request a list of names.
            out.writeByte(PORT_LIST);
            out.writeUTF(pattern);
            out.flush();

            in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

            byte num = in.readByte();

            result = new String[num];
            for (int i = 0; i < num; i++) {
                result[i] = in.readUTF();
            }
        } finally {
            NameServer.closeConnection(in, out, s);
        }
        return result;
    }
}
