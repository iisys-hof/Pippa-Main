package de.iisys.pippa.core.message.speech_message;

import java.io.File;

public interface AnalyserSpeechMessage {

	public void setAudioText(String text);
	
	//getaudio
	public File getAudio();
}
