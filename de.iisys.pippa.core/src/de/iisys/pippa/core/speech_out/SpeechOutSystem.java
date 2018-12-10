package de.iisys.pippa.core.speech_out;

/**
 * Interface offered by the speech-out-service that should be used by the system
 * to register the currently running (therefore allowed) skill executables with
 * the speech-out-service. Calls through the SpeechOut-Interface by skill
 * executables that are not currently not registered are ignored by the service.
 * 
 * @author rpszabad
 *
 */
public interface SpeechOutSystem {

	public void registerShort(SpeechOutListener executable);

	public void registerLong(SpeechOutListener executable);


}
