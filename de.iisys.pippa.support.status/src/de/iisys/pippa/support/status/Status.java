package de.iisys.pippa.support.status;

import java.util.logging.Logger;

import de.iisys.pippa.core.service_loader.PippaServiceLoader;
import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.skill_executable.ASkillExecutable;
import de.iisys.pippa.core.status.StatusAccess;
import de.iisys.pippa.core.status.StatusReader;

public class Status implements StatusAccess, StatusReader {

	private static Status instance = null;

	private Logger log = null;

	private volatile Skill[] lastDispatchedSkills = {};
	private volatile Skill lastResolvedSkill = null;
	private volatile boolean dialogRunning = false;
	private volatile Skill dialogWith = null;
	private volatile ASkillExecutable runningShortExecutable = null;
	private volatile ASkillExecutable lastShortExecutable = null;
	private volatile ASkillExecutable runningLongExecutable = null;
	private volatile ASkillExecutable lastLongExecutable = null;
	private volatile int volume = 5;
	private volatile boolean isMuted = false;
	private volatile boolean isRecognising = false;
	private volatile boolean isRecording = false;

	private Status() {
		this.log = PippaServiceLoader.getLogger(null);
	}

	public static synchronized Status getInstance() {
		if (Status.instance == null) {
			Status.instance = new Status();
		}
		return Status.instance;
	}

	public boolean getDialogRunning() {
		return dialogRunning;
	}

	public void setDialogRunning(boolean dialogRunning) {
		this.dialogRunning = dialogRunning;
		this.log.fine("dialog running: " + this.dialogRunning);
	}

	public Skill getDialogWith() {
		return dialogWith;
	}

	public void setDialogWith(Skill dialogWith) {
		this.dialogWith = dialogWith;
		this.log.fine("dialog with: " + this.dialogWith);
	}

	public Skill getLastResolvedSkill() {
		return lastResolvedSkill;
	}

	public void setLastResolvedSkill(Skill lastRunningSkill) {
		this.lastResolvedSkill = lastRunningSkill;
		this.log.fine("last resolved skill: "
				+ this.lastResolvedSkill.getClass().getSimpleName());
	}

	public Skill[] getLastDispatchedSkills() {
		return lastDispatchedSkills;
	}

	public void setLastDispatchedSkills(Skill[] lastDispatchedSkills) {
		this.lastDispatchedSkills = lastDispatchedSkills;
		this.log.fine("last dispatched skills:");
		for (Skill skill : this.lastDispatchedSkills) {
			this.log.fine(skill.getClass().getSimpleName());
		}
	}

	public ASkillExecutable getRunningShortExecutable() {
		return this.runningShortExecutable;
	}

	public ASkillExecutable getLastShortExecutable() {
		return this.lastShortExecutable;
	}

	public ASkillExecutable getRunningLongExecutable() {
		return this.runningLongExecutable;
	}

	public ASkillExecutable getLastLongExecutable() {
		return this.lastLongExecutable;
	}

	public void setRunningShortExecutable(ASkillExecutable runningExecutable) {
		if (this.runningShortExecutable != null) {
			this.lastShortExecutable = this.runningShortExecutable;
		}
		this.runningShortExecutable = runningExecutable;

		if (this.runningShortExecutable != null) {
			this.log.fine("running short executable: "
					+ this.runningShortExecutable.getExecutableId());
		} else {
			this.log.fine("no running short executable");
		}

		if (this.lastShortExecutable != null) {
			this.log.fine("last short executable: "
					+ this.lastShortExecutable.getExecutableId());
		}
	}

	public void setRunningLongExecutable(ASkillExecutable runningExecutable) {
		if (this.runningLongExecutable != null) {
			this.lastLongExecutable = this.runningLongExecutable;
		}
		this.runningLongExecutable = runningExecutable;

		if (this.runningLongExecutable != null) {
			this.log.fine("running long executable: "
					+ this.runningLongExecutable.getExecutableId());
		} else {
			this.log.fine("no running long executable");
		}

		if (this.lastLongExecutable != null) {
			this.log.fine("last long executable: "
					+ this.lastLongExecutable.getExecutableId());
		}
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
		this.log.fine("volume: " + this.volume);
	}

	public boolean isMuted() {
		return isMuted;
	}

	public void setMuted(boolean isMuted) {
		this.isMuted = isMuted;
		this.log.fine("is muted: " + this.isMuted);
	}

	@Override
	public boolean getRecognising() {
		return this.isRecognising;
	}

	@Override
	public void setRecognising(boolean isRecognising) {
		this.isRecognising = isRecognising;
		this.log.fine("is recognising: " + this.isRecognising);
	}

	@Override
	public boolean getRecording() {
		return this.isRecording;
	}

	@Override
	public void setRecording(boolean isRecording) {
		this.isRecording = isRecording;
		this.log.fine("is recording: " + this.isRecording);
	}
	
	public void reset() {
		this.log.info("resetting status");
		lastDispatchedSkills = new Skill[] {};
		lastResolvedSkill = null;
		dialogRunning = false;
		dialogWith = null;
		runningShortExecutable = null;
		lastShortExecutable = null;
		runningLongExecutable = null;
		lastLongExecutable = null;
		volume = 5;
		isMuted = false;
		isRecognising = false;
		isRecording = false;
	}

}
