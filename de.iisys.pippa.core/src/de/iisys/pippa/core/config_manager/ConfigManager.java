package de.iisys.pippa.core.config_manager;

import de.iisys.pippa.core.skill.Skill;

public interface ConfigManager {

	public Config loadSkillConfig(Skill skillRef);

	public boolean storeSkillConfig(Skill skillRef, Config skillConfig) throws Exception;
	
	public Config loadMainConfig();

}
