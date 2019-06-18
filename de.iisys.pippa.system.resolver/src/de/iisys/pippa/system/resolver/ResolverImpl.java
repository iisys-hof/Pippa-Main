package de.iisys.pippa.system.resolver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message.feedack_message.FeedbackMessage;
import de.iisys.pippa.core.message.feedack_message.FeedbackMessageWriter;
import de.iisys.pippa.core.message.speech_message.ResolverSpeechMessage;
import de.iisys.pippa.core.message.stop_message.StopMessage;
import de.iisys.pippa.core.message.unschedule_message.UnscheduleMessage;
import de.iisys.pippa.core.message_processor.AMessageProcessor;
import de.iisys.pippa.core.resolver.Resolver;
import de.iisys.pippa.core.service_loader.PippaServiceLoader;
import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.status.StatusAccess;

public class ResolverImpl extends AMessageProcessor implements Resolver {

	private Logger log = null;

	/**
	 * flag that signals whether this runnable should return and end
	 */
	protected boolean isClosed = false;

	/**
	 * reference to the system status object, is loaded from service registry
	 */
	private StatusAccess status = null;

	/**
	 * reference to a timer object that causes the active dialog to quit after a
	 * certain time of inactivity ##not yet implemented##
	 */
	@SuppressWarnings("unused")
	private Timer dialogTimeout = null;

	/**
	 * list of messages that set a presenceRequest, so they are kept here until the
	 * system registers that the user is present, then send them out
	 */
	protected ArrayList<ResolverSpeechMessage> waitingPresenceMessages = new ArrayList<ResolverSpeechMessage>();

	/**
	 * map to keep track of messages that were dispatched to multiple skills, only
	 * when all have sent their message to the resolver, they are evaluated
	 */
	LinkedHashMap<UUID, ArrayList<ResolverSpeechMessage>> multipleSendersMap = new LinkedHashMap<UUID, ArrayList<ResolverSpeechMessage>>();

	private BlockingQueue<AMessage> dispatcherQueue = new LinkedBlockingQueue<AMessage>();
	private BlockingQueue<AMessage> schedulerQueue = new LinkedBlockingQueue<AMessage>();
	private BlockingQueue<AMessage> skillRegistryQueue = new LinkedBlockingQueue<AMessage>();
	private BlockingQueue<AMessage> executorQueue = new LinkedBlockingQueue<AMessage>();

	private BundleContext context = null;

	/**
	 * only used for testing
	 * 
	 * @param status
	 */
	protected ResolverImpl(StatusAccess status) {
		this.status = status;
	}

	public ResolverImpl() {

		this.context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

	}

	@Override
	public BlockingQueue<AMessage> getDispatcherQueue() {
		return this.dispatcherQueue;
	}

	@Override
	public BlockingQueue<AMessage> getSchedulerQueue() {
		return this.schedulerQueue;
	}

	@Override
	public BlockingQueue<AMessage> getExecutorQueue() {
		return this.executorQueue;
	}

	@Override
	public BlockingQueue<AMessage> getSkillRegistryQueue() {
		return this.skillRegistryQueue;
	}

	@Override
	public void setDispatcherQueue(BlockingQueue<AMessage> dispatcherQueue) {
		this.dispatcherQueue = dispatcherQueue;
	}

	@Override
	public void setSchedulerQueue(BlockingQueue<AMessage> schedulerQueue) {
		this.schedulerQueue = schedulerQueue;
	}

	@Override
	public void setExecutorQueue(BlockingQueue<AMessage> executorQueue) {
		this.executorQueue = executorQueue;
	}

	@Override
	public void setSkillRegistryQueue(BlockingQueue<AMessage> skillRegistryQueue) {
		this.skillRegistryQueue = skillRegistryQueue;
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

		this.log.fine("entering run()-loop");

		while (!this.isClosed) {

			// reset temporary variables
			AMessage nextMessage = null;

			try {

				// waits for messages here
				nextMessage = this.getIncomingQueue().take();

				if (nextMessage != null) {

					this.log.info("received message");

					if (nextMessage instanceof StopMessage) {
						this.handleStopMessage(nextMessage);
					}

					else if (nextMessage instanceof ResolverSpeechMessage) {
						this.handleResolverSpeechMessage(nextMessage);
					}

					else if (nextMessage instanceof UnscheduleMessage) {
						this.handleUnscheduleMessage(nextMessage);
					}

					else {
						this.log.fine("message of unmeant kind, forwarding");
						this.executorQueue.put(nextMessage);
					}

				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		this.log.fine("left run()-loop");
		// some cleaning up before returning

		return;

	}

	/**
	 * Checks whether the incoming message is a single message or belongs to a set
	 * of messages that were dispatched to multiple skills. Forwards the message to
	 * one of two methods accordingly.
	 * 
	 * @param nextMessage
	 */
	private void handleResolverSpeechMessage(AMessage nextMessage) {

		this.log.info("handling SpeechMessage - " + nextMessage.getMessageId());

		ResolverSpeechMessage speechMessage = (ResolverSpeechMessage) nextMessage;

		// this message is a copy of multiple messages that were sent to multiple skills
		if (speechMessage.getDispatchedSkills().length > 1) {
			this.log.fine("message belongs to a set of multiple messages");
			this.handleMultipleMessages(speechMessage);

		}

		// this message is a single message that was sent to a single skill
		else {
			this.log.fine("message is a single message");
			this.handleSingleMessage(speechMessage);
		}

	}

	/**
	 * Receives messages that belong to a set of messages that were dispatched to
	 * multiple skills. If the set is incomplete the message is stored until all
	 * messages arrived. If the set is complete, the messages are evaluated by their
	 * confidence-level and only the one with the highest confidence is forwarded to
	 * be executed.
	 * 
	 * @param speechMessage
	 */
	private void handleMultipleMessages(ResolverSpeechMessage speechMessage) {

		this.log.info("handling one of a set of multiple messages - " + speechMessage.getMessageId());

		// none of the multiple messages were received yet
		if (!this.multipleSendersMap.containsKey(speechMessage.getMessageId())) {

			this.log.fine("first message of it's set - " + speechMessage.getMessageId() + " - 1/"
					+ speechMessage.getDispatchedSkills().length);

			// store this message as a set of multiple message, grouped by their common
			// message-id
			ArrayList<ResolverSpeechMessage> messageList = new ArrayList<ResolverSpeechMessage>();
			messageList.add(speechMessage);
			this.multipleSendersMap.put(speechMessage.getMessageId(), messageList);

		}

		// another message of this set of was received before
		else {

			ArrayList<ResolverSpeechMessage> messageList = this.multipleSendersMap.get(speechMessage.getMessageId());
			if (messageList.contains(speechMessage)) {
				// TODO message was already received once, this really shouldn't be possible..
			} else {
				messageList.add(speechMessage);
			}

			this.log.fine("another message of it's set - " + speechMessage.getMessageId() + " - " + messageList.size()
					+ "/" + speechMessage.getDispatchedSkills().length);

			// if all messages of the set were received, find the one with the highest
			// confidence and forward it
			if (messageList.size() == speechMessage.getDispatchedSkills().length) {

				ResolverSpeechMessage highestConfidenceMessage = null;

				for (ResolverSpeechMessage confidenceMessage : messageList) {
					if (highestConfidenceMessage == null) {
						highestConfidenceMessage = confidenceMessage;
					}

					else {

						if (highestConfidenceMessage.getConfidence() == confidenceMessage.getConfidence()) {
							// TODO choose by another method, that delivers consistent results
						}

						else if (confidenceMessage.getConfidence() > highestConfidenceMessage.getConfidence()) {
							highestConfidenceMessage = confidenceMessage;
						}
					}
				}

				this.log.info("message with highest confidence - "
						+ highestConfidenceMessage.getSkillRef().getClass().getSimpleName() + " - "
						+ highestConfidenceMessage.getMessageId());

				// step through all messages of this set again
				for (ResolverSpeechMessage message : messageList) {

					// send all rejected messages a feedback message
					if (message != highestConfidenceMessage) {
						this.sendFeedbackMessage(message, true, false, false, false, false);
					}

					// work on chosen message by giving it to the single-message-method
					else {
						this.handleSingleMessage(speechMessage);
					}

				}

				// remove set from storage
				this.multipleSendersMap.remove(speechMessage.getMessageId());

			}
		}

	}

	/**
	 * Receives a single message that was either sent from a single skill or was
	 * singled out from set of multiple messages by the multiple-message-method
	 * before. Checks if the message is to be executed somewhere in the future, if
	 * the message shall wait for the user to be present before being executed or if
	 * it can be executed at once. It then either sends it to the scheduler, stores
	 * it in a list of waiting messages or forwards it for execution.
	 * 
	 * @param speechMessage
	 */
	private void handleSingleMessage(ResolverSpeechMessage speechMessage) {

		this.log.info("handle single message - " + speechMessage.getMessageId());

		// if the message is supposed to be returned to the dispatcher
		if (speechMessage.getReturnToDispatcher() == true) {
			this.log.fine("message will be returned to dispatcher - " + speechMessage.getMessageId());
			this.returnMessage((AMessage) speechMessage);
		} else {

			// if the message has no prohibited combination of properties
			if (this.checkMessage(speechMessage)) {

				// message shall be scheduled and the future execution time has not yet passed
				if (speechMessage.getFutureExecutionRequest()
						&& speechMessage.getExecutionDate().isAfter(LocalDateTime.now())) {
					try {
						this.log.fine("message is scheduled - " + speechMessage.getMessageId());
						this.schedulerQueue.put((AMessage) speechMessage);
						this.sendFeedbackMessage(speechMessage, false, false, true, false, false);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}

				// message shall wait for presence
				else if (speechMessage.getPresenceRequest()) {
					this.log.fine("message is waiting for presence - " + speechMessage.getMessageId());
					this.waitingPresenceMessages.add(speechMessage);
					this.sendFeedbackMessage(speechMessage, false, true, false, false, false);
				}

				// message is not to be scheduled and not to be waiting for presence
				else {

					this.log.fine("message is executed - " + speechMessage.getMessageId());
					this.sendMessage((AMessage) speechMessage);

					// message was not returned from scheduler, it must have been created manually
					// -> user is present --> append messages that are waiting for presence
					if (!speechMessage.getFutureExecutionRequest()) {
						this.log.fine("user is present, send all presence-waiting messages");
						for (ResolverSpeechMessage message : this.waitingPresenceMessages) {
							this.log.fine("sending message - " + message.getMessageId());
							this.sendMessage((AMessage) message);
						}
						this.waitingPresenceMessages.clear();
					}
				}
			}

			// if the message has a prohibited combination of properties
			else {
				// it is discarded by disregard and the sender is given feedback about the
				// rejection
				this.log.fine("message is rejected due to prohibited combination of properties - "
						+ speechMessage.getMessageId());
				this.sendFeedbackMessage(speechMessage, true, false, false, false, false);

			}
		}
	}

	/**
	 * Checks whether the message is OK according to predetermined requirements.
	 * 
	 * Dialog-Stop-Requests may only be sent by the skill currently having the
	 * dialog.
	 * 
	 * Dialog-Stop-Requests may only be sent if there is a Dialog.
	 * 
	 * Presence- and Future-Execution-Requests may not contain Long-Running
	 * executables or Dialog-Requests of any kind.
	 * 
	 * @param speechMessage
	 * @return false if the message has errors, true if the message is ok
	 */
	private boolean checkMessage(ResolverSpeechMessage speechMessage) {

		// the message wants to stop the current dialog
		if (speechMessage.getStopDialogRequest()) {
			if (this.status.getDialogRunning()) {
				if (this.status.getDialogWith() != speechMessage.getSkillRef()) {
					return false;
				}
			} else {
				return false;
			}
		}

		else if (speechMessage.getPresenceRequest() || speechMessage.getFutureExecutionRequest()) {
			if (speechMessage.getLongRunning() || speechMessage.getStartDialogRequest()
					|| speechMessage.getStopDialogRequest()) {
				return false;
			}
		}

		return true;
	}

	private void sendMessage(AMessage message) {
		try {
			this.executorQueue.put(message);
			this.setStatus((ResolverSpeechMessage) message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private void returnMessage(AMessage message) {
		try {
			this.dispatcherQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void setStatus(ResolverSpeechMessage message) {

		this.log.info("setting status according to message- " + message.getMessageId());

		this.log.fine("last resolved skill " + message.getSkillRef());

		// set last skill that was resolved to be executed
		status.setLastResolvedSkill(message.getSkillRef());

		// if there is a dialog already running and it's not with the current message's
		// sender
		if (this.status.getDialogRunning() && this.status.getDialogWith() != message.getSkillRef()) {

			// and if the new message wants to start a dialog too
			if (message.getStartDialogRequest()) {

				// tell current dialog skill about the end of the dialog
				this.sendFeedbackMessage(this.status.getDialogWith(), false, false, false, false, true);

				// end current dialog by setting the status
				this.status.setDialogRunning(false);
				this.status.setDialogWith(null);

				this.log.fine("ended running dialog to start it's own");
			}

			// and the new message sends a long running executable
			else if (message.getLongRunning()) {

				this.sendFeedbackMessage(this.status.getDialogWith(), false, false, false, false, true);
				this.status.setDialogRunning(false);
				this.status.setDialogWith(null);

				this.log.fine("ended running dialog because message contains long running executable");
			}
		}

		// if there is no dialog running and the message requests to start a dialog
		if (message.getStartDialogRequest()) {
			this.status.setDialogRunning(true);
			this.status.setDialogWith(message.getSkillRef());
			this.sendFeedbackMessage(message, false, false, false, true, false);

			this.log.fine("started new dialog");
		}

		// if the message requests to stop the current dialog
		if (message.getStopDialogRequest()) {
			// and the requesting skill is the one currently having the dialog
			if (this.status.getDialogWith() == message.getSkillRef()) {
				this.status.setDialogRunning(false);
				this.status.setDialogWith(null);
				this.sendFeedbackMessage(message, false, false, false, false, true);

				this.log.fine("skill ended it's own dialog");
			}
		}

	}

	private void sendFeedbackMessage(Skill skill, boolean isRejected, boolean isWaiting, boolean isScheduled,
			boolean startedDialog, boolean endedDialog) {

		this.log.info("sending feedback message - " + skill.getClass().getSimpleName());

		FeedbackMessageWriter feedback = new FeedbackMessage(skill);
		feedback.setIsRejected(isRejected);
		feedback.setIsWaiting(isWaiting);
		feedback.setIsScheduled(isScheduled);
		feedback.setStartedDialog(startedDialog);
		feedback.setEndedDialog(endedDialog);

		try {
			this.skillRegistryQueue.put((AMessage) feedback);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private void sendFeedbackMessage(ResolverSpeechMessage message, boolean isRejected, boolean isWaiting,
			boolean isScheduled, boolean startedDialog, boolean endedDialog) {

		this.log.info("sending feedback message - " + message.getSkillRef().getClass().getSimpleName());

		FeedbackMessageWriter feedback = new FeedbackMessage((AMessage) message);
		feedback.setIsRejected(isRejected);
		feedback.setIsWaiting(isWaiting);
		feedback.setIsScheduled(isScheduled);
		feedback.setStartedDialog(startedDialog);
		feedback.setEndedDialog(endedDialog);

		try {
			this.skillRegistryQueue.put((AMessage) feedback);
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

		this.log.info("handle StopMessage - " + nextMessage.getMessageId());

		StopMessage stopMessage = (StopMessage) nextMessage;

		if (stopMessage.isStopAndClose()) {
			this.log.fine("message is stopAndClose");
			this.isClosed = true;
		}

		try {
			this.executorQueue.put(stopMessage);
			this.schedulerQueue.put(stopMessage);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private void handleUnscheduleMessage(AMessage nextMessage) {

		this.log.info("handle UnscheduleMessage - " + nextMessage.getMessageId());

		UnscheduleMessage unscheduleMessage = (UnscheduleMessage) nextMessage;

		try {
			this.schedulerQueue.put(unscheduleMessage);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
