package de.iisys.pippa.system.message_storage;

import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message.speech_message.MessageStorageSpeechMessage;
import de.iisys.pippa.core.message.stop_message.StopMessage;
import de.iisys.pippa.core.message_processor.AMessageProcessor;

public class MessageStorage extends AMessageProcessor {

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
						}
					}

					else if (nextMessage instanceof MessageStorageSpeechMessage) {
						MessageStorageSpeechMessage speechMessage = (MessageStorageSpeechMessage) nextMessage;

						System.out.println("MessageStorage Received MessageStorageSpeechMessage");

						this.getOutgoingQueue().put((AMessage) speechMessage);
					}

				}
			} catch (InterruptedException e) {
				// TODO
			}

		}

		// some cleaning up before returning

		return;

	}

}
