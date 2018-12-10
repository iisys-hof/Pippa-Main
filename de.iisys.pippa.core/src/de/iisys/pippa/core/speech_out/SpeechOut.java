package de.iisys.pippa.core.speech_out;

/**
 * Interface offered by the speech-out-service that interfaces with the systems hardware and
 * a TTS-system to offer the ability to output spoken text through the systems
 * speakers.
 * 
 * @author rpszabad
 *
 */
public interface SpeechOut {

	/**
	 * In case the calling skill-executable is currently registered with the
	 * speech-out-service, the given text is converted into a playable soundfile and
	 * set to be played back.
	 * 
	 * @param skillExecutable
	 * @param text
	 * @param hasMarkup
	 *            denotes whether the given text contains SSML markup
	 */
	public void setOutputText(SpeechOutListener skillExecutable, String text, boolean hasMarkup);

	/**
	 * In case the calling skill-executable is currently registered with the
	 * speech-out-service, the previously given text is played back now.
	 * 
	 * @param skillExecutable
	 */
	public void play(SpeechOutListener skillExecutable);

	/**
	 * In case the calling skill-executable is currently registered with the
	 * speech-out-service, the previously played back text is paused. This is
	 * ignored for speech output rendered by short executables (longPlaying ==
	 * false);
	 * 
	 * @param skillExecutable
	 */
	public void pause(SpeechOutListener skillExecutable);

	/**
	 * In case the calling skill-executable is currently registered with the
	 * speech-out-service, the previously set text is stopped, resume is not
	 * possible in this case.
	 * 
	 * @param skillExecutable
	 */
	public void stop(SpeechOutListener skillExecutable);

}
