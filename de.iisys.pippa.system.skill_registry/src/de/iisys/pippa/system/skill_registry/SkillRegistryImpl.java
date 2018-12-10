package de.iisys.pippa.system.skill_registry;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message.feedack_message.FeedbackMessage;
import de.iisys.pippa.core.message.speech_message.SkillRegistrySpeechMessage;
import de.iisys.pippa.core.message.stop_message.StopMessage;
import de.iisys.pippa.core.message_processor.AMessageProcessor;
import de.iisys.pippa.core.service_loader.PippaServiceLoader;
import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.skill_registry.SkillRegistry;
import de.iisys.pippa.core.status.StatusAccess;

public class SkillRegistryImpl extends AMessageProcessor implements SkillRegistry {

	private Logger log = null;

	/**
	 * reference to the system's status object
	 */
	protected StatusAccess status = null;

	/**
	 * reference to the bundle's context
	 */
	protected BundleContext context = null;

	/**
	 * contains all Skills that were registered
	 */
	protected List<Skill> registeredSkills = new CopyOnWriteArrayList<Skill>();

	/**
	 * flag that is used internally to determine whether the runnable should return
	 * (close the thread)
	 */
	protected boolean isClosed = false;

	/**
	 * only implemented to be used by the package test-classes
	 * 
	 * @param status
	 */
	protected SkillRegistryImpl(StatusAccess status) {
		this.status = status;
	}

	public SkillRegistryImpl() {
		this.context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
	}

	@Override
	public void registerSkill(Skill skillRef) {

		if (skillRef == null) {
			throw (new NullPointerException("The given Skill-Object is NULL."));
		}

		this.log.info("registering skill: " + skillRef.getClass().getSimpleName());

		if (!this.registeredSkills.contains(skillRef)) {

			this.registeredSkills.add(skillRef);
		}

	}

	@Override
	public void deregisterSkill(Skill skillRef) {

		if (skillRef == null) {
			throw (new NullPointerException("The given Skill-Object is NULL."));
		}

		this.log.info("deregistering skill: " + skillRef.getClass().getSimpleName());

		if (this.registeredSkills.contains(skillRef)) {
			this.registeredSkills.remove(skillRef);
		}

	}

	@Override
	public void run() {

		if (this.log == null) {
			this.log = PippaServiceLoader.getLogger(this.context);
		}
		if (this.status == null) {
			this.status = PippaServiceLoader.getStatus(this.context);
			if (this.status == null) {
				this.log.severe("could not retrieve the needed Status-Object: closing Skill-Registry");
				this.isClosed = true;
			}
		}

		this.log.fine("entering run()-loop");

		// check every go-around if the skillRegistry-thread should be closed
		while (!this.isClosed) {

			// reset temporary variables
			AMessage nextMessage = null;

			try {

				// skillRegistry will wait here for the next incoming message
				nextMessage = this.getIncomingQueue().take();

				if (nextMessage != null) {

					// this type of message will be forwarded and eventually stop the
					// skillRegistry-thread
					if (nextMessage instanceof StopMessage) {

						this.handleStopMessage(nextMessage);

					}

					// this will cause some action inside the skillRegistry
					else if (nextMessage instanceof SkillRegistrySpeechMessage) {

						this.handleSkillRegistrySpeechMessage(nextMessage);

					}

					else if (nextMessage instanceof FeedbackMessage) {

						this.handleFeedbackMessage(nextMessage);

					}

					// The received message-type is of no interest for this class, just forward it
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
	 * Will simply forward the received feedback to the given receiver, if it is
	 * still in the skill registry.
	 * 
	 * @param nextMessage
	 */
	private void handleFeedbackMessage(AMessage nextMessage) {

		FeedbackMessage feedbackMessage = (FeedbackMessage) nextMessage;

		this.log.info("forwarding FeedbackMessage - " + nextMessage.getMessageId() + " - receiver: "
				+ feedbackMessage.getReceiver().getClass().getSimpleName());

		if (this.registeredSkills.contains(feedbackMessage.getReceiver())) {
			try {
				((AMessageProcessor) feedbackMessage.getReceiver()).getIncomingQueue().put(feedbackMessage);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			this.log.info(
					"receiver " + feedbackMessage.getReceiver().getClass().getSimpleName() + " could not be found");
		}

	}

	/**
	 * Will forward the received message to all receivers referenced inside the
	 * message, as long as they still exist in the skill-registry. If there is more
	 * than one receiver, a shallow copy of the message object will be created to
	 * pass to each receiver.
	 * 
	 * @param nextMessage
	 */
	private void handleSkillRegistrySpeechMessage(AMessage nextMessage) {

		SkillRegistrySpeechMessage speechMessage = (SkillRegistrySpeechMessage) nextMessage;

		this.log.info("handling SpeechMessage " + nextMessage.getMessageId() + " with "
				+ speechMessage.getDispatchedSkills().length + " receivers");

		Skill[] dispatchedSkills = speechMessage.getDispatchedSkills();

		if (dispatchedSkills.length > 0) {

			// step through all skills that are referenced in the message
			for (Skill skill : dispatchedSkills) {

				// clone message for every referenced skill
				// TODO keep in mind this is a shallow copy!
				SkillRegistrySpeechMessage cloneMessage = speechMessage.copy();
				cloneMessage.setSkillRef(skill);

				// check if the referenced skill is actually in the registry of skills and send
				// message it's way there
				if (this.registeredSkills.contains(skill)) {

					try {
						this.log.fine("forwarding SpeechMessage " + nextMessage.getMessageId() + " to "
								+ skill.getClass().getSimpleName());
						((AMessageProcessor) skill).getIncomingQueue().put((AMessage) cloneMessage);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				} else {
					// TODO react to a skill being referenced in message but not in registry
					this.log.fine("Skill " + skill.getClass().getSimpleName() + " is referenced in message "
							+ nextMessage.getMessageId() + " but not found in registry");
				}
			}

		} else {
			// TODO react to an empty list of chosen skills
			this.log.fine("list of receiving skills for message " + nextMessage.getMessageId() + " was empty");
		}

	}

	/**
	 * Called when a stop-message was received in the incoming queue. Checks if the
	 * message contains a call for closing down the Runnable and acts accordingly.
	 * Then forwards the message to all skills that were chosen during the last call
	 * and might still be running.
	 * 
	 * If the message has the stop-and-close flag set it will be forwarded to all
	 * skills in the registry.
	 * 
	 * @param nextMessage
	 */
	private void handleStopMessage(AMessage nextMessage) {

		StopMessage stopMessage = (StopMessage) nextMessage;

		Skill[] skillsToBeStopped = null;

		// if this stopmessage shuts down the system, all threads (and therefore all
		// skills) have to notified to stop and close
		if (stopMessage.isStopAndClose()) {
			this.log.info("received stop-and-close-message and forwarding it to all skills in registry");
			this.isClosed = true;
			skillsToBeStopped = this.registeredSkills.toArray(new Skill[this.registeredSkills.size()]);
		}
		// otherwise just stop the last dispatched and potentially running skills
		else {
			this.log.info("received stop-message and forwarding it the last resolved skills");
			boolean dispatchedContainResolved = false;

			if (this.status.getLastResolvedSkill() == null) {
				dispatchedContainResolved = true;
			}

			else {
				for (Skill dispatched : this.status.getLastDispatchedSkills()) {
					if (dispatched == this.status.getLastResolvedSkill()) {
						dispatchedContainResolved = true;
						break;
					}
				}
			}

			if (dispatchedContainResolved) {
				skillsToBeStopped = this.status.getLastDispatchedSkills();
			}

			else {
				skillsToBeStopped = new Skill[this.status.getLastDispatchedSkills().length + 1];
				for (int i = 0; i < this.status.getLastDispatchedSkills().length; i++) {
					skillsToBeStopped[i] = this.status.getLastDispatchedSkills()[i];
				}
				skillsToBeStopped[skillsToBeStopped.length - 1] = this.status.getLastResolvedSkill();
			}

		}

		try {

			if (skillsToBeStopped != null && skillsToBeStopped.length > 0) {

				for (Skill lastSkill : skillsToBeStopped) {

					this.log.fine("forwarding stop-message to skill " + lastSkill.getClass().getSimpleName());

					// send each skill a copy of the stopMessage
					StopMessage copyMessage = stopMessage.copy();

					((AMessageProcessor) lastSkill).getIncomingQueue().put(copyMessage);

				}

			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// TODO what if there are no last chosen skills? the message should reach the
		// resolver nonetheless

	}

}
