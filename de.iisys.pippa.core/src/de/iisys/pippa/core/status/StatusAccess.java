package de.iisys.pippa.core.status;

import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.skill_executable.ASkillExecutable;

public interface StatusAccess extends StatusReader {

	public void setDialogRunning(boolean dialogRunning);

	public void setDialogWith(Skill dialogWith);

	public void setLastResolvedSkill(Skill lastResolvedSkill);

	public void setLastDispatchedSkills(Skill[] lastDispatchedSkills);

	public void setRunningShortExecutable(ASkillExecutable runningExecutable);
	
	public void setRunningLongExecutable(ASkillExecutable runningExecutable);
	
	public void setVolume(int volume);
	
	public void setMuted(boolean isMuted);

	public void setRecognising(boolean isRecognising);
	
	public void setRecording(boolean isRecording);
	
	public void reset();
			
}
