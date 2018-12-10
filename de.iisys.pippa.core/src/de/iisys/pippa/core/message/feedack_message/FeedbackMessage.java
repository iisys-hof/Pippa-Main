package de.iisys.pippa.core.message.feedack_message;

import java.util.UUID;

import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message.speech_message.SpeechMessage;
import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.skill_executable.ASkillExecutable;

/**
 * Message that is sent from several message processors to a skill upon certain
 * events that the skill should be aware of.
 * 
 * @author rpszabad
 *
 */
public class FeedbackMessage extends AMessage implements FeedbackMessageReader, FeedbackMessageWriter {

	/**
	 * skill to receive the message
	 */
	private Skill receiver = null;

	/**
	 * this message may reference another (speech-)message that caused the sending
	 * of the feedback
	 */
	private UUID referenceMessageId = null;
	
	/**
	 * this message may reference a skill-executable that caused the sending
	 * of the feedback
	 */
	private String referenceExecutableId = null;
	
	/**
	 * when the referenced message was rejected inside the resolver
	 */
	private boolean isRejected = false;
	
	/**
	 * when the referenced executable started running
	 */
	private boolean isRunning = false;
	
	/**
	 * when the referenced executable stopped running
	 */
	private boolean isStopped = false;
	
	/**
	 * when the referenced message is waiting for user-presence
	 */
	private boolean isWaiting = false;
	
	/**
	 * when the referenced message is scheduled
	 */
	private boolean isScheduled = false;
	
	/**
	 * when the referenced message was successfully unscheduled by the skill (removed from scheduler and execution)
	 */
	private boolean isUnscheduled = false;
	
	/**
	 * when the referenced message started the dialog mode for the skill
	 */
	private boolean startedDialog = false;
	
	/**
	 * when the referenced message ended the dialog mode for the skill
	 */
	private boolean endedDialog = false;

	public FeedbackMessage(AMessage message) {
		SpeechMessage speechMessage = (SpeechMessage) message;
		this.receiver = speechMessage.getSkillRef();
		this.referenceMessageId = speechMessage.getMessageId();
		if (speechMessage.getExecutable() != null) {
			this.referenceExecutableId = speechMessage.getExecutable().getExecutableId();
		}
	}
	
	public FeedbackMessage(Skill skill) {
		this.receiver = skill;
	}
	
	public FeedbackMessage(Skill skill, ASkillExecutable executable) {
		this.receiver = skill;
		this.referenceExecutableId = executable.getExecutableId();
	}

	public boolean getIsRejected() {
		return isRejected;
	}

	public void setIsRejected(boolean isRejected) {
		this.isRejected = isRejected;
	}

	public boolean getIsRunning() {
		return isRunning;
	}

	public void setIsRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
	public boolean getIsStopped() {
		return isStopped;
	}

	public void setIsStopped(boolean isStopped) {
		this.isStopped = isStopped;
	}

	public boolean getIsWaiting() {
		return isWaiting;
	}

	public void setIsWaiting(boolean isWaiting) {
		this.isWaiting = isWaiting;
	}

	public boolean getIsScheduled() {
		return isScheduled;
	}

	public void setIsScheduled(boolean isScheduled) {
		this.isScheduled = isScheduled;
	}

	public boolean getIsUnscheduled() {
		return isUnscheduled;
	}

	public void setIsUnscheduled(boolean isUnscheduled) {
		this.isUnscheduled = isUnscheduled;
	}

	public boolean getStartedDialog() {
		return startedDialog;
	}

	public void setStartedDialog(boolean startedDialog) {
		this.startedDialog = startedDialog;
	}

	public boolean getEndedDialog() {
		return endedDialog;
	}

	public void setEndedDialog(boolean endedDialog) {
		this.endedDialog = endedDialog;
	}

	public Skill getReceiver() {
		return receiver;
	}

	public UUID getReferenceMessageId() {
		return referenceMessageId;
	}

	public String getReferenceExecutableId() {
		return referenceExecutableId;
	}
}
