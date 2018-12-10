package de.iisys.pippa.system.speech_analyser.STT.DeepSpeech;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeepSpeechStreamObserver implements Runnable {

	/**
	 * Input to be watched
	 */
	BufferedReader reader = null;

	/**
	 * Entity to recall after a user utterance was found
	 */
	DeepSpeechSTT recall = null;

	/**
	 * Pattern that the utterance can be found inside
	 */
	Pattern pattern = null;

	/**
	 * denotes whether this thread should be closed/closing
	 */
	boolean isClosed = false;

	public DeepSpeechStreamObserver(BufferedReader reader, DeepSpeechSTT recall,
			Pattern pattern) {
		this.reader = reader;
		this.recall = recall;
		this.pattern = pattern;
	}

	/**
	 * Continuously reads from the given BufferedReader and matches the lines for
	 * the given pattern. If matched, extracts the found user utterance and
	 * returns it via a recall, then keeps reading and matching.
	 */
	@Override
	public void run() {

		String line = null;

		try {
			while ((line = reader.readLine()) != null && !isClosed) {

				this.makeRecall(line);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void makeRecall(String utterance) {
		this.recall.recall(utterance);
	}

}
