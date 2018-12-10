package de.iisys.pippa.core.message.speech_message;

import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.skill_executable.ASkillExecutable;

public interface ExecutorSpeechMessage {

	public boolean getLongRunning();

	public ASkillExecutable getSkillExecutable();
	
	public Skill getSkillRef();
	
	public boolean getStartDialogRequest();

}
