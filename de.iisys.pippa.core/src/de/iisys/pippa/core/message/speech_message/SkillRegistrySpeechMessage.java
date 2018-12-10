package de.iisys.pippa.core.message.speech_message;

import de.iisys.pippa.core.skill.Skill;

public interface SkillRegistrySpeechMessage {

	public Skill[] getDispatchedSkills();

	public SkillRegistrySpeechMessage copy();
	
	public void setSkillRef(Skill skillRef);

}
