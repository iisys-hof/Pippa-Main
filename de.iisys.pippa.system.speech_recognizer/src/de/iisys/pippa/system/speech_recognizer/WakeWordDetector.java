package de.iisys.pippa.system.speech_recognizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;

public interface WakeWordDetector {

	void setTargetDataLine(TargetDataLine targetLine, AudioFormat format);

	boolean detect();

}
