package ibis.impl.net.gen;

import ibis.impl.net.NetDriver;
import ibis.impl.net.NetPoller;
import ibis.impl.net.NetInputUpcall;
import ibis.impl.net.NetPortType;

import java.io.IOException;

/**
 * Provides a generic multiple network input poller.
 */
public final class GenPoller extends NetPoller {

    /**
     * @param pt the {@link ibis.impl.net.NetPortType NetPortType}.
     * @param driver the driver of this poller.
     * @param context the context.
     * @param inputUpcall the input upcall for upcall receives, or
     *        <code>null</code> for downcall receives
     */
    public GenPoller(NetPortType pt,
	   	     NetDriver driver,
		     String context,
		     NetInputUpcall inputUpcall)
	    throws IOException {
	super(pt, driver, context, inputUpcall);
    }
}
