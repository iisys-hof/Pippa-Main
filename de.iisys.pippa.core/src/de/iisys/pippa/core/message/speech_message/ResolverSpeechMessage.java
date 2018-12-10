package de.iisys.pippa.core.message.speech_message;

import java.time.LocalDateTime;
import java.util.UUID;

import de.iisys.pippa.core.skill.Skill;

public interface ResolverSpeechMessage {

	public Skill getSkillRef();
	public float getConfidence();
	public boolean getFutureExecutionRequest();
	public LocalDateTime getExecutionDate();
	public boolean getPresenceRequest();
	public boolean getStartDialogRequest();
	public boolean getStopDialogRequest();
	public Skill[] getDispatchedSkills();
	public boolean getReturnToDispatcher();
	public UUID getMessageId();
	public boolean getLongRunning();
}
