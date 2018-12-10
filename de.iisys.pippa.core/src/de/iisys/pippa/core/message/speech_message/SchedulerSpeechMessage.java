package de.iisys.pippa.core.message.speech_message;

import java.time.LocalDateTime;

public interface SchedulerSpeechMessage {

	public boolean getFutureExecutionRequest();
	
	public LocalDateTime getExecutionDate();
	
}
