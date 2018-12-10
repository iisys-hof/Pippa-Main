package de.iisys.pippa.core.message.speech_message;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;

import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.skill.SkillRegex;
import de.iisys.pippa.core.skill_executable.ASkillExecutable;

/**
 * Main message class that is passed through the system's message-processors and
 * subsequently enriched with information and objects. Should lead to an
 * executable action, that is conducted by the system executor.
 * 
 * @author rpszabad
 *
 */
public class SpeechMessage extends AMessage implements SkillSpeechMessage, AnalyserSpeechMessage,
		AudioStorageSpeechMessage, SchedulerSpeechMessage, DispatcherSpeechMessage, SkillRegistrySpeechMessage,
		RecognizerSpeechMessage, ResolverSpeechMessage, ExecutorSpeechMessage, MessageStorageSpeechMessage {

	// TODO what kind of audio file
	/**
	 * audio file from the speech recognizer with the users utterance
	 */
	private File audio = null;

	/**
	 * audio converted to a string by the speech analyser
	 */
	private String audioText = "";

	/**
	 * replacement url for the cast-off audio file from audio storage
	 */
	private String audioUrl = "";

	/**
	 * skills that were chosen by the dispatcher to compute on the users utterance
	 */
	private Skill[] dispatchedSkills = {};

	/**
	 * regexes that triggered the dispatcher to choose the chosen skill for
	 * computation
	 */
	private HashMap<Skill, SkillRegex[]> matchedRegexes = new HashMap<Skill, SkillRegex[]>();

	/**
	 * receiver of this certain message object, as there might be a copy for each
	 * choosen skill
	 */
	private Skill skillRef = null;

	/**
	 * confidence given by the skill that the users utterance was meant for it
	 */
	private float confidence = (float) 0.0;

	/**
	 * set if the skill wishes the execution of it's message/executable at a later
	 * time
	 */
	private boolean futureExecutionRequest = false;

	/**
	 * denotes the wished time of execution
	 */
	private LocalDateTime executionDate = null;

	/**
	 * set if the skill wishes the execution of it's message as soon as the user's
	 * presence was detected, combinable with futureExecutionRequest
	 */
	private boolean presenceRequest = false;

	/**
	 * set if the skill wishes to start a dialog
	 */
	private boolean startDialogRequest = false;

	/**
	 * set if the skill wishes to end it's dialog
	 */
	private boolean stopDialogRequest = false;

	/**
	 * Set if the skills executable is set to be long-running. This means that
	 * other, short-running executables can be executed in parallel, e.g. there is
	 * one executable playing music (long) and another quickly telling the time
	 * (short)
	 */
	private boolean longRunning = false;

	/**
	 * Set if the skill can not make any sense of the speech-message (and the user
	 * utterance), expectedly during dialog mode. This will cause the message to
	 * return to the dispatcher for a full regex-scan.
	 */
	private boolean returnToDispatcher = false;

	/**
	 * the executable that is inserted by the skill to be executed by the systems
	 * executor
	 */
	private ASkillExecutable skillExecutable = null;

	public SpeechMessage() {
		super();
	}

	public String getAudioText() {
		return audioText;
	}

	public void setAudioText(String audioText) {
		this.audioText = audioText;
	}

	public String getAudioUrl() {
		return audioUrl;
	}

	public void setAudioUrl(String audioUrl) {
		this.audioUrl = audioUrl;
	}

	public Skill[] getDispatchedSkills() {
		return dispatchedSkills;
	}

	public void setDispatchedSkills(Skill[] dispatchedSkills) {
		this.dispatchedSkills = dispatchedSkills;
	}

	public SkillRegex[] getMatchedRegexes(Skill skillRef) {
		return matchedRegexes.get(skillRef);
	}

	public void setMatchedRegexes(HashMap<Skill, SkillRegex[]> matchedRegexes) {
		this.matchedRegexes = matchedRegexes;
	}

	public Skill getSkillRef() {
		return skillRef;
	}

	public void setSkillRef(Skill skillRef) {
		this.skillRef = skillRef;
	}

	public float getConfidence() {
		return confidence;
	}

	public void setConfidence(float confidence) {
		this.confidence = confidence;
	}

	public boolean isFutureExecutionRequest() {
		return futureExecutionRequest;
	}

	public void setFutureExecutionRequest(boolean futureExecutionRequest) {
		this.futureExecutionRequest = futureExecutionRequest;
	}

	public LocalDateTime getExecutionDate() {
		return executionDate;
	}

	public void setExecutionDate(LocalDateTime executionDate) {
		this.executionDate = executionDate;
	}

	public boolean isPresenceRequest() {
		return presenceRequest;
	}

	public void setPresenceRequest(boolean presenceRequest) {
		this.presenceRequest = presenceRequest;
	}

	public void setStartDialogRequest(boolean dialogRequest) {
		this.startDialogRequest = dialogRequest;
	}

	public boolean isLongRunning() {
		return longRunning;
	}

	public void setLongRunning(boolean longRunning) {
		this.longRunning = longRunning;
	}

	public ASkillExecutable getExecutable() {
		return skillExecutable;
	}

	public void setSkillExecutable(ASkillExecutable executable) {
		this.skillExecutable = executable;
	}

	public void setAudio(File audioFile) {
		this.audio = audioFile;
	}

	public boolean getFutureExecutionRequest() {
		return futureExecutionRequest;
	}

	public boolean getPresenceRequest() {
		return presenceRequest;
	}

	public boolean getStartDialogRequest() {
		return startDialogRequest;
	}

	public boolean getLongRunning() {
		return longRunning;
	}

	public ASkillExecutable getSkillExecutable() {
		return skillExecutable;
	}

	public File getAudio() {
		return audio;
	}

	public HashMap<Skill, SkillRegex[]> getMatchedRegexes() {
		return matchedRegexes;
	}

	private SpeechMessage(SpeechMessage copyMessage) {
		super(copyMessage);

		this.audio = copyMessage.audio;
		this.audioText = new String(copyMessage.audioText);
		this.audioUrl = new String(copyMessage.audioUrl);
		this.dispatchedSkills = copyMessage.dispatchedSkills.clone();
		this.matchedRegexes = copyMessage.matchedRegexes;
		this.skillRef = copyMessage.skillRef;
		this.confidence = copyMessage.confidence;
		this.futureExecutionRequest = copyMessage.futureExecutionRequest;
		this.executionDate = copyMessage.executionDate;
		this.presenceRequest = copyMessage.presenceRequest;
		this.startDialogRequest = copyMessage.startDialogRequest;
		this.longRunning = copyMessage.longRunning;
		this.skillExecutable = copyMessage.skillExecutable;
	}
	
	public SpeechMessage copy() {
		return new SpeechMessage(this);
	}

	public boolean getReturnToDispatcher() {
		return this.returnToDispatcher;
	}

	public void setReturnToDispatcher(boolean returnToDispatcher) {
		this.returnToDispatcher = returnToDispatcher;
	}

	public void setStopDialogRequest(boolean stopDialogRequest) {
		this.stopDialogRequest = stopDialogRequest;
	}

	public boolean getStopDialogRequest() {
		return this.stopDialogRequest;
	}

}
