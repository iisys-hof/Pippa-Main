package de.iisys.pippa.skill.sorry;

import java.util.Collection;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message.speech_message.SkillSpeechMessage;
import de.iisys.pippa.core.message.stop_message.StopMessage;
import de.iisys.pippa.core.message_processor.AMessageProcessor;
import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.skill.SkillRegex;
import de.iisys.pippa.core.skill_executable.ASkillExecutable;
import de.iisys.pippa.core.speech_out.SpeechOut;

public class SorrySkillImpl extends AMessageProcessor implements Skill {

	private boolean isClosed = false;

	private SpeechOut speechOut = null;

	// regex matches an empty string which is not followed by another empty string =
	// unmatchable
	SkillRegex[] skillRegexes = new SkillRegex[] { new SkillRegex(this, "(?!)") };

	ASkillExecutable sorrySkillExecutable = null;

	public SkillRegex[] getRegexes() {
		return this.skillRegexes;
	}

	private void getSpeechOut() {
		if (this.speechOut == null) {

			Collection<ServiceReference<SpeechOut>> serviceReferences = null;

			try {

				BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

				serviceReferences = context.getServiceReferences(SpeechOut.class, "(name=SpeechOut)");

				SpeechOut service = context.getService(((List<ServiceReference<SpeechOut>>) serviceReferences).get(0));

				this.speechOut = service;

			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	
	public void run() {

		this.getSpeechOut();

		while (!this.isClosed) {

			AMessage nextMessage = null;

			try {

				nextMessage = this.getIncomingQueue().take();

				if (nextMessage != null) {

					if (nextMessage instanceof StopMessage) {
						StopMessage stopMessage = (StopMessage) nextMessage;
						if (stopMessage.isStopAndClose()) {
							this.isClosed = true;
						}
						this.getOutgoingQueue().put(nextMessage);
					}

					else if (nextMessage instanceof SkillSpeechMessage) {
						SkillSpeechMessage speechMessage = (SkillSpeechMessage) nextMessage;

						System.out.println("SorrySkill Received SkillSpeechMessage");

						// TODO
						sorrySkillExecutable = new SorrySkillExecutableImpl(this.speechOut);

						speechMessage.setConfidence((float) 1.0);
						speechMessage.setSkillExecutable(sorrySkillExecutable);

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
