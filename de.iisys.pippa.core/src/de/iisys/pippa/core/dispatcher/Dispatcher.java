package de.iisys.pippa.core.dispatcher;

import de.iisys.pippa.core.skill.Skill;

public interface Dispatcher {

	public void registerRegexes(Skill skillRef);

	public void deregisterRegexes(Skill skillRef);
	
}
