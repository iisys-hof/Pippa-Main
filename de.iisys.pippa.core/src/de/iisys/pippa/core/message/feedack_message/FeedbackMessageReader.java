package de.iisys.pippa.core.message.feedack_message;

import java.time.LocalDateTime;
import java.util.UUID;

import de.iisys.pippa.core.skill.Skill;

public interface FeedbackMessageReader {

	boolean getIsRejected();

	boolean getIsRunning();

	boolean getIsWaiting();

	boolean getIsScheduled();

	boolean getStartedDialog();

	boolean getEndedDialog();

	boolean getIsUnscheduled();
	
	Skill getReceiver();

	UUID getReferenceMessageId();

	String getReferenceExecutableId();
	
	UUID getMessageId();
	
	LocalDateTime getTimeStamp();

	boolean getIsStopped();

}