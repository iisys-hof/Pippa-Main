package de.iisys.pippa.system.executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import de.iisys.pippa.core.executor.Executor;
import de.iisys.pippa.core.logger.PippaLogger;
import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message.executable_end_message.ExecutableEndMessage;
import de.iisys.pippa.core.message.feedack_message.FeedbackMessage;
import de.iisys.pippa.core.message.feedack_message.FeedbackMessageWriter;
import de.iisys.pippa.core.message.speech_message.ExecutorSpeechMessage;
import de.iisys.pippa.core.message.stop_message.StopMessage;
import de.iisys.pippa.core.message_processor.AMessageProcessor;
import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.skill_executable.ASkillExecutable;
import de.iisys.pippa.core.speech_out.SpeechOutListener;
import de.iisys.pippa.core.speech_out.SpeechOutSystem;
import de.iisys.pippa.core.status.StatusAccess;

public class ExecutorImpl extends AMessageProcessor implements Executor {

	private Logger log = null;

	/**
	 * flag that signals whether this runnable should return and end
	 */
	private boolean isClosed = false;

	/**
	 * reference to the system status object, is loaded from service registry
	 */
	private StatusAccess status = null;

	/**
	 * reference to the system speech out service, is loaded from service registry,
	 * is set up with the currently executed executables to keep control of the
	 * access to the speechout system
	 */
	private SpeechOutSystem speechOut = null;

	/**
	 * list of short-messages that are waiting to be executed one after another
	 */
	ArrayList<ExecutorSpeechMessage> waitingShortMessages = new ArrayList<ExecutorSpeechMessage>();

	/**
	 * waiting place for a single waiting long-message, which is waiting for the
	 * currently waiting short-message to end execution
	 */
	ExecutorSpeechMessage waitingLongMessage = null;

	/**
	 * running place for the currently running short-executable
	 */
	Thread shortThread = null;

	/**
	 * running place for the currently running long-executable
	 */
	Thread longThread = null;

	/**
	 * reference to the skill which belongs to the currently running
	 * short-executable
	 */
	Skill shortSkill = null;

	/**
	 * reference to the skill which belongs to the currently running long-executable
	 */
	Skill longSkill = null;

	/**
	 * reference to the currently running short-executable
	 */
	ASkillExecutable shortExecutable = null;

	/**
	 * reference to the currently running long-executable
	 */
	ASkillExecutable longExecutable = null;

	/**
	 * message queue towards the skill registry, used for sending feedback-messages
	 */
	private BlockingQueue<AMessage> skillRegistryQueue = new LinkedBlockingQueue<AMessage>();

	private BundleContext context = null;

	/**
	 * only to use for testing
	 * 
	 * @param speechOut
	 * @param status
	 * @param log
	 */
	public ExecutorImpl(SpeechOutSystem speechOut, StatusAccess status, Logger log) {
		this.speechOut = speechOut;
		this.status = status;
		this.log = log;
	}

	public ExecutorImpl() {
	}

	@Override
	public void run() {

		this.getLogger();
		this.getSpeechOut();
		this.getStatus();

		this.log.fine("entering run()-loop");

		while (!this.isClosed) {

			AMessage nextMessage = null;

			try {

				nextMessage = this.getIncomingQueue().take();

				if (nextMessage != null) {

					this.log.info("received message");

					if (nextMessage instanceof ExecutableEndMessage) {
						this.handleEndMessage(nextMessage);
					}

					else if (nextMessage instanceof ExecutorSpeechMessage) {
						this.handleExecutorSpeechMessage(nextMessage);
					}

					else if (nextMessage instanceof StopMessage) {
						this.handleStopMessage(nextMessage);
					}

					else {
						this.log.fine("message of unmeant kind, forwarding");
						this.getOutgoingQueue().put(nextMessage);
					}

				}
			} catch (InterruptedException e) {
				// TODO
			}

		}

		// some cleaning up before returning

		this.log.fine("left run()-loop");

		return;

	}

	/**
	 * Checks whether the incoming message is a short- or long-running message.
	 * Depending on whether there are other executables already running the messages
	 * are laid down for later execution or executed directly.
	 * 
	 * @param nextMessage
	 */
	private void handleExecutorSpeechMessage(AMessage nextMessage) {

		ExecutorSpeechMessage speechMessage = (ExecutorSpeechMessage) nextMessage;

		this.log.info("received ExecutorSpeechMessage");
		this.log.fine(speechMessage.getSkillRef().getClass().getSimpleName());
		this.log.fine(speechMessage.getSkillExecutable().getExecutableId());
		this.log.fine("" + speechMessage.getLongRunning());

		// short message
		if (speechMessage.getLongRunning() == false) {

			// add to list of waiting messages
			this.waitingShortMessages.add(speechMessage);

			// if there is no short executable currently running start executing the next
			// short message
			if (this.shortExecutable == null) {
				this.executeShortExecutable();
			}

		}

		// long message
		else {

			// if there is no short executable currently running execute the long message
			if (this.shortExecutable == null) {
				this.executeLongExecutable(speechMessage);
			} else {
				// store message until the short messages are finished
				this.waitingLongMessage = speechMessage;
			}

		}

	}

	/**
	 * Receives a message from an executable that was running before and has now
	 * stopped. Calls one of two methods to handle the stop in the executor, like
	 * reseting references etc.
	 * 
	 * @param nextMessage
	 */
	private void handleEndMessage(AMessage nextMessage) {

		ExecutableEndMessage endMessage = (ExecutableEndMessage) nextMessage;

		this.log.info("received ExecutableEndMessage");
		this.log.fine(endMessage.getExecutableRef().getExecutableId());

		if (endMessage.getExecutableRef() == this.shortExecutable) {
			this.stopShortExecutable();
		}

		else if (endMessage.getExecutableRef() == this.longExecutable) {
			this.stopLongExecutable();
		}

	}

	/**
	 * Called when a stop-message was received in the incoming queue. Checks if the
	 * message contains a call for closing down the Runnable and acts accordingly.
	 * Then forwards the message.
	 * 
	 * Checks for running or waiting executables and ends / discards those.
	 * 
	 * @param nextMessage
	 */
	private void handleStopMessage(AMessage nextMessage) {

		StopMessage stopMessage = (StopMessage) nextMessage;

		this.log.info("received StopMessage");
		this.log.fine("stop and close:" + stopMessage.isStopAndClose());

		if (stopMessage.isStopAndClose()) {
			this.isClosed = true;
		}

		// remove all waiting messages
		this.waitingShortMessages.clear();
		this.waitingLongMessage = null;

		this.stopShortExecutable();
		this.stopLongExecutable();

		try {
			this.getOutgoingQueue().put(nextMessage);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block e.printStackTrace();
		}

	}

	/**
	 * Checks the executor's list of short messages and executes the oldest one.
	 * Stops a running long-executable if the message requests a dialog and
	 * registers the executable with the speechOut-service to enable it for speech
	 * output. Also sets the executable's outgoing-queue to the executor's incoming
	 * queue to enable receiving ExecutableEndMessages.
	 * 
	 */
	private void executeShortExecutable() {

		if (this.waitingShortMessages.size() > 0) {

			ExecutorSpeechMessage speechMessage = this.waitingShortMessages.remove(0);

			this.log.info("executing ShortMessage");
			this.log.fine(speechMessage.getSkillRef().getClass().getSimpleName());
			this.log.fine(speechMessage.getSkillExecutable().getExecutableId());

			ASkillExecutable executable = speechMessage.getSkillExecutable();

			if (executable != null) {

				// long executables are to be stopped when ther comes a dialog-request
				if (speechMessage.getStartDialogRequest()) {
					this.stopLongExecutable();
				}

				this.shortExecutable = executable;
				if (executable instanceof SpeechOutListener) {
					this.speechOut.registerShort((SpeechOutListener) executable);
				}
				this.shortExecutable.setOutgoingQueue(this.getIncomingQueue());

				this.shortThread = new Thread(this.shortExecutable);
				this.shortThread.start();
				this.sendFeedbackMessage(speechMessage, true, false);
				this.shortSkill = speechMessage.getSkillRef();
				this.status.setRunningShortExecutable(this.shortExecutable);

			}

		}

	}

	/**
	 * Executes the given message's long-executable. Stops the currently running
	 * long-executable. Registers the executable with the speechOut-service to
	 * enable it for speech output. Also sets the executable's outgoing-queue to the
	 * executor's incoming queue to enable receiving ExecutableEndMessages.
	 * 
	 * @param speechMessage
	 */
	private void executeLongExecutable(ExecutorSpeechMessage speechMessage) {

		// long executables are to be stopped if there comes a new long along
		if (this.longExecutable != null) {
			this.stopLongExecutable();
		}

		this.longExecutable = speechMessage.getSkillExecutable();
		if (longExecutable instanceof SpeechOutListener) {
			this.speechOut.registerLong((SpeechOutListener) this.longExecutable);
		}
		this.longExecutable.setOutgoingQueue(this.getIncomingQueue());

		this.longThread = new Thread(this.longExecutable);
		this.longThread.start();
		this.sendFeedbackMessage(speechMessage, true, false);
		this.longSkill = speechMessage.getSkillRef();
		this.status.setRunningLongExecutable(this.longExecutable);

	}

	/**
	 * Stops the currently running short executable and resets it's references and
	 * registration inside the speechOut-service. Starts the next short-executable
	 * if there is one waiting. Otherwise starts the waiting long-executable if
	 * there is one.
	 * 
	 */
	private void stopShortExecutable() {

		this.log.info("stopping short executable");

		if (this.shortSkill != null) {
			this.sendFeedbackMessage(this.shortSkill, this.shortExecutable, false, true);
		}

		if (this.shortExecutable != null) {
			this.shortExecutable.stop();
		}

		this.shortSkill = null;
		this.shortThread = null;
		this.shortExecutable = null;
		this.speechOut.registerShort(null);
		this.status.setRunningShortExecutable(null);

		// if there are already other short messages waiting, start executing the next
		if (this.waitingShortMessages.size() > 0) {
			this.executeShortExecutable();
		}

		// if not and there is a long-message waiting, execute that
		else if (this.waitingLongMessage != null) {
			ExecutorSpeechMessage message = this.waitingLongMessage;
			this.waitingLongMessage = null;
			this.executeLongExecutable(message);
		}

	}

	/**
	 * Stops the currently running long executable and resets it's references and
	 * registration inside the speechOut-service.
	 * 
	 */
	private void stopLongExecutable() {

		this.log.info("stopping long executable");

		if (this.longSkill != null) {
			this.sendFeedbackMessage(this.longSkill, this.longExecutable, false, true);
		}

		if (this.longExecutable != null) {
			this.longExecutable.stop();
		}

		this.longSkill = null;
		this.longThread = null;
		this.longExecutable = null;
		this.speechOut.registerLong(null);
		this.status.setRunningLongExecutable(null);

	}

	private void sendFeedbackMessage(Skill skill, ASkillExecutable executable, boolean isRunning, boolean isStopped) {

		FeedbackMessageWriter feedback = new FeedbackMessage(skill, executable);
		feedback.setIsRunning(isRunning);
		feedback.setIsStopped(isStopped);

		try {
			this.skillRegistryQueue.put((AMessage) feedback);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.log.info("sent FeedbackMessage");
		this.log.fine(skill.getClass().getSimpleName());
		this.log.fine(executable.getExecutableId());

	}

	private void sendFeedbackMessage(ExecutorSpeechMessage message, boolean isRunning, boolean isStopped) {

		FeedbackMessageWriter feedback = new FeedbackMessage((AMessage) message);
		feedback.setIsRunning(isRunning);
		feedback.setIsStopped(isStopped);

		try {
			this.skillRegistryQueue.put((AMessage) feedback);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.log.info("sent FeedbackMessage");
		this.log.fine(message.getSkillRef().getClass().getSimpleName());
		if (message.getSkillExecutable() != null) {
			this.log.fine(message.getSkillExecutable().getExecutableId());
		}

	}

	private void getStatus() {

		if (this.status == null) {

			try {

				this.context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
				Collection<ServiceReference<StatusAccess>> serviceReferencesStatus = null;
				serviceReferencesStatus = context.getServiceReferences(StatusAccess.class, "(name=Status)");
				StatusAccess service = context
						.getService(((List<ServiceReference<StatusAccess>>) serviceReferencesStatus).get(0));

				this.status = service;

				this.log.info("loaded Status");

			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getSpeechOut() {

		if (this.speechOut == null) {

			try {
				this.context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
				Collection<ServiceReference<SpeechOutSystem>> serviceReferences = null;
				serviceReferences = context.getServiceReferences(SpeechOutSystem.class, "(name=SpeechOut)");
				SpeechOutSystem service = context
						.getService(((List<ServiceReference<SpeechOutSystem>>) serviceReferences).get(0));

				this.speechOut = service;

				this.log.info("loaded SpeechOut");

			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getLogger() {

		if (this.log == null) {

			try {
				BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
				Collection<ServiceReference<PippaLogger>> serviceReferences = null;
				serviceReferences = context.getServiceReferences(PippaLogger.class, "(name=PippaLogger)");
				PippaLogger service = context
						.getService(((List<ServiceReference<PippaLogger>>) serviceReferences).get(0));

				this.log = service.getLogger();

				this.log.info("loaded Logger");

			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public BlockingQueue<AMessage> getSkillRegistryQueue() {
		return this.skillRegistryQueue;
	}

	@Override
	public void setSkillRegistryQueue(BlockingQueue<AMessage> skillRegistryQueue) {
		this.skillRegistryQueue = skillRegistryQueue;
	}

}
