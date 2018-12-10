package de.iisys.pippa.core.message.speech_message;

import java.time.LocalDateTime;

import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.skill.SkillRegex;
import de.iisys.pippa.core.skill_executable.ASkillExecutable;

public interface SkillSpeechMessage {

	public void setConfidence(float confidence);

	public void setFutureExecutionRequest(boolean futureExecutionRequest);
	
	public void setExecutionDate(LocalDateTime executionDate);

	public void setPresenceRequest(boolean requestPresence);

	public void setLongRunning(boolean longRunning);

	public void setSkillExecutable(ASkillExecutable skillExecutable);

	public String getAudioText();

	public SkillRegex[] getMatchedRegexes(Skill skillRef);
	
	public void setReturnToDispatcher(boolean returnToDispatcher);
	
	public void setStartDialogRequest(boolean startDialogRequest);
	
	public void setStopDialogRequest(boolean startDialogRequest);

}
