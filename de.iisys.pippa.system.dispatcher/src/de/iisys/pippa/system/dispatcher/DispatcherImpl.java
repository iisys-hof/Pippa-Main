package de.iisys.pippa.system.dispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import de.iisys.pippa.core.dispatcher.Dispatcher;
import de.iisys.pippa.core.logger.PippaLogger;
import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message.speech_message.DispatcherSpeechMessage;
import de.iisys.pippa.core.message.stop_message.StopMessage;
import de.iisys.pippa.core.message_processor.AMessageProcessor;
import de.iisys.pippa.core.service_loader.PippaServiceLoader;
import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.skill.SkillRegex;
import de.iisys.pippa.core.status.StatusAccess;
import de.iisys.pippa.core.volume_controller.VolumeController;

public class DispatcherImpl extends AMessageProcessor implements Dispatcher {

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
	 * contains all Skills that were registered together with their Regexes
	 */
	protected ConcurrentHashMap<Skill, SkillRegex[]> registeredExpressions = new ConcurrentHashMap<Skill, SkillRegex[]>();

	protected Matcher expressionMatcher = null;

	/**
	 * flag that is used internally to determine whether the runnable should return
	 * (close the thread)
	 */
	protected boolean isClosed = false;

	/**
	 * reference to the skill which is to be called when no match can be found
	 * (sorry for not understanding)
	 */
	protected Skill sorrySkill = null;

	/**
	 * currently used pattern to check if the user wants the currently running
	 * actions to stop
	 */
	protected Pattern stopPattern = Pattern.compile("^(stop)");

	protected Pattern mutePattern = Pattern.compile("(mute)");
	protected Pattern unmutePattern = Pattern.compile("(unmute)");
	protected Pattern volumePattern = Pattern.compile("(volume)");
	protected Pattern louderPattern = Pattern.compile("(louder)|(up)");
	protected Pattern softerPattern = Pattern.compile("(softer)|(down)");

	Pattern[] volumePatterns = new Pattern[] { unmutePattern, mutePattern, volumePattern, louderPattern,
			softerPattern };

	/**
	 * list of the latest chosen skills, is renewed with every new message
	 */
	List<Skill> dispatchedSkillsList = new ArrayList<Skill>();

	/**
	 * map of the latest matched regexes, mapped by the skill they belong to
	 */
	HashMap<Skill, SkillRegex[]> matchedRegexes = new HashMap<Skill, SkillRegex[]>();

	/**
	 * reference to the system's volume control object
	 */
	protected VolumeController volumeController = null;

	/**
	 * only implemented to be used by the package test-classes
	 * 
	 * @param status
	 * @param sorrySkill
	 * @param volumeController
	 */
	protected DispatcherImpl(StatusAccess status, Skill sorrySkill, VolumeController volumeController) {
		this.status = status;
		this.sorrySkill = sorrySkill;
		this.volumeController = volumeController;
	}

	/**
	 * Constructor uses it's service reference to find the needed objects such the
	 * status-object, sorry-skill and system-skill. Both these skills are special
	 * skills and can therefore be known by name.
	 */
	public DispatcherImpl() {

		this.context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

	}

	@Override
	public void registerRegexes(Skill skillRef) {

		this.log.info("registering regexes for " + skillRef.getClass().getSimpleName());

		if (skillRef == null) {
			throw (new NullPointerException("The given Skill-Object is NULL."));
		}

		this.registeredExpressions.put(skillRef, skillRef.getRegexes());

	}

	@Override
	public void deregisterRegexes(Skill skillRef) {

		if (skillRef == null) {
			throw (new NullPointerException("The given Skill-Object is NULL."));
		}

		this.log.info("deregistering regexes for " + skillRef.getClass().getSimpleName());

		if (this.registeredExpressions.containsKey(skillRef)) {
			this.registeredExpressions.remove(skillRef);
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
				this.log.severe("could not retrieve the needed Status-Object: closing dispatcher");
				this.isClosed = true;
			}
		}

		try {
			this.sorrySkill = (Skill) PippaServiceLoader.getService(Skill.class, "Sorry", this.context);
		} catch (Exception e1) {
			this.log.severe("could not retrieve Sorry-Skill");
			e1.printStackTrace();
		}

		try {
			this.volumeController = (VolumeController) PippaServiceLoader.getService(VolumeController.class,
					"VolumeController", this.context);
		} catch (Exception e1) {
			this.log.severe("could not retrieve Volume-Controller");
			e1.printStackTrace();
		}

		this.log.fine("entering run()-loop");

		// check every go-around if the dispatcher-thread should be closed
		while (!this.isClosed) {

			// reset temporary variables
			AMessage nextMessage = null;
			this.dispatchedSkillsList = new ArrayList<Skill>();
			this.matchedRegexes = new HashMap<Skill, SkillRegex[]>();

			try {

				// dispatcher will wait here for the next incoming message
				nextMessage = this.getIncomingQueue().take();

				if (nextMessage != null) {

					// this type of message will be forwarded and eventually stop the
					// dispatcher-thread
					if (nextMessage instanceof StopMessage) {
						this.handleStopMessage(nextMessage);
					}

					// this will cause some action inside the dispatcher
					else if (nextMessage instanceof DispatcherSpeechMessage) {
						this.handleDispatcherSpeechMessage(nextMessage);
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
	 * Will react to the received message in accord with the message's parameters
	 * and the referenced status-object. Dependent on the signs this will trigger a
	 * stop-message, call for the system-access skill, call for the sorry-skill or
	 * call for (multiple) other skills that are to be identified by their
	 * registered regexes.
	 * 
	 * @param nextMessage
	 */
	private void handleDispatcherSpeechMessage(AMessage nextMessage) {

		DispatcherSpeechMessage speechMessage = (DispatcherSpeechMessage) nextMessage;

		this.log.info("handling SpeechMessage - " + nextMessage.getMessageId());

		// check if the user called stop
		if (this.matchStopRegex(speechMessage.getAudioText())) {

			this.log.info("message was a stop-message");

			this.status.setDialogRunning(false);
			this.status.setDialogWith(null);

			StopMessage stopMessage = new StopMessage(this, false);

			this.sendMessage(stopMessage);

		}

		// check if there is a system access regex triggered
		else if (this.matchSystemRegexes(speechMessage.getAudioText())) {

			this.log.info("message contained regexes for system-access");

		}

		// check if the message was already out and came back because the receiver could
		// not handle it
		else if (speechMessage.getReturnToDispatcher()) {

			this.log.info("message is a message that was returned to dispatcher ");

			String utteranceText = speechMessage.getAudioText();
			// match utterance again..
			this.matchRegexes(utteranceText);

			// ..and see if there are any receivers and if they are not the same receivers
			// that returned the message
			if (dispatchedSkillsList.size() > 0 && !(this.dispatchedSkillsList
					.containsAll(Arrays.asList(speechMessage.getDispatchedSkills()))
					&& Arrays.asList(speechMessage.getDispatchedSkills()).containsAll(this.dispatchedSkillsList))) {

				this.log.info("message could be matched with new receivers");

				// turn list of chosen skills to array
				Skill[] dispatchedSkills = new Skill[dispatchedSkillsList.size()];
				dispatchedSkills = dispatchedSkillsList.toArray(dispatchedSkills);

				// fill message with chosen skills and triggering regexes
				speechMessage.setDispatchedSkills(dispatchedSkills);
				speechMessage.setMatchedRegexes(matchedRegexes);

				this.log.fine(dispatchedSkills.toString());

				// reset return flag
				speechMessage.setReturnToDispatcher(false);

				this.sendMessage((AMessage) speechMessage);
			}

			// no new skills could be identified to handle the message
			// so we tell the user we are sorry
			else {
				this.log.info("message could not be matched with new receivers");

				Skill[] dispatchedSkills = { this.sorrySkill };
				speechMessage.setDispatchedSkills(dispatchedSkills);
				speechMessage.setMatchedRegexes(matchedRegexes);

				this.sendMessage((AMessage) speechMessage);
			}

		}

		// check if a dialog is currently running..
		else if (this.status.getDialogRunning()) {

			this.log.info("message was received while a dialog is running");
			this.log.fine(this.status.getDialogWith().getClass().getSimpleName());

			this.log.info("message is to be sent to the current dialog partner");
			this.log.fine(this.status.getDialogWith().getClass().getSimpleName());

			// set the current dialog partner as receiver
			Skill dialogReceiver = this.status.getDialogWith();

			Skill[] dispatchedSkills = { dialogReceiver };
			speechMessage.setDispatchedSkills(dispatchedSkills);
			speechMessage.setMatchedRegexes(matchedRegexes);

			this.sendMessage((AMessage) speechMessage);

		}

		// now we know it's not a returned message and no dialog is running
		else {

			this.log.info("message is a fresh message and no dialog is running");

			// get the users utterance as text
			String utteranceText = speechMessage.getAudioText();
			this.log.fine("utterance: " + utteranceText);

			this.matchRegexes(utteranceText);

			// if there were any matches between regexes and utterance
			if (dispatchedSkillsList.size() > 0) {

				// turn list of chosen skills to array
				Skill[] dispatchedSkills = new Skill[dispatchedSkillsList.size()];
				dispatchedSkills = dispatchedSkillsList.toArray(dispatchedSkills);

				this.log.info(matchedRegexes.size() + " regex matches found");
				for (SkillRegex[] regexes : matchedRegexes.values()) {
					for (SkillRegex regex : regexes) {
						this.log.fine(regex.getSkillRef().getClass().getSimpleName() + " - " + regex.getPattern());
					}
				}

				// fill message with chosen skills and triggering regexes
				speechMessage.setDispatchedSkills(dispatchedSkills);
				speechMessage.setMatchedRegexes(matchedRegexes);

				this.sendMessage((AMessage) speechMessage);
			}

			// no matching regexes were found
			// we tell the user we are sorry..
			else {
				this.log.info("no regex matches found");

				Skill[] dispatchedSkills = { this.sorrySkill };
				speechMessage.setDispatchedSkills(dispatchedSkills);
				speechMessage.setMatchedRegexes(matchedRegexes);

				this.sendMessage((AMessage) speechMessage);

			}

		}

	}

	/**
	 * Puts the given message into the classe's outgoing queue. Updates the status
	 * object accordingly.
	 * 
	 * @param message
	 */
	private void sendMessage(AMessage message) {

		this.log.info("sending message");

		if (message instanceof DispatcherSpeechMessage) {
			DispatcherSpeechMessage speechMessage = (DispatcherSpeechMessage) message;
			this.status.setLastDispatchedSkills(speechMessage.getDispatchedSkills());
			for (Skill skillRef : speechMessage.getDispatchedSkills()) {
				this.log.fine(skillRef.getClass().getSimpleName());
			}
		}

		try {
			this.getOutgoingQueue().put(message);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Checks the utterance against the regex that describes the stop-word.
	 * 
	 * @param utteranceText
	 * @return true if stop-word was uttered
	 */
	private boolean matchStopRegex(String utteranceText) {

		this.log.info("checking for stop-word utterance");

		this.matchedRegexes = new HashMap<Skill, SkillRegex[]>();

		this.expressionMatcher = this.stopPattern.matcher(utteranceText);

		if (this.expressionMatcher.find()) {
			this.log.fine("stop-word was uttered");
			return true;
		}

		return false;
	}

	/**
	 * Checks the utterance against the regexes that describe calls for
	 * system-access.
	 * 
	 * @param utteranceText
	 * @return true if call for system access was uttered
	 */
	private boolean matchSystemRegexes(String utteranceText) {

		this.log.info("checking for system-access utterance");

		this.matchedRegexes = new HashMap<Skill, SkillRegex[]>();

		for (Pattern pattern : this.volumePatterns) {

			this.expressionMatcher = pattern.matcher(utteranceText);

			if (this.expressionMatcher.find()) {
				this.log.fine("system-access call was uttered");

				if (pattern.equals(this.mutePattern)) {
					this.volumeController.mute();
				}
				
				if (pattern.equals(this.unmutePattern)) {
					this.volumeController.unmute();
				}
				
				if (pattern.equals(this.volumePattern)) {
					int volumeTo = this.extractFirstNumber(utteranceText);
					this.volumeController.setVolumeTo(volumeTo);
				}
				
				if (pattern.equals(this.louderPattern)) {
					if (this.containsNumber(utteranceText)) {
						int louderBy = this.extractFirstNumber(utteranceText);
						this.volumeController.increaseVolumeBy(louderBy);
					} else {
						this.volumeController.increaseVolumeBy(1);
					}
				}
				
				if (pattern.equals(this.softerPattern)) {
					if (this.containsNumber(utteranceText)) {
						int softerBy = this.extractFirstNumber(utteranceText);
						this.volumeController.decreaseVolumeBy(softerBy);
					} else {
						this.volumeController.decreaseVolumeBy(1);
					}
				}

				return true;
				
			}
		}

		return false;
	}

	/**
	 * Matches the utterance against all currently registered regexes. Sets a list
	 * if chosen skills and a list of matched regexes.
	 * 
	 * @param utteranceText
	 */
	private void matchRegexes(String utteranceText) {

		this.log.info("checking utterance against collection of regexes");

		this.dispatchedSkillsList = new ArrayList<Skill>();
		this.matchedRegexes = new HashMap<Skill, SkillRegex[]>();

		// check all registered regular expressions for a match within the utterance
		// step through all arrays of regexes, which are bundled by Skill
		for (SkillRegex[] regexes : this.registeredExpressions.values()) {

			// to store which regexes triggered a match
			List<SkillRegex> matched = new ArrayList<SkillRegex>();

			// check each individual regex for a match
			for (SkillRegex regex : regexes) {

				this.log.fine("checking regex: " + regex.getSkillRef().getClass().getSimpleName() + " - "
						+ regex.getPattern());

				this.expressionMatcher = regex.getPattern().matcher(utteranceText);

				// if matching, put the associated skill on the list of chosen skills
				if (this.expressionMatcher.find()) {

					this.log.fine("regex matched, adding skill and regex!");

					// but only once
					boolean contained = false;
					for (Skill skillRef : dispatchedSkillsList) {
						if (skillRef.getClass().getName().equals(regex.getSkillRef().getClass().getName())) {
							contained = true;
						}
					}
					if (!contained) {
						this.log.fine("first match for skill, adding to list");
						dispatchedSkillsList.add(regex.getSkillRef());
					} else {
						this.log.fine("skill already in matched list, not adding again");
					}

					// and add the triggering regex to the list of triggering regexes (;
					matched.add(regex);

				}
			}

			this.log.fine(matched.size() + " matching regexes found");

			// if there were any matches at all
			if (matched.size() > 0) {

				// turn list of triggering regexes into an array and store it to a HashMap,
				// bundled by skill
				SkillRegex[] matchedArray = new SkillRegex[matched.size()];
				matchedArray = matched.toArray(matchedArray);

				matchedRegexes.put(matched.get(0).getSkillRef(), matchedArray);
			}

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
	
	private int extractFirstNumber(String str) {

		StringBuilder sb = new StringBuilder();
		boolean isDigit = false;

		for (char c : str.toCharArray()) {

			if (Character.isDigit(c)) {
				sb.append(c);
				isDigit = true;
			}

			else if (isDigit) {
				break;
			}

		}

		if (sb.length() > 0) {
			return Integer.parseInt(sb.toString());
		} else {
			return -1;
		}

	}
	
	private boolean containsNumber(String input) {

		return input.matches(".*\\d+.*");

	}

}
