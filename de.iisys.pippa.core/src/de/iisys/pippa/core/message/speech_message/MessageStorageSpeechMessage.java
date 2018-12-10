package de.iisys.pippa.core.message.speech_message;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.skill.SkillRegex;
import de.iisys.pippa.core.skill_executable.ASkillExecutable;

public interface MessageStorageSpeechMessage {

	public UUID getMessageId();
	
	public LocalDateTime getTimeStamp();
	
	//TODO
	public Object getAudio();
	
	public String getAudioText();
	
	public String getAudioUrl();
	
	public Skill[] getDispatchedSkills();
	
	public float getConfidence();
	
	public boolean getFutureExecutionRequest();
	
	public LocalDateTime getExecutionDate();
	
	public boolean getPresenceRequest();
	
	public boolean getStartDialogRequest();
	
	public boolean getStopDialogRequest();
	
	public boolean getLongRunning();
	
	public ASkillExecutable getExecutable();
	
	public HashMap<Skill, SkillRegex[]>getMatchedRegexes();
	
}
