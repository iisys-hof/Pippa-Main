package de.iisys.pippa.system.audio_storage;

import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message.speech_message.AudioStorageSpeechMessage;
import de.iisys.pippa.core.message.stop_message.StopMessage;
import de.iisys.pippa.core.message_processor.AMessageProcessor;

public class AudioStorage extends AMessageProcessor {

	private boolean isClosed = false;

	@Override
	public void run() {

		while (!this.isClosed) {

			AMessage nextMessage = null;

			try {

				nextMessage = this.getIncomingQueue().take();

				if (nextMessage != null) {

					if (nextMessage instanceof StopMessage) {
						StopMessage stopMessage = (StopMessage) nextMessage;
						if (stopMessage.isStopAndClose()) {
							this.isClosed = true;
							this.getOutgoingQueue().put(nextMessage);
							// TODO Stopping some operations?
						}
					}

					else if (nextMessage instanceof AudioStorageSpeechMessage) {
						AudioStorageSpeechMessage speechMessage = (AudioStorageSpeechMessage) nextMessage;

						System.out.println("Audiostorage Received AudioStorageSpeechMessage");

						// Get SoundFile from SpeechMessage

						// remove SoundFile from SpeechMessage

						// TODO some real File-with-URL- replacement
						// add EXPECTED URL of SoundFile to SpeechMessage
						speechMessage.setAudioUrl("/home/user/Desktop/AudioFiles/someuniquename.wav");

						// put enriched SpeechMessage into next Queue
						this.getOutgoingQueue().put((AMessage) speechMessage);

						// store SoundFile on hard-disk
						// this is done after passing the message since it
						// potentially takes a longer time

					}

					else {
						// The received message-type is of no interest for this class, just pass it
						this.getOutgoingQueue().put(nextMessage);
					}

				}
			} catch (InterruptedException e) {
				// TODO Catch probable Exception
			}

		}

		// TODO some cleaning up before returning

		return;

	}

}
