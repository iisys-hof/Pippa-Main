package de.iisys.pippa.core.skill_registry;

import de.iisys.pippa.core.skill.Skill;

public interface SkillRegistry {

	public void registerSkill(Skill skillRef);
	
	public void deregisterSkill(Skill skillRef);
	
}
