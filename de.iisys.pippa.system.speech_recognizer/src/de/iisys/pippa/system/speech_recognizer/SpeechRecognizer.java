package de.iisys.pippa.system.speech_recognizer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message.speech_message.RecognizerSpeechMessage;
import de.iisys.pippa.core.message.speech_message.SpeechMessage;
import de.iisys.pippa.core.message_processor.AMessageProcessor;
import de.iisys.pippa.core.service_loader.PippaServiceLoader;
import de.iisys.pippa.core.status.StatusAccess;

public class SpeechRecognizer extends AMessageProcessor {

	private final String SOUNDFILES_PATH = System.getProperty("user.home")
			+ "/Desktop/Sound/";

	private Logger log = null;

	/**
	 * reference to the system status object, is loaded from service registry
	 */
	private StatusAccess status = null;
	
	/**
	 * reference to the bundle's context
	 */
	protected BundleContext context = null;

	private boolean isClosed = false;

	/**
	 * is set when the detector-engine hears the wakeword being uttered
	 */
	private boolean wakewordDetected = false;

	/**
	 * is set to true while an utterance is detected after the wakeword
	 */
	boolean isSpeaking = false;

	/**
	 * time that the recorder is listening after there is silence after user
	 * speech was detected before
	 */
	int pauseTime = 750;
	
	/**
	 * set to false while the pauseTime was not yet reached, set by a
	 * timer
	 */
	boolean pauseListen = false;
	
	/**
	 * timer object to let the pause time expire
	 */
	Timer pauseTimer = null;
	
	/**
	 * the maximum time in ms the recorder is listening after the wakeword was
	 * uttered
	 */
	int maximumListenTime = 5000;

	/**
	 * set to false while the maximum listen time did not expire yet, set by a
	 * timer
	 */
	boolean maximumListen = false;

	/**
	 * timer object to let the maximum listen time expire
	 */
	Timer maximumTimer = null;

	/**
	 * the minimum time in ms the recorder is listening after the wakeword was
	 * uttered or silence was detected during recording
	 */
	int minimumListenTime = 2000;

	/**
	 * threshold for the root-mean-square average silence detection
	 */
	// double thresholdSilence = 0.002;
	double thresholdSilence = 0.01;

	/**
	 * set to true while the minimum listen time did not expire yet, set by a
	 * timer
	 */
	boolean minimumListen = false;

	/**
	 * timer object to let the minimum listen time expire
	 */
	Timer minimumTimer = null;

	/**
	 * target line to get microphone access
	 */
	private TargetDataLine microphoneLine = null;

	/**
	 * sample rate to be used for recording audio
	 */
	float sampleRate = 16000;

	/**
	 * sample size in bits to be used for recording audio
	 */
	int sampleSizeInBits = 16;

	/**
	 * recording channels to be used for recording audio
	 */
	int channels = 1;

	/**
	 * flag that notes if the expected data for recording audio is signed
	 */
	boolean signed = true;

	/**
	 * flag that notes if the expected data is big-endian-style
	 */
	boolean bigEndian = false;

	/**
	 * with the used Matrix-Voice microphone, the one (of many) mixer channels
	 * that shall be used for recording has to be identified by name
	 */
	String mixerName = "default";

	/**
	 * combined attributes set for the recording line that is expected
	 */
	AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
			channels, signed, bigEndian);

	/**
	 * data-line-info object that incorporates that this line is for recording
	 * and the expected format
	 */
	DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

	/**
	 * stream used to write the recorded audio data to
	 */
	ByteArrayOutputStream out = new ByteArrayOutputStream();

	/**
	 * frame size for recording from microphone
	 */
	int frameSizeInBytes = format.getFrameSize();

	/**
	 * buffer length in frames, given by recording channel / microphone
	 */
	int bufferLengthInFrames = 0;

	/**
	 * buffer length in bytes, given by recording channel / microphone
	 */
	int bufferLengthInBytes = 0;

	/**
	 * array that data is read into from the microphone in each reading-cycle
	 */
	byte[] data = null;

	/**
	 * number of bytes read from the microphone input stream
	 */
	int numBytesRead;

	/**
	 * reference to the wake-word-detector engine
	 */
	WakeWordDetector detector = null;

	public SpeechRecognizer() {
		this.context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		this.checkAndSetDirectory(SOUNDFILES_PATH);
	}

	protected SpeechRecognizer(StatusAccess status) {
		this.status = status;
	}
	
	/**
	 * Listens for a set wake-work through a wake-word-engine. If uttered, the
	 * following user-utterance is recorded and put into a speechMessage which
	 * is then forwarded.
	 * 
	 * 
	 */
	@Override
	public void run() {

		if (this.log == null) {
			this.log = PippaServiceLoader.getLogger(this.context);
		}

		if (this.status == null) {
			this.status = PippaServiceLoader.getStatus(this.context);
			if (this.status == null) {
				this.log.severe("could not retrieve the needed Status-Object: closing Resolver");
				this.isClosed = true;
			}
		}
		
		// retrieve the expected microphone data-target-line
		this.getMicrophone();

		// set the prepared buffers, arrays, and etc with data from the
		// retrieved microphone line
		this.bufferLengthInFrames = this.microphoneLine.getBufferSize() / 8;
		this.bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
		this.data = new byte[bufferLengthInBytes];

		// start the wakeword-detector enginge
		this.detector = new SnowboyDetector(microphoneLine, format);

		this.log.fine("entering run()-loop");

		// start listening for hotwords and recording audio
		while (!this.isClosed) {

			this.log.fine("entering wakeword detection-loop");

			// listen for wakeword to be detected
			while (!this.wakewordDetected || this.status.getRecognising() == true) {

				this.wakewordDetected = this.detector.detect();

				this.isSpeaking = true;
				this.minimumListen = false;
				this.maximumListen = false;
			}

			this.log.info("wakeword detected");

			this.wakewordDetected = false;

			this.microphoneLine.start();

			// start timer for maximum record time
			this.setMaximumTimer();

			// start timer for minimum record time
			this.setMinimumTimer();
			
			// remove previously recorded sound from stream
			this.out.reset();
			
			this.log.info("entering recording-loop");

			this.status.setRecording(true);
			
			// while there is speech (!= silence) detected or the minimum listen
			// time is not expired and the maximum listen time is not expired
			while ((this.isSpeaking || !this.minimumListen || !this.pauseListen) && !this.maximumListen) {

				// read data from microphone buffer
				if ((numBytesRead = this.microphoneLine.read(data, 0,
						bufferLengthInBytes)) == -1) {
					break;
				}

				// write read data to outputstream
				out.write(data, 0, numBytesRead);

				// check if there is still noise on the inputstream
				boolean checkSpeaking = this.checkSpeaking(this.data,
						this.thresholdSilence);

				// react to edges in audio volume
				// was speaking, is not speaking anymore
				if (this.isSpeaking == true && checkSpeaking == false) {System.out.println("was speaking, not anymore");
					this.setPauseTimer();
				}

				// was not speaking but is speaking again
				else if (this.isSpeaking == false && checkSpeaking == true) {System.out.println("not speaking, now is");
					this.removePauseTimer();
				}

				this.isSpeaking = checkSpeaking;

			}
			
			this.log.info("leaving recording-loop");
			this.log.fine("is Speaking: " + this.isSpeaking
					+ " | minimum listen time reached: " + !this.minimumListen
					+ " | maximum listen time reached: " + this.maximumListen);

			this.status.setRecording(false);
			this.removeMaximumTimer();
			this.removeMinimumTimer();
			this.removePauseTimer();

			// audio recording stopped, store audio to disk
			File audioFile = this.storeAudio(this.out.toByteArray(),
					this.format, this.frameSizeInBytes);

			// if storing worked, prepare and send speechMessage to next
			// processor
			if (audioFile != null) {

				RecognizerSpeechMessage speechMessage = new SpeechMessage();
				speechMessage.setAudio(audioFile);

				try {
					this.log.info("sending message");
					this.getOutgoingQueue().put((AMessage) speechMessage);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		this.log.info("closing speech recognizer");

		// clear outputstream
		try {
			out.flush();
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// close microphone line
		this.microphoneLine.stop();
		this.microphoneLine.close();
		this.microphoneLine = null;

		return;

	}

	private void setMinimumTimer() {

		this.log.fine("setting timer for minimum listening time: "
				+ this.minimumListenTime + "ms");

		this.removeMinimumTimer();

		this.minimumTimer = new Timer();
		this.minimumTimer.schedule(new TimerTask() {
			public void run() {
				minimumListen = true;
			}
		}, this.minimumListenTime);
	}

	private void removeMinimumTimer() {
		this.log.fine("removing timer for minimum listening time");

		if (this.minimumTimer != null) {
			this.minimumTimer.cancel();
			this.minimumTimer = null;
		}
	}

	private void setPauseTimer() {

		this.log.fine("setting timer for pause between words time: "
				+ this.pauseTime + "ms");

		this.removePauseTimer();

		this.pauseTimer = new Timer();
		this.pauseTimer.schedule(new TimerTask() {
			public void run() {
				pauseListen = true;
			}
		}, this.pauseTime);
	}

	private void removePauseTimer() {
		this.log.fine("removing timer for pause between words time");

		if (this.pauseTimer != null) {
			this.pauseTimer.cancel();
			this.pauseTimer = null;
		}
	}

	private void setMaximumTimer() {
		this.log.fine("setting timer for maximum listening time: "
				+ this.maximumListenTime + "ms");

		this.removeMaximumTimer();

		this.maximumTimer = new Timer();
		this.maximumTimer.schedule(new TimerTask() {
			public void run() {
				maximumListen = true;
			}
		}, this.maximumListenTime);
	}

	private void removeMaximumTimer() {
		this.log.fine("removing timer for maximum listening time");
		if (this.maximumTimer != null) {
			this.maximumTimer.cancel();
			this.maximumTimer = null;
		}
	}

	/**
	 * Done using input from https://stackoverflow.com/a/26824664
	 * 
	 * Calculates the root-mean-square over the current data-array content and
	 * returns a boolean according to it's relation with the threshold for
	 * silence.
	 * 
	 * @return false if rms is below threshold, else true
	 */
	private boolean checkSpeaking(byte[] data, double thresholdSilence) {

		this.log.info("checking if user is speaking");

		float[] volumeSamples = new float[data.length / 2];

		// convert data-bytes to samples
		for (int i = 0, s = 0; i < data.length;) {

			int sample = 0;

			sample |= data[i++] & 0xFF;
			sample |= data[i++] << 8;

			volumeSamples[s++] = sample / 32768f;

		}

		// calculate root-mean-square
		float rms = 0;
		for (float sample : volumeSamples) {
			rms += sample * sample;
		}
		rms = (float) Math.sqrt(rms / volumeSamples.length);
		if (rms < thresholdSilence) {
			this.log.fine("not speaking");
			return false;
		} else {
			this.log.fine("is speaking");
			return true;
		}
	}

	/**
	 * Stores the audio that was recorded from the outputstream to a file in a
	 * known folder. Filename is random.
	 * 
	 * @return the created file object or null if unsuccessful
	 */
	private File storeAudio(byte audioBytes[], AudioFormat format,
			int frameSizeInBytes) {

		this.log.info("storing recorded audio to a file on disk");

		String fileName = UUID.randomUUID().toString();

		ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
		AudioInputStream audioInputStream = new AudioInputStream(bais, format,
				audioBytes.length / frameSizeInBytes);

		File audioFile = new File(SOUNDFILES_PATH + fileName + ".wav");

		try {

			AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE,
					audioFile);
			this.log.fine("filename: " + audioFile.getName());
		} catch (Exception ex) {
			this.log.severe("error storing file " + audioFile.getName()
					+ ", returning null");
			ex.printStackTrace();
			return null;
		}

		return audioFile;

	}

	/**
	 * Gets a microphone line from a mixer with a set name and the given format.
	 * This action is tailored to the demands the Matrix-Voice microphone
	 * brings.
	 */
	private void getMicrophone() {

		this.log.info("searching system mixers for microphone: "
				+ this.mixerName);

		// iterate all known mixer's info
		Mixer.Info[] systemMixers = AudioSystem.getMixerInfo();
		for (Mixer.Info mixerInfo : systemMixers) {
System.out.println(mixerInfo.toString());
			// find the one with the right name
			if (mixerInfo.getName().startsWith(this.mixerName)) {
				this.log.fine("found mixer: " + mixerInfo.getDescription());
				// get the actual mixer
				Mixer microphoneMixer = AudioSystem.getMixer(mixerInfo);

				// get a target-line (for receiving audio) from the mixer
				try {
					microphoneLine = (TargetDataLine) microphoneMixer
							.getLine(info);
					microphoneLine.open(format);
				} catch (Exception e) {
					this.log.severe("could not retrieve microphone line, this will terminate the recognizer");
					e.printStackTrace();
					this.isClosed = true;
				}

			}
		}

	}

	/**
	 * Checks if the given directory exists and creates it if not.
	 * 
	 * @param directoryPath
	 */
	private void checkAndSetDirectory(String directoryPath) {

		File directory = new File(directoryPath);

		if (!directory.exists()) {
			directory.mkdirs();
		}
	}
}
