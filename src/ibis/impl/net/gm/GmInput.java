package ibis.ipl.impl.net.gm;

import ibis.ipl.impl.net.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Hashtable;


/**
 * The GM input implementation (block version).
 */
public final class GmInput extends NetBufferedInput {

        private final boolean         useUpcallThreadOpt = true;        


	/**
	 * The peer {@link ibis.ipl.impl.net.NetSendPort NetSendPort}
	 * local number.
	 */
	private volatile Integer      spn  	      = null;

	/**
	 * The buffer block allocator.
	 */
	private NetAllocator          allocator       = null;

        private long                  deviceHandle   =    0;
	private long                  inputHandle    =    0;
        private int                   lnodeId        =   -1;
        private int                   lportId        =   -1;
        private int                   lmuxId         =   -1;
        private int                   lockId         =   -1;
        private int []                lockIds        = null;
        private int                   rnodeId        =   -1;
        private int                   rportId        =   -1;
        private int                   rmuxId         =   -1;
        private int                   blockLen       =    0;
        private boolean               firstBlock     = true;
        private UpcallThread          upcallThread   = null;
        private volatile Runnable    currentThread   =  null;
        private final int            threadStackSize = 256;
        private int                  threadStackPtr  = 0;
        private PooledUpcallThread[] threadStack     = new PooledUpcallThread[threadStackSize];
        private int                  upcallThreadNum = 0;

        private Driver               gmDriver     = null;
        
	native long nInitInput(long deviceHandle) throws NetIbisException;
        native int  nGetInputNodeId(long inputHandle) throws NetIbisException;
        native int  nGetInputPortId(long inputHandle) throws NetIbisException;
        native int  nGetInputMuxId(long inputHandle) throws NetIbisException;
        native void nConnectInput(long inputHandle, int remoteNodeId, int remotePortId, int remoteMuxId) throws NetIbisException;
        native int nPostBuffer(long inputHandle, byte []b, int base, int length) throws NetIbisException;
	native void nCloseInput(long inputHandle) throws NetIbisException;

	/**
	 * Constructor.
	 *
	 * @param sp the properties of the input's 
	 * {@link ibis.ipl.impl.net.NetSendPort NetSendPort}.
	 * @param driver the GM driver instance.
	 */
	GmInput(NetPortType pt, NetDriver driver, String context)
		throws NetIbisException {
                super(pt, driver, context);

                gmDriver = (Driver)driver;

                Driver.gmAccessLock.lock(false);
                deviceHandle = Driver.nInitDevice(0);
                inputHandle = nInitInput(deviceHandle);
                Driver.gmAccessLock.unlock(false);

                arrayThreshold = 256;
	}

        private final class PooledUpcallThread extends Thread {
                private boolean  end   = false;
                private NetMutex sleep = new NetMutex(true);

                public PooledUpcallThread(String name) {
                        super("GmInput.PooledUpcallThread: "+name);
                }                

                public void run() {
                        log.in();
                        while (!end) {
                                //System.err.println("trying to get lock");
                                try {
                                        sleep.ilock();
                                        currentThread = this;
                                } catch (InterruptedException e) {
                                        continue;
                                }
                                
                                while (!end) {
                                        //System.err.println("got lock, pumping");
                                        try {
                                                gmDriver.blockingPump(lockId, lockIds);
                                        } catch (NetIbisClosedException e) {
                                                end = true;
                                                continue;
                                        } catch (NetIbisException e) {
                                                throw new Error(e.getMessage());
                                        }
                                        
                                        //System.err.println("got message, starting upcall");
                                        activeNum = spn;
                                        firstBlock = true;
                                        initReceive();
                                        if (activeNum == null) {
                                                throw new Error("invalid state, spn = "+spn);
                                        }
                                        
                                        try {
                                                upcallFunc.inputUpcall(GmInput.this, activeNum);
                                        } catch (NetIbisInterruptedException e) {
                                                if (currentThread == this) {
                                                        activeNum = null;
                                                        PooledUpcallThread ut = null;
                                        
                                                        synchronized(threadStack) {
                                                                if (threadStackPtr > 0) {
                                                                        ut = threadStack[--threadStackPtr];
                                                                } else {
                                                                        ut = new PooledUpcallThread("no "+upcallThreadNum++);
                                                                        ut.start();
                                                                }        
                                                        }

                                                        currentThread = ut;

                                                        if (ut != null) {
                                                                ut.exec();
                                                        }
                                                }
                                                break;
                                        } catch (NetIbisClosedException e) {
                                                if (currentThread == this) {
                                                        activeNum = null;
                                                        PooledUpcallThread ut = null;
                                        
                                                        synchronized(threadStack) {
                                                                if (threadStackPtr > 0) {
                                                                        ut = threadStack[--threadStackPtr];
                                                                } else {
                                                                        ut = new PooledUpcallThread("no "+upcallThreadNum++);
                                                                        ut.start();
                                                                }        
                                                        }

                                                        currentThread = ut;

                                                        if (ut != null) {
                                                                ut.exec();
                                                        }
                                                }
                                                break;
                                        } catch (NetIbisException e) {
                                                throw new Error(e.getMessage());
                                        }
                                        
                                        
                                        if (currentThread == this) {
                                                
                                                try {
                                                        GmInput.super.finish();
                                                } catch (Exception e) {
                                                        throw new Error(e.getMessage());
                                                }
                                                
                                                //System.err.println("reusing thread");
                                                continue;
                                        } else {
                                                synchronized (threadStack) {
                                                        if (threadStackPtr < threadStackSize) {
                                                                //System.err.println("storing thread");
                                                                threadStack[threadStackPtr++] = this;
                                                        } else {
                                                                //System.err.println("distroying thread");
                                                                return;
                                                        }
                                                }
                                                break;
                                        }
                                }
                        }
                        log.out();
                }

                public void exec() {
                        log.in();
                        sleep.unlock();
                        log.out();
                }

                public void end() {
                        log.in();
                        end = true;
                        this.interrupt();
                        log.out();
                }
        }

        private final class UpcallThread extends Thread {
                private boolean end = false;

                public UpcallThread(String name) {
                        super("GmInput.UpcallThread: "+name);
                }                

                public void run() {
                        log.in();
                        while (!end) {
                                try {
                                        gmDriver.blockingPump(lockId, lockIds);
                                } catch (NetIbisClosedException e) {
                                        end = true;
                                        continue;
                                } catch (NetIbisException e) {
                                        throw new Error(e.getMessage());
                                }
                                
                                activeNum = spn;
                                firstBlock = true;
                                initReceive();
                                try {
                                        upcallFunc.inputUpcall(GmInput.this, activeNum);
                                } catch (NetIbisInterruptedException e) {
                                        activeNum = null;
                                        break;
                                } catch (NetIbisClosedException e) {
                                        break;
                                } catch (NetIbisException e) {
                                        throw new Error(e.getMessage());
                                }
                                
                                activeNum = null;
                        }
                        log.out();
                }

                public void end() {
                        log.in();
                        end = true;
                        this.interrupt();
                        log.out();
                }
        }

	/*
	 * Sets up an incoming GM connection.
	 *
	 * @param spn {@inheritDoc}
	 * @param is {@inheritDoc}
	 * @param os {@inheritDoc}
	 */
	public synchronized void setupConnection(NetConnection cnx) throws NetIbisException {
                log.in();
                //System.err.println("setupConnection-->");
                if (this.spn != null) {
                        throw new Error("connection already established");
                }
                
                Driver.gmAccessLock.lock(false);
                lnodeId = nGetInputNodeId(inputHandle);
                lportId = nGetInputPortId(inputHandle);
                lmuxId  = nGetInputMuxId(inputHandle);
                lockId  = lmuxId*2 + 2;

                //System.err.println("initializing lockIds");
                lockIds = new int[2];
                lockIds[0] = lockId; // input lock
                lockIds[1] = 0;      // main  lock
                Driver.gmLockArray.initLock(lockId, true);
                Driver.gmAccessLock.unlock(false);

                Hashtable lInfo = new Hashtable();
                lInfo.put("gm_node_id", new Integer(lnodeId));
                lInfo.put("gm_port_id", new Integer(lportId));
                lInfo.put("gm_mux_id", new Integer(lmuxId));
                Hashtable rInfo = null;
                

		try {
                        ObjectOutputStream os = new ObjectOutputStream(cnx.getServiceLink().getOutputSubStream(this, "gm"));
                        os.flush();

                        ObjectInputStream  is = new ObjectInputStream(cnx.getServiceLink().getInputSubStream (this, "gm"));
                        os.writeObject(lInfo);
                        os.flush();

                        rInfo = (Hashtable)is.readObject();

                        rnodeId = ((Integer) rInfo.get("gm_node_id")).intValue();
                        rportId = ((Integer) rInfo.get("gm_port_id")).intValue();
                        rmuxId  = ((Integer) rInfo.get("gm_mux_id") ).intValue();

                        Driver.gmAccessLock.lock(false);
                        nConnectInput(inputHandle, rnodeId, rportId, rmuxId);
                        Driver.gmAccessLock.unlock(false);

                        os.write(1);
                        os.flush();
                        is.read();

                        os.close();
                        is.close();
		} catch (IOException e) {
			throw new NetIbisException(e);
		} catch (ClassNotFoundException e) {
                        throw new Error(e);
                }

                mtu = 2*1024*1024;
                
		allocator = new NetAllocator(mtu);
		this.spn = cnx.getNum();
                if (spn == null) {
                        throw new Error("invalid state");
                }

                if (upcallFunc != null) {
                        if (useUpcallThreadOpt) {
                                PooledUpcallThread up = new PooledUpcallThread(lnodeId+":"+lportId+"("+lmuxId+") --> "+rnodeId+":"+rportId+"("+rmuxId+")");
                                up.start();
                                up.exec();
                        } else {
                                (upcallThread = new UpcallThread(lnodeId+":"+lportId+"("+lmuxId+") --> "+rnodeId+":"+rportId+"("+rmuxId+")")).start();
                        }
                }
                log.out();
	}

        /**
         * {@inheritDoc}
         */
	public Integer poll(boolean block) throws NetIbisException {
                log.in();
                //System.err.println("poll-->");

                if (activeNum != null) {
                        throw new Error("invalid state");
                }
                
                activeNum = null;
                
		if (spn == null) {
			return null;
		}

                if (block) {
                        gmDriver.blockingPump(lockId, lockIds);
                } else {
                        if (!gmDriver.tryPump(lockId, lockIds)) {
                                return null;
                        }
                }                
                                
                activeNum  = spn;
                firstBlock = true;
                initReceive();
                log.out();

                return activeNum;
	}

	/**
	 * {@inheritDoc}
	 */
	public void receiveByteBuffer(NetReceiveBuffer b) throws NetIbisException {
                log.in();
                //System.err.println("Receiving buffer, base = "+b.base+", length = "+b.length);

                if (firstBlock) {
                        firstBlock = false;
                } else {
                        /* Request reception */
                        gmDriver.blockingPump(lockId, lockIds);
                }
  
                Driver.gmReceiveLock.lock();

                Driver.gmAccessLock.lock(true);
                int result = nPostBuffer(inputHandle, b.data, 0, b.data.length);
                Driver.gmAccessLock.unlock(true);

                //System.err.println("receiveByteBuffer: size = "+result);

                if (result == 0) {
                        /* Ack completion */
                        gmDriver.blockingPump(lockId, lockIds);

                        /* Communication transmission */
                        gmDriver.blockingPump(lockId, lockIds);

                        if (b.length == 0) {
                                b.length = blockLen;
                        } else {
                                if (b.length != blockLen) {
                                        throw new Error("length mismatch: got "+blockLen+" bytes, "+b.length+" bytes were required");
                                }
                        }
                
                } else {
                        if (b.length == 0) {
                                b.length = result;
                        } else {
                                if (b.length != result) {
                                        throw new Error("length mismatch: got "+result+" bytes, "+b.length+" bytes were required");
                                }
                        }
                }

                Driver.gmReceiveLock.unlock();
                //System.err.println("Receiving buffer, base = "+b.base+", length = "+b.length+" - ok");
                log.out();
        }
        

        public void finish() throws NetIbisException {
                log.in();
                super.finish();
                activeNum = null;
                
                //System.err.println("GmInput: finish-->");
                if (currentThread != null) {
                        //System.err.println("GmInput: finish, need to select another thread");

                        PooledUpcallThread ut = null;
                                        
                        synchronized(threadStack) {
                                if (threadStackPtr > 0) {
                                        ut = threadStack[--threadStackPtr];
                                } else {
                                        ut = new PooledUpcallThread("no "+upcallThreadNum++);
                                        ut.start();
                                }        
                        }

                        currentThread = ut;

                        if (ut != null) {
                                ut.exec();
                        }
                }
                //System.err.println("GmInput: finish<--");
                log.out();
        }

        public synchronized void close(Integer num) throws NetIbisException {
                log.in();
                if (spn == num) {
                        Driver.gmAccessLock.lock(true);
                        Driver.gmLockArray.deleteLock(lockId);

                        if (inputHandle != 0) {
                                nCloseInput(inputHandle);
                                inputHandle = 0;
                        }
                
                        if (deviceHandle != 0) {
                                Driver.nCloseDevice(deviceHandle);
                                deviceHandle = 0;
                        }

                        synchronized (threadStack) {                
                                if (upcallThread != null) {
                                        upcallThread.end();
                                }

                                if (currentThread != null) {
                                        ((PooledUpcallThread)currentThread).end();
                                }
                
                                while (threadStackPtr > 0) {
                                        threadStack[--threadStackPtr].end();
                                }
                        }
                
                        Driver.gmAccessLock.unlock(true);
                        spn = null;
                }
                log.out();
        }
        

	/**
	 * {@inheritDoc}
	 */
	public void free() throws NetIbisException {
                log.in();
		spn = null;

                Driver.gmAccessLock.lock(true);
                Driver.gmLockArray.deleteLock(lockId);

                if (inputHandle != 0) {
                        nCloseInput(inputHandle);
                        inputHandle = 0;
                }
                
                if (deviceHandle != 0) {
                        Driver.nCloseDevice(deviceHandle);
                        deviceHandle = 0;
                }

                if (upcallThread != null) {
                        upcallThread.end();
                        while (true) {
                                try {
                                        upcallThread.join();
                                        break;
                                } catch (InterruptedException e) {
                                        //
                                }
                        }
                }

                synchronized (threadStack) {                
                        if (currentThread != null) {
                                ((PooledUpcallThread)currentThread).end();
                                while (true) {
                                        try {
                                                ((PooledUpcallThread)currentThread).join();
                                                break;
                                        } catch (InterruptedException e) {
                                                //
                                        }
                                }
                        }

                        for (int i = 0; i < threadStackSize; i++) {
                                if (threadStack[i] != null) {
                                        threadStack[i].end();
                                        while (true) {
                                                try {
                                                        threadStack[i].join();
                                                        break;
                                                } catch (InterruptedException e) {
                                                        //
                                                }
                                        }
                                }
                        }
                }
                
                Driver.gmAccessLock.unlock(true);

		super.free();
                log.out();
	}
}
