package de.iisys.pippa.core.message_processor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.iisys.pippa.core.message.AMessage;

/**
 * Abstract superclass for all subordinate message processor classes. Message
 * processors receive messages through their incoming-queues, carry out
 * individual computation, enrich the message-object with additional information
 * and dispatch the message through their outgoing-queue.
 * 
 * @author rpszabad
 *
 */
public abstract class AMessageProcessor implements Runnable {

	// Queue which is used to receive messages from the previous processor.
	// Can be be set with the previous processor's outgoing queue to create an
	// automated chain.
	private BlockingQueue<AMessage> incomingQueue = new LinkedBlockingQueue<AMessage>();

	// Queue which is used to send messages to the next processor. Can be set wit a
	// subsequent's processor's incoming queue to create an automated chain.
	private BlockingQueue<AMessage> outgoingQueue = new LinkedBlockingQueue<AMessage>();

	public final void setIncomingQueue(BlockingQueue<AMessage> incomingQueue) {
		this.incomingQueue = incomingQueue;
	}

	public final void setOutgoingQueue(BlockingQueue<AMessage> outgoingQueue) {
		this.outgoingQueue = outgoingQueue;
	}

	public final BlockingQueue<AMessage> getIncomingQueue() {
		return this.incomingQueue;
	}

	public final BlockingQueue<AMessage> getOutgoingQueue() {
		return outgoingQueue;
	}

}
