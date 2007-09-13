/* $Id$ */

package ibis.ipl;

/**
 * Describes the upcalls that are generated for the Ibis group
 * management of a pool.
 * At most one of the methods in this interface will be active at any
 * time (they are serialized by ibis).
 * These upcalls must be explicitly enabled, by means of the
 * {@link Registry#enableEvents()}
 * method.
 * The following also holds:
 * <BR>
 * - For any given Ibis identifier, at most one
 * {@link #joined(IbisIdentifier) joined()} call will be generated.
 * <BR>
 * - For any given Ibis identifier, at most one
 * {@link #left(IbisIdentifier) left()} call will be generated.
 * <BR>
 * - An Ibis instance will also receive a
 *   {@link #joined(IbisIdentifier) joined()} upcall for itself.
 */
public interface RegistryEventHandler {
    /**
     * Upcall generated when an Ibis instance joined the pool.
     * @param joinedIbis the ibis identifier of the Ibis instance that joined the
     * pool. Note: an Ibis instance will also receive a
     * <code>joined</code> upcall for itself.
     * All Ibis instances receive the <code>joined</code> upcalls in the
     * same order.
     */
    public void joined(IbisIdentifier joinedIbis);

    /**
     * Upcall generated when an Ibis instance voluntarily left the pool.
     * @param leftIbis the ibis identifier of the Ibis instance that left the
     * pool.
     */
    public void left(IbisIdentifier leftIbis);

    /**
     * Upcall generated when an Ibis instance crashed or was killed, implicitly
     * removing it from the pool.
     * @param corpse the ibis identifier of the dead Ibis instance.
     */
    public void died(IbisIdentifier corpse);

    /**
     * Upcall generated when one or more Ibisses are sent a signal.
     *
     * This call can only be the result of a
     * {@link Registry#signal(String, IbisIdentifier[])}
     * call. It is always the result of a call by the application.
     * How the receiver of this upcall reacts to this is up to the application.
     * @param signal the value of the signal supplied by the user.
     */
    public void gotSignal(String signal);
    
    /**
     * Upcall generated when a new result for an election is available.
     *
     * @param electionName the name of the election.
     * @param winner the winner of the election. This parameter may be null if
     * the previous winner of an election died or left.
     */
    public void electionResult(String electionName, IbisIdentifier winner);
        
}
