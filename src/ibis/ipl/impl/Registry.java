/* $Id$ */

package ibis.ipl.impl;

import ibis.ipl.IbisCapabilities;
import ibis.ipl.IbisConfigurationException;
import ibis.ipl.IbisProperties;
import ibis.ipl.RegistryEventHandler;

import java.io.IOException;
import java.util.Properties;

/** 
 * This implementation of the {@link ibis.ipl.Registry} interface
 * defines the API between an Ibis implementation and the registry.
 * This way, an Ibis implementation can dynamically load any registry
 * implementaton.
 */
public abstract class Registry implements ibis.ipl.Registry {

    /**
     * Notifies the registry that the calling Ibis instance is leaving.
     * @exception IOException may be thrown when communication with the
     * registry fails.
     */
    public abstract void leave() throws IOException;

    /**
     * Obtains a sequence number from the registry.
     * Each sequencer has a name, which must be provided to this call.
     * @param name the name of this sequencer.
     * @exception IOException may be thrown when communication with the
     * registry fails.
     */
    public abstract long getSequenceNumber(String name) throws IOException;

    /**
     * Creates a registry for the specified Ibis instance.
     * @param handler the handler for registry events, or <code>null</code>
     * if no registry events are needed.
     * @param properties to get some properties from, and to pass on
     * to the registry.
     * @param data the implementation dependent data in the IbisIdentifier.
     * @param ibisImplemenentationIdentifier the identification of this ibis 
     * implementation, including version, class and such. Must be identical
     * for all ibisses in a single pool.
     * @exception Throwable can be any exception resulting from looking up
     * the registry constructor or the invocation attempt.
     */
    public static Registry createRegistry(IbisCapabilities caps, 
            RegistryEventHandler handler, Properties properties, 
            byte[] data, String ibisImplemenentationIdentifier) throws Throwable {

        String registryName = properties.getProperty(IbisProperties.REGISTRY_IMPL);

        if (registryName == null) {
            throw new IbisConfigurationException("Could not create registry: "
                    + "property " + IbisProperties.REGISTRY_IMPL + "  is not set.");
        }

        Class<?> c = Class.forName(registryName);

        try {
            return (Registry) c.getConstructor(new Class[] {
                    IbisCapabilities.class, RegistryEventHandler.class, 
                    Properties.class, byte[].class, String.class}).newInstance(
                        new Object[] { caps, handler, properties, data, ibisImplemenentationIdentifier });
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Returns the Ibis identifier.
     */
    public abstract IbisIdentifier getIbisIdentifier();
}
