package ibis.util;

/**
 * Condition variable synchronization construct.
 *
 * Condition variables are part of thread synchronization primitives of the
 * {@link Monitor} construct. Threads can wait on a condition variable,
 * and condition variables can be signalled by other threads to wake up one
 * waiting thread. A bcast call wakes up all waiting threads on a
 * ConditionVariable.
 *
 * A thread that calls <code>cv_wait</code>, <code>cv_signal</code> or
 * <code>cv_bcast<code> must have locked the {@link Monitor} that owns this
 * ConditionVariable.
 *
 * Condition Variables can be <code>interruptible</code>. For interruptible
 * Condition Variables, <code>Thread.interrupt</code>ing the {@link Thread} that
 * is waiting on this Condition Variable causes the waiting thread to return
 * with an {@link InterruptedException}. Non-interruptible Condition Variables
 * ignore <code>Thread.interrupt</code>.
 */
final public class ConditionVariable {

    private Monitor lock;
    private final boolean INTERRUPTIBLE;

    static long waits;
    static long timed_waits;
    static long signals;
    static long bcasts;


    ConditionVariable(Monitor lock, boolean interruptible) {
	this.lock = lock;
	INTERRUPTIBLE = interruptible;
    }


    ConditionVariable(Monitor lock) {
	this(lock, false);
    }


    final public void cv_wait() throws InterruptedException {
	lock.checkImOwner();
	if (Monitor.STATISTICS) {
	    waits++;
	}

	try {
	    synchronized (this) {
		lock.unlock();
		if (INTERRUPTIBLE) {
		    wait();
		} else {
		    try {
			wait();
		    } catch (InterruptedException e) {
			// Ignore
		    }
		}
	    }
	} finally {
	    lock.lock();
	}
    }


    final public boolean cv_wait(long timeout) throws InterruptedException {
	lock.checkImOwner();
	if (Monitor.STATISTICS) {
	    timed_waits++;
	}

	boolean timedOut = false;

	try {
	    synchronized (this) {
		long now = System.currentTimeMillis();
		lock.unlock();
		if (INTERRUPTIBLE) {
		    wait(timeout);
		} else {
		    try {
			wait(timeout);
		    } catch (InterruptedException e) {
			// Ignore
		    }
		}
		timedOut = (System.currentTimeMillis() - now >= timeout);
	    }
	} finally {
	    lock.lock();
	}

	return timedOut;
    }


    final public void cv_signal() {
	lock.checkImOwner();
	if (Monitor.STATISTICS) {
	    signals++;
	}

	synchronized (this) {
	    notify();
	}
    }


    final public void cv_bcast() {
	lock.checkImOwner();
	if (Monitor.STATISTICS) {
	    bcasts++;
	}

	synchronized (this) {
	    notifyAll();
	}
    }

    static public void report(java.io.PrintStream out) {
	if (Monitor.STATISTICS) {
	    out.println("Condition variables: wait " + waits +
			" timed wait " + timed_waits +
			" signal " + signals +
			" bcast " + bcasts);
	}
    }

}
