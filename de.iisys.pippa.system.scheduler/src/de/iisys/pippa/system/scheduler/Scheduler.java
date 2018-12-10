package de.iisys.pippa.system.scheduler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message.feedack_message.FeedbackMessage;
import de.iisys.pippa.core.message.feedack_message.FeedbackMessageWriter;
import de.iisys.pippa.core.message.speech_message.SchedulerSpeechMessage;
import de.iisys.pippa.core.message.stop_message.StopMessage;
import de.iisys.pippa.core.message.unschedule_message.UnscheduleMessage;
import de.iisys.pippa.core.message_processor.AMessageProcessor;

public class Scheduler extends AMessageProcessor {

	private static final Logger log = Logger.getLogger(Scheduler.class.getName());

	private boolean isClosed = false;
	HashMap<AMessage, Timer> timers = new HashMap<AMessage, Timer>();

	private AMessageProcessor skillRegistry = null;

	protected BlockingQueue<AMessage> feedbackQueue = null;

	public Scheduler() {
	}

	private void getSkillRegistry() {

		if (this.skillRegistry == null && this.feedbackQueue == null) {

			BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

			Collection<ServiceReference<AMessageProcessor>> serviceReferencesStatus = null;

			try {
				// get all service references registered under the given name (which should be
				// 1)
				serviceReferencesStatus = context.getServiceReferences(AMessageProcessor.class, "(name=SkillRegistry)");

				// get actual object from reference
				AMessageProcessor service = context
						.getService(((List<ServiceReference<AMessageProcessor>>) serviceReferencesStatus).get(0));

				this.skillRegistry = service;

				this.feedbackQueue = this.skillRegistry.getIncomingQueue();

			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void run() {

		this.getSkillRegistry();

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

					else if (nextMessage instanceof SchedulerSpeechMessage) {

						SchedulerSpeechMessage speechMessage = (SchedulerSpeechMessage) nextMessage;

						log.info("Scheduler Received SchedulerSpeechMessage");

						// if request for future execution and still in time
						if (speechMessage.getFutureExecutionRequest()
								&& speechMessage.getExecutionDate().isAfter(LocalDateTime.now())) {

							log.info("scheduling message");

							Timer timer = new Timer();
							timer.schedule(
									new SchedulerTimer(this.getOutgoingQueue(), this.timers, (AMessage) speechMessage,
											Scheduler.log),
									Date.from(speechMessage.getExecutionDate().atZone(ZoneId.systemDefault())
											.toInstant()));

							this.timers.put((AMessage) speechMessage, timer);

							FeedbackMessageWriter feedbackMessage = new FeedbackMessage(nextMessage);
							feedbackMessage.setIsScheduled(true);

							this.feedbackQueue.put((AMessage) feedbackMessage);

						}

						// return to resolver
						else {
							log.info("returning message without prior scheduling of message");
							this.getOutgoingQueue().put((AMessage) speechMessage);

							FeedbackMessageWriter feedbackMessage = new FeedbackMessage(nextMessage);
							feedbackMessage.setIsScheduled(false);

							this.feedbackQueue.put((AMessage) feedbackMessage);
						}

					}

					else if (nextMessage instanceof UnscheduleMessage) {

						UnscheduleMessage unscheduleMessage = (UnscheduleMessage) nextMessage;

						for (AMessage message : this.timers.keySet()) {

							if (message.getMessageId() == unscheduleMessage.getUnscheduleMessageId()) {
								this.timers.get(message).cancel();
								this.timers.remove(message);

								FeedbackMessageWriter feedbackMessage = new FeedbackMessage(message);
								feedbackMessage.setIsUnscheduled(true);

								this.feedbackQueue.put((AMessage) feedbackMessage);

								break;
							}
						}

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

class SchedulerTimer extends TimerTask {

	HashMap<AMessage, Timer> timers = null;
	BlockingQueue<AMessage> outgoingQueue = null;
	AMessage message = null;
	Logger log = null;

	SchedulerTimer(BlockingQueue<AMessage> outgoingQueue, HashMap<AMessage, Timer> timers, AMessage message,
			Logger log) {
		this.outgoingQueue = outgoingQueue;
		this.message = message;
		this.log = log;
		this.timers = timers;
	}

	@Override
	public void run() {
		try {
			log.info("timer over, returning message to resolver");
			this.outgoingQueue.put(message);
			this.timers.remove(message);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
