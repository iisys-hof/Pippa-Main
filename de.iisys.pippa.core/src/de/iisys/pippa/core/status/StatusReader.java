package de.iisys.pippa.core.status;

import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.skill_executable.ASkillExecutable;

public interface StatusReader {
	
	public boolean getDialogRunning();

	public Skill getDialogWith();

	public Skill getLastResolvedSkill();

	public Skill[] getLastDispatchedSkills();
	
	public ASkillExecutable getRunningShortExecutable();
		
	public ASkillExecutable getLastShortExecutable();
		
	public ASkillExecutable getRunningLongExecutable();

	public ASkillExecutable getLastLongExecutable();
	
	public boolean isMuted();
	
	public int getVolume();

	public boolean getRecognising();
	
	public boolean getRecording();
}
