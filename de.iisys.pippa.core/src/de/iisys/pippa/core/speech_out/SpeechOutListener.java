package de.iisys.pippa.core.speech_out;

/**
 * Interface that has to be implemented by every class that wants to use the
 * systems speech-out-service. Creates a return point for the speech-service to
 * inform the listener about the current state of it's set text/audio as it
 * changes.
 * 
 * @author rpszabad
 *
 */
public interface SpeechOutListener {

	/**
	 * the audio started playing
	 */
	public void speechOutStarted();

	/**
	 * the audio was paused
	 */
	public void speechOutPaused();

	/**
	 * the audio resumed playing from a paused state
	 */
	public void speechOutResumed();

	/**
	 * the audio was stopped before it finished playing
	 */
	public void speechOutStopped();

	/**
	 * the audio finished playing
	 */
	public void speechOutFinished();

}
