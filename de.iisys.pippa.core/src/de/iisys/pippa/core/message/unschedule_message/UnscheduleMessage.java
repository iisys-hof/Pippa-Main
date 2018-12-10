package de.iisys.pippa.core.message.unschedule_message;

import java.util.UUID;

import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.skill.Skill;

/**
 * Message created by skills to advise the scheduler to remove the referenced
 * message from it's schedule.
 * 
 * @author rpszabad
 *
 */
public class UnscheduleMessage extends AMessage {

	/**
	 * origin of the message
	 */
	private Skill creator = null;

	/**
	 * message to be removed from the schedule
	 */
	private UUID messagetoDelete = null;

	public UnscheduleMessage(Skill creator, UUID messageToDelete) {
		super();
		this.creator = creator;
		this.messagetoDelete = messageToDelete;
	}

	public Skill getCreator() {
		return creator;
	}

	public UUID getUnscheduleMessageId() {
		return messagetoDelete;
	}

}
