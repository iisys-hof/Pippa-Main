package de.iisys.pippa.system.speech_analyser.STT.DeepSpeech;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.iisys.pippa.core.speech_analyser.STT;

public class DeepSpeechSTT implements STT {

	/**
	 * Maximum time the speech recognition may take before it is timed out.
	 */
	private int conversionTimeoutInMillis = 30000;

	/**
	 * Command used to create a sub-process running the Julius-STT engine. Will
	 * take it's speech-recognition-configuration from juliusFile.jconf and wait
	 * for a file-path to be input through it's stdin.
	 */
	String emptyCommand[] = {
			"bash",
			"-c",
			"source /home/pippa/deepspeech-venv/bin/activate && "
			+ "deepspeech "
			+ "--model /home/pippa/models/output_graph.pbmm "
			+ "--alphabet /home/pippa/models/alphabet.txt "
			+ "--lm /home/pippa/models/lm.binary "
			+ "--trie /home/pippa/models/trie "
			+ "--audio ",
			" && deactivate"
	};

	/**
	 * Pattern string to filter out the returned recognized speech from all the
	 * stuff that is returned by the subprocess.
	 */
	final String pattern1 = "Running inference.";

	/**
	 * Pattern to filter out the returned recognized speech from all the stuff
	 * that is returned by the subprocess.
	 */
	final Pattern p = Pattern.compile(pattern1);

	/**
	 * Process to run the Julius speech recognizer
	 */
	Process deepspeechProcess = null;

	/**
	 * Stdin of the created subprocess, used to communicate towards the
	 * subprocess.
	 */
	OutputStream dOut = null;

	/**
	 * Stdout of the created subprocess, used to read the output of the
	 * subprocess.
	 */
	InputStream dIn = null;

	/**
	 * Stderr of the created subprocess, used to read the error-output of the
	 * subprocess.
	 */
	InputStream dErr = null;

	/**
	 * Used to print the sound-file-path towards the subprocess.
	 */
	PrintWriter deepspeechOut = null;

	/**
	 * Used to read the returned input from the subprocess.
	 */
	BufferedReader deepspeechIn = null;

	/**
	 * Used to read the returned errors from the subprocess.
	 */
	BufferedReader deepspeechErr = null;

	/**
	 * Is true while the Julius speech recognition is busy.
	 */
	boolean isRecognizing = false;

	/**
	 * The subprocess output is read continuously an in parallel during
	 * recognition.
	 */
	Thread streamObserverThread = null;

	/**
	 * The subprocess output is read continuously an in parallel during
	 * recognition.
	 */
	DeepSpeechStreamObserver streamObserver = null;

	/**
	 * Storage for the utterance that was delivered by Julius and is to be
	 * returned via a recall.
	 */
	String utterance = null;

	/**
	 * Timer object to watch the timeout of the speech recognition.
	 */
	private Timer conversionTimer;

	/**
	 * Flag that denotes that a timeout occurred during recognition.
	 */
	protected boolean conversionTimeout = false;

	public DeepSpeechSTT() {

	}

	/**
	 * Can be called from the outside to start speech-recognition on a given
	 * file. Will return null if speech recognition is timed out or recognition
	 * is already busy during call. Will start recognition and block to only
	 * check periodically if the recognition was finished.
	 */
	@Override
	public String recognize(File audioFile) {

		// only start working if no recognition is in progress yet
		if (!this.isRecognizing) {

			// (re)set variables
			this.utterance = null;
			this.isRecognizing = true;

			// start timer that watches for timeout
			this.setConversionTimer();

		
			String[] command = this.emptyCommand.clone();
			command[command.length-2] = command[command.length-2] + audioFile.getAbsolutePath();
			
			// create new speech recognition subprocess
			ProcessBuilder pb = new ProcessBuilder(command);

			// start process and link std channels for later use
			try {
				this.deepspeechProcess = pb.start();

				this.dIn = this.deepspeechProcess.getInputStream();
				this.deepspeechIn = new BufferedReader(new InputStreamReader(dIn));

				this.dOut = this.deepspeechProcess.getOutputStream();
				this.deepspeechOut = new PrintWriter(new OutputStreamWriter(dOut), true);

				this.dErr = this.deepspeechProcess.getErrorStream();
				this.deepspeechErr = new BufferedReader(new InputStreamReader(dErr));


			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// create an observer for the stdout of the subprocess
			this.createStreamObserver();


			// wait for the conversion to finish as long as it is working and
			// not timed out, isRecognizing is set by recall() which is call by
			// StreamObserver which waits for a user utterance to be returned by
			// Julius
			// TODO find a better way than polling
			while (this.isRecognizing && !this.conversionTimeout) {
				try {
					Thread.sleep(100);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// if recognition was stopped through a timeout
			if (this.conversionTimeout) {

			}

			// remove timeout-timer
			this.removeConversionTimer();

			// return user utterance or null
			return this.utterance;

		}

		return null;
	}

	/**
	 * Creates and starts a new StreamObserver in a Thread that will watch the
	 * speech-recognition sub-process and recall on every recognized utterance
	 * that was delivered by the recognition subprocess.
	 */
	private void createStreamObserver() {

		this.streamObserver = new DeepSpeechStreamObserver(this.deepspeechIn, this,
				this.p);
		this.streamObserverThread = new Thread(this.streamObserver);
		this.streamObserverThread.start();

	}

	/**
	 * Is called by the StreamObserver to deliver the lates recognized
	 * user-utterance.
	 * 
	 * @param utterance
	 */
	void recall(String utterance) {

		if (isRecognizing) {
			this.utterance = utterance;
			this.isRecognizing = false;
		}
	}

	private void setConversionTimer() {
		// this.log.fine("setting timer for maximum listening time: "
		// + this.maximumListenTime + "ms");

		this.removeConversionTimer();

		this.conversionTimer = new Timer();
		this.conversionTimer.schedule(new TimerTask() {
			public void run() {
				conversionTimeout = true;
			}
		}, this.conversionTimeoutInMillis);
	}

	private void removeConversionTimer() {
		// this.log.fine("removing timer for maximum listening time");
		if (this.conversionTimer != null) {
			this.conversionTimer.cancel();
			this.conversionTimer = null;
			this.conversionTimeout = false;
		}
	}
}