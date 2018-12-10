package de.iisys.pippa.core.message.speech_message;

import java.util.HashMap;

import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.skill.SkillRegex;

public interface DispatcherSpeechMessage {

	public void setDispatchedSkills(Skill[] dispatchedSkills);
	
	public String getAudioText();
	
	public void setMatchedRegexes(HashMap<Skill, SkillRegex[]> matchedRegexes);
	
	public boolean getReturnToDispatcher();
	
	public void setReturnToDispatcher(boolean returnToDispatcher);
	
	public Skill[] getDispatchedSkills();
	
}
