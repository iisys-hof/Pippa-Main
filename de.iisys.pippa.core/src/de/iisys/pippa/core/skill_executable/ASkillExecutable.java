package de.iisys.pippa.core.skill_executable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message.executable_end_message.ExecutableEndMessage;

/**
 * Abstract superclass for skill-executables that already delivers some of the
 * superior functionality. Implements a Runnable, whose run() method
 * encapsulates a freely implementable doRun() method. On return of doRun() and
 * ExecutableEndMessage is sent through the outgoing queue to inform about the
 * end of the executable.
 * 
 * The incorporated stop() method sets a the isStopped flag and also interrupts
 * the current thread. Both objects/mechanisms may be used inside the doRun() to
 * listen for the outside wish to stop the execution of further code.
 * 
 * @author rpszabad
 *
 */
public abstract class ASkillExecutable implements Runnable {

	/**
	 * set when the executable and it's thread should stop
	 */
	private boolean isStopped = false;

	/**
	 * id that identifies this executable, mostly for debugging purposes
	 */
	String executableId = "";

	/**
	 * queue used to send out messages, has to be set from the outside to actually lead somewhere 
	 */
	BlockingQueue<AMessage> outgoingQueue = new LinkedBlockingQueue<AMessage>();

	Thread currentThread = null;

	public ASkillExecutable(String executableId) {
		this.executableId = executableId;
	}

	public final void setOutgoingQueue(BlockingQueue<AMessage> outgoingQueue) {
		this.outgoingQueue = outgoingQueue;
	}

	public final void run() {

		this.currentThread = Thread.currentThread();

		try {
			// Skill's Runnable distinct code execution
			doRun();
		} finally {
			// upon return from execution, a message is sent out (likely to the executor)
			ExecutableEndMessage endMessage = new ExecutableEndMessage(this);
			try {
				this.outgoingQueue.put((AMessage) endMessage);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * sets the stop-flag and triggers an interrupt
	 */
	public final void stop() {
		this.isStopped = true;
		this.currentThread.interrupt();
	}

	public final boolean isStopped() {
		return this.isStopped;
	}

	public final String getExecutableId() {
		return this.executableId;
	}

	public abstract void doRun();

}
