package de.iisys.pippa.core.message.stop_message;

import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message_processor.AMessageProcessor;

/**
 * Message that is passed through the system and instructs every receiver to
 * stop what it is currently doing. This message is triggered by the user
 * uttering the stop-word.
 * 
 * If the flag stopAndClose is set to true, the receiver should also return from
 * it's current runnable loop to close the external thread.
 * 
 * @author rpszabad
 *
 */
public class StopMessage extends AMessage {

	/**
	 * set when every receiver should take care of ending it's current thread
	 */
	private boolean stopAndClose = false;

	/**
	 * origin of the message
	 */
	private AMessageProcessor creator = null;

	public StopMessage(AMessageProcessor creator, boolean stopAndClose) {
		super();
		this.stopAndClose = stopAndClose;
		this.creator = creator;
	}

	private StopMessage(StopMessage copyMessage) {
		super(copyMessage);
		this.creator = copyMessage.creator;
		this.stopAndClose = copyMessage.stopAndClose;
	}

	public boolean isStopAndClose() {
		return this.stopAndClose;
	}

	public AMessageProcessor getCreator() {
		return this.creator;
	}

	public StopMessage copy() {
		return new StopMessage(this);
	}

}
