package de.iisys.pippa.core.message;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abstract superclass for all subordinate message-classes.
 * 
 * @author rpszabad
 *
 */
public abstract class AMessage {

	/**
	 * unique random id created by the system at creation time
	 */
	private UUID messageId = null;

	/**
	 * timestamp of creation
	 */
	private LocalDateTime timeStamp = null;

	/**
	 * creates UUID and sets current time as timestamp
	 */
	public AMessage() {
		this.messageId = UUID.randomUUID();
		this.timeStamp = LocalDateTime.now();
	}

	/**
	 * Copyconstructor used in child-classes to create identical copies of child
	 * classes, e.g. when dispatching a message to multiple skills.
	 * 
	 * @param copyMessage
	 *            message to be copied
	 */
	public AMessage(AMessage copyMessage) {
		this.messageId = copyMessage.messageId;
		this.timeStamp = copyMessage.timeStamp;
	}

	public UUID getMessageId() {
		return messageId;
	}

	public LocalDateTime getTimeStamp() {
		return timeStamp;
	}

}
