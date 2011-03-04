/*
 * Created on 13.04.2004
 */
package bildbearbeiter;

import javax.swing.SwingUtilities;

/**
 * Sinn: kapselt intensive Arbeiten von der eigentlichen
 * GUI ab ... hoffentlich geht dann die Fortschrittsanzeige wieder.
 * 
 * @author hirsch, 13.04.2004
 * @version 2004-04-13
 * $Id$
 * 
 */
public abstract class SwingWorker {
	private Object value = null;  // see getValue(), setValue()

	/**
	 * Class to maintain reference to current worker thread
	 * under separate synchronization control.
	 */
	private static class ThreadVar {
		private Thread thread = null;

		/**
		 * startet einen Thread mit übergebenem Thread als Parameter
		 * @param t Thread
		 */
		ThreadVar(Thread t) {
			thread = t;
		} // end of Konstruktor


		synchronized Thread get() {
			return thread;
		} // end of get


		synchronized void clear() {
			thread = null;
		} // end of clear
	} // end of class ThreadVar

	private ThreadVar threadVar;


	/**
	 * Get the value produced by the worker thread, or null if it
	 * hasn't been constructed yet.
	 */
	protected synchronized Object getValue() {
		return value;
	} // end of getValue


	/**
	 * Set the value produced by worker thread
	 */
	private synchronized void setValue(Object x) {
		value = x;
	} // end of setValue


	/**
	 * Compute the value to be returned by the <code>get</code> method.
	 */
	public abstract Object construct();


	/**
	 * Called on the event dispatching thread (not on the worker thread)
	 * after the <code>construct</code> method has returned.
	 */
	public void finished() {
	} // end of finished


	/**
	 * A new method that interrupts the worker thread.  Call this method
	 * to force the worker to stop what it's doing.
	 */
	public void interrupt() {
		Thread t = threadVar.get();
		if (t != null) {
			t.interrupt();
		}
		threadVar.clear();
	}


	/**
	 * Return the value created by the <code>construct</code> method.
	 * Returns null if either the constructing thread or the current
	 * thread was interrupted before a value was produced.
	 *
	 * @return the value created by the <code>construct</code> method
	 */
	public Object get() {
		while (true) {
			Thread t = threadVar.get();
			if (t == null) {
				return getValue();
			}
			try {
				t.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // propagate
				return null;
			}
		}
	} // end of get


	/**
	 * Start a thread that will call the <code>construct</code> method
	 * and then exit.
	 */
	public SwingWorker() {
		final Runnable doFinished = new Runnable() {
			public void run() {
				finished();
			}
		};

		Runnable doConstruct = new Runnable() {
			public void run() {
				try {
					setValue(construct());
				} finally {
					threadVar.clear();
				}

				SwingUtilities.invokeLater(doFinished);
			}
		};

		Thread t = new Thread(doConstruct);
		threadVar = new ThreadVar(t);
	} // end of Konstruktor


	/**
	 * Start the worker thread.
	 */
	public void start() {
		Thread t = threadVar.get();
		if (t != null) {
			t.start();
		}
	} // end of start
} // end of class