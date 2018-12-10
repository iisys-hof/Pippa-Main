package de.iisys.pippa.system.speech_recognizer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import ai.kitt.snowboy.SnowboyDetect;

public class SnowboyDetector implements WakeWordDetector {

	static {
		System.loadLibrary("snowboy-detect-java");
	}

	private final String SNOWBOY_PATH = System.getProperty("user.home")
			+ "/Desktop/Snowboy/";
	private final String MODEL = "snowboy.umdl";
	private final String COMMON = "common.res";
	private final String SENSITIVITY = "0.5";
	private final float GAIN = 1;
	private final boolean FRONTEND = false;

	TargetDataLine targetLine = null;
	AudioFormat format = null;
	SnowboyDetect detector = null;

	public SnowboyDetector(TargetDataLine targetLine, AudioFormat format) {
		this.targetLine = targetLine;

		this.detector = new SnowboyDetect(SNOWBOY_PATH + COMMON, SNOWBOY_PATH
				+ MODEL);
		this.detector.SetSensitivity(SENSITIVITY);
		this.detector.SetAudioGain(GAIN);
		this.detector.ApplyFrontend(FRONTEND);

		this.setTargetDataLine(targetLine, format);
	}

	@Override
	public boolean detect() {

		// Reads 0.1 second of audio in each call.
		byte[] targetData = new byte[3200];
		short[] snowboyData = new short[1600];
		int numBytesRead;

		numBytesRead = targetLine.read(targetData, 0, targetData.length);

		if (numBytesRead == -1) {
			System.out.print("Snowboy fails to read audio data.");
		}

		// Converts bytes into int16 that Snowboy will read.
		ByteBuffer.wrap(targetData).order(ByteOrder.LITTLE_ENDIAN)
				.asShortBuffer().get(snowboyData);

		// Detection.
		int result = detector.RunDetection(snowboyData, snowboyData.length);
		if (result > 0) {
			return true;
		}

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setTargetDataLine(TargetDataLine targetLine, AudioFormat format) {
		this.targetLine = targetLine;
		this.format = format;
		if (!targetLine.isOpen()) {
			try {
				targetLine.open(format);
			} catch (LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		targetLine.start();
	}

}
