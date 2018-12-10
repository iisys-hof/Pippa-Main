package de.iisys.pippa.system.speech_analyser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import de.iisys.pippa.core.speech_analyser.STT;
import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message.speech_message.AnalyserSpeechMessage;
import de.iisys.pippa.core.message.speech_message.ResolverSpeechMessage;
import de.iisys.pippa.core.message.speech_message.SpeechMessage;
import de.iisys.pippa.core.message.stop_message.StopMessage;
import de.iisys.pippa.core.message_processor.AMessageProcessor;
import de.iisys.pippa.core.service_loader.PippaServiceLoader;
import de.iisys.pippa.core.status.StatusAccess;
import de.iisys.pippa.system.speech_analyser.STT.DeepSpeech.DeepSpeechSTT;
import de.iisys.pippa.system.speech_analyser.STT.Julius.JuliusSTT;

public class SpeechAnalyser extends AMessageProcessor {

	public STT speechToText = null;

	private Logger log = null;

	/**
	 * reference to the system's status object
	 */
	protected StatusAccess status = null;

	/**
	 * reference to the bundle's context
	 */
	protected BundleContext context = null;

	private boolean isClosed = false;

	public SpeechAnalyser() {
		this.context = FrameworkUtil.getBundle(this.getClass())
				.getBundleContext();
	}

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

		try {

			this.speechToText = new JuliusSTT();

		} catch (Exception e) {
			e.printStackTrace();
		}

		while (!this.isClosed) {

			/*
			 * for testing purposes this class now opens an inputstream to the
			 * console which allows users to enter utterances in textform as
			 * they would be returned from the speech-analyser
			 */

			/*BufferedReader br = null;
			br = new BufferedReader(new InputStreamReader(System.in));
			String input = null;
			try {
				input = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (input != null && input != "") {

				SpeechMessage speechMessage = new SpeechMessage();

				speechMessage.setAudioText(input);

				try {
					this.getOutgoingQueue().put(speechMessage);
				} catch (InterruptedException e) { // TODO Auto-generated catch
													// block
					e.printStackTrace();
				}

			}*/

			this.log.fine("entering run()-loop");


				// reset temporary variables
				AMessage nextMessage = null;

				try {

					// analyser will wait here for the next incoming message
					nextMessage = this.getIncomingQueue().take();

					if (nextMessage != null) {

						// this type of message will be forwarded and eventually
						// stop the
						// dispatcher-thread
						if (nextMessage instanceof StopMessage) {
							this.handleStopMessage(nextMessage);
						}

						// this will cause some action inside the analyser
						else if (nextMessage instanceof AnalyserSpeechMessage) {
							this.handleAnalyserSpeechMessage(nextMessage);
						}

						// The received message-type is of no interest for this
						// class, just forward it
						else {
							this.log.fine("received message of unmeant kind, forwarding");
							this.getOutgoingQueue().put(nextMessage);
						}

					}
				} catch (InterruptedException e) {
					// TODO Catch probable Exception
				}

			

		}

		this.log.fine("left run()-loop");
		// TODO some cleaning up before returning

		return;

	}

	/**
	 * 
	 * @param nextMessage
	 */
	private void handleAnalyserSpeechMessage(AMessage nextMessage) {

		AnalyserSpeechMessage speechMessage = (AnalyserSpeechMessage) nextMessage;

		this.log.info("handling SpeechMessage - " + nextMessage.getMessageId());

		File audioFile = speechMessage.getAudio();

		if (audioFile != null) {

			this.log.fine("starting speech recognition from file: "
					+ audioFile.getAbsolutePath());

			this.status.setRecognising(true);

			String transcript = this.speechToText.recognize(audioFile);

			this.status.setRecognising(false);

			this.log.fine("finished speech recognition with transcript: "
					+ transcript);

			this.sendMessage(speechMessage, transcript);

		} else {
			// TODO
		}

	}

	private void sendMessage(AnalyserSpeechMessage message, String transcript) {

		this.log.info("sending message with transcript");

		message.setAudioText(transcript);

		try {
			this.getOutgoingQueue().put((AMessage) message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Called when a stop-message was received in the incoming queue. Checks if the
	 * message contains a call for closing down the Runnable and acts accordingly.
	 * Then forwards the message.
	 * 
	 * @param nextMessage
	 */
	private void handleStopMessage(AMessage nextMessage) {

		StopMessage stopMessage = (StopMessage) nextMessage;

		if (stopMessage.isStopAndClose()) {
			this.isClosed = true;
			this.log.info("received and forwarding stop-and-close-message");
		} else {
			this.log.info("received and forwarding stop-message");
		}

		this.status.setDialogRunning(false);
		this.status.setDialogWith(null);

		try {
			this.getOutgoingQueue().put(nextMessage);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
