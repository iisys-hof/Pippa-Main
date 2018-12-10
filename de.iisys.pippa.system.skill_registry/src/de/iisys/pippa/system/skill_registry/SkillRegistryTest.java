package de.iisys.pippa.system.skill_registry;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message.feedack_message.FeedbackMessage;
import de.iisys.pippa.core.message.speech_message.SpeechMessage;
import de.iisys.pippa.core.message.stop_message.StopMessage;
import de.iisys.pippa.core.message_processor.AMessageProcessor;
import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.status.StatusAccess;
import de.iisys.pippa.skill.clock.ClockSkillImpl;
import de.iisys.pippa.skill.sorry.SorrySkillImpl;
import de.iisys.pippa.skill.system_access.SystemAccessSkillImpl;
import de.iisys.pippa.support.status.Status;

public class SkillRegistryTest {

	static StatusAccess status = null;
	static SkillRegistryImpl skillRegistry = null;
	static Skill clockSkill = null;
	static Skill sorrySkill = null;
	static Skill systemSkill = null;
	static SpeechMessage speechMessage = null;
	static Thread thread = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

		status = Status.getInstance();
		clockSkill = new ClockSkillImpl();
		sorrySkill = new SorrySkillImpl();
		systemSkill = new SystemAccessSkillImpl();
		skillRegistry = new SkillRegistryImpl(status);
		speechMessage = new SpeechMessage();

		thread = new Thread(skillRegistry);
		thread.start();
		
		Thread.sleep(100);
		
		skillRegistry.registerSkill(clockSkill);
		skillRegistry.registerSkill(sorrySkill);
		skillRegistry.registerSkill(systemSkill);

	}

	@SuppressWarnings("deprecation")
	@After
	public void tearDown() throws Exception {
		status = null;
		skillRegistry = null;
		clockSkill = null;
		sorrySkill = null;
		systemSkill = null;
		speechMessage = null;
		if (thread != null) {
			thread.stop();
		}
	}

	@Test
	public void testSimpleMessageSingleReceiver() {

		speechMessage.setDispatchedSkills(new Skill[] { clockSkill });

		assertTrue("ClockSkill has not received a message yet",
				((AMessageProcessor) clockSkill).getIncomingQueue().size() == 0);
		assertTrue("SystemSkill has not received a message yet",
				((AMessageProcessor) systemSkill).getIncomingQueue().size() == 0);
		assertTrue("SorrySkill has not received a message yet",
				((AMessageProcessor) sorrySkill).getIncomingQueue().size() == 0);

		try {
			skillRegistry.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertTrue("ClockSkill has received 1 message",
				((AMessageProcessor) clockSkill).getIncomingQueue().size() == 1);
		try {
			SpeechMessage copyMessage = (SpeechMessage) ((AMessageProcessor) clockSkill).getIncomingQueue().take();
			assertTrue("received message contains reference to skill", copyMessage.getSkillRef() == clockSkill);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("SystemSkill has not received a message yet",
				((AMessageProcessor) systemSkill).getIncomingQueue().size() == 0);
		assertTrue("SorrySkill has not received a message yet",
				((AMessageProcessor) sorrySkill).getIncomingQueue().size() == 0);

	}

	@Test
	public void testSimpleMessageMultipleReceiver() {

		speechMessage.setDispatchedSkills(new Skill[] { clockSkill, systemSkill });

		assertTrue("ClockSkill has not received a message yet",
				((AMessageProcessor) clockSkill).getIncomingQueue().size() == 0);
		assertTrue("SystemSkill has not received a message yet",
				((AMessageProcessor) systemSkill).getIncomingQueue().size() == 0);
		assertTrue("SorrySkill has not received a message yet",
				((AMessageProcessor) sorrySkill).getIncomingQueue().size() == 0);

		try {
			skillRegistry.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertTrue("ClockSkill has received 1 message",
				((AMessageProcessor) clockSkill).getIncomingQueue().size() == 1);
		try {
			SpeechMessage copyMessage = (SpeechMessage) ((AMessageProcessor) clockSkill).getIncomingQueue().take();
			assertTrue("received message contains reference to skill", copyMessage.getSkillRef() == clockSkill);
		} catch (InterruptedException e) {
			fail();
		}
		
		assertTrue("SystemSkill has not received 1 message",
				((AMessageProcessor) systemSkill).getIncomingQueue().size() == 1);
		try {
			SpeechMessage copyMessage = (SpeechMessage) ((AMessageProcessor) systemSkill).getIncomingQueue().take();
			assertTrue("received message contains reference to skill", copyMessage.getSkillRef() == systemSkill);
		} catch (InterruptedException e) {
			fail();
		}
		
		assertTrue("SorrySkill has not received a message yet",
				((AMessageProcessor) sorrySkill).getIncomingQueue().size() == 0);

	}

	@Test
	public void testStopMessageAsReceiverNoResolved() {

		status.setLastDispatchedSkills(new Skill[] { clockSkill, systemSkill });

		StopMessage stopperMessage = new StopMessage(null, false);

		try {
			skillRegistry.getIncomingQueue().put(stopperMessage);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		AMessage stopMessage = null;

		try {
			stopMessage = ((AMessageProcessor) clockSkill).getIncomingQueue().take();
		} catch (InterruptedException e) {
			fail();
		}
		assertTrue("Message in Queue is of type StopMessage", stopMessage instanceof StopMessage);
		assertTrue("stopMessage is not closing", ((StopMessage) stopMessage).isStopAndClose() == false);
		assertTrue("stopMessage creator is still null", ((StopMessage) stopMessage).getCreator() == null);

		try {
			stopMessage = ((AMessageProcessor) systemSkill).getIncomingQueue().take();
		} catch (InterruptedException e) {
			fail();
		}
		assertTrue("Message in Queue is of type StopMessage", stopMessage instanceof StopMessage);
		assertTrue("stopMessage is not closing", ((StopMessage) stopMessage).isStopAndClose() == false);
		assertTrue("stopMessage creator is still null", ((StopMessage) stopMessage).getCreator() == null);

		assertTrue("SorrySkill has not received a message yet",
				((AMessageProcessor) sorrySkill).getIncomingQueue().size() == 0);

		assertTrue("skillRegistry is not set to be closed", skillRegistry.isClosed == false);
		assertTrue("SkillRegistry has not closed", thread.isAlive() == true);

	}

	@Test
	public void testStopMessageAsReceiverWithResolvedContained() {

		status.setLastDispatchedSkills(new Skill[] { clockSkill, systemSkill });
		status.setLastResolvedSkill(clockSkill);

		StopMessage stopperMessage = new StopMessage(null, false);

		try {
			skillRegistry.getIncomingQueue().put(stopperMessage);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		AMessage stopMessage = null;

		try {
			stopMessage = ((AMessageProcessor) clockSkill).getIncomingQueue().take();
		} catch (InterruptedException e) {
			fail();
		}
		assertTrue("Message in Queue is of type StopMessage", stopMessage instanceof StopMessage);
		assertTrue("stopMessage is not closing", ((StopMessage) stopMessage).isStopAndClose() == false);
		assertTrue("stopMessage creator is still null", ((StopMessage) stopMessage).getCreator() == null);

		try {
			stopMessage = ((AMessageProcessor) systemSkill).getIncomingQueue().take();
		} catch (InterruptedException e) {
			fail();
		}
		assertTrue("Message in Queue is of type StopMessage", stopMessage instanceof StopMessage);
		assertTrue("stopMessage is not closing", ((StopMessage) stopMessage).isStopAndClose() == false);
		assertTrue("stopMessage creator is still null", ((StopMessage) stopMessage).getCreator() == null);

		assertTrue("SorrySkill has not received a message yet",
				((AMessageProcessor) sorrySkill).getIncomingQueue().size() == 0);

		assertTrue("skillRegistry is not set to be closed", skillRegistry.isClosed == false);
		assertTrue("SkillRegistry has not closed", thread.isAlive() == true);

	}
	
	@Test
	public void testStopMessageAsReceiverWithResolvedNotContained() {

		status.setLastDispatchedSkills(new Skill[] { clockSkill, systemSkill });
		status.setLastResolvedSkill(sorrySkill);

		StopMessage stopperMessage = new StopMessage(null, false);

		try {
			skillRegistry.getIncomingQueue().put(stopperMessage);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		AMessage stopMessage = null;

		try {
			stopMessage = ((AMessageProcessor) clockSkill).getIncomingQueue().take();
		} catch (InterruptedException e) {
			fail();
		}
		assertTrue("Message in Queue is of type StopMessage", stopMessage instanceof StopMessage);
		assertTrue("stopMessage is not closing", ((StopMessage) stopMessage).isStopAndClose() == false);
		assertTrue("stopMessage creator is still null", ((StopMessage) stopMessage).getCreator() == null);

		try {
			stopMessage = ((AMessageProcessor) systemSkill).getIncomingQueue().take();
		} catch (InterruptedException e) {
			fail();
		}
		assertTrue("Message in Queue is of type StopMessage", stopMessage instanceof StopMessage);
		assertTrue("stopMessage is not closing", ((StopMessage) stopMessage).isStopAndClose() == false);
		assertTrue("stopMessage creator is still null", ((StopMessage) stopMessage).getCreator() == null);

		try {
			stopMessage = ((AMessageProcessor) sorrySkill).getIncomingQueue().take();
		} catch (InterruptedException e) {
			fail();
		}
		assertTrue("Message in Queue is of type StopMessage", stopMessage instanceof StopMessage);
		assertTrue("stopMessage is not closing", ((StopMessage) stopMessage).isStopAndClose() == false);
		assertTrue("stopMessage creator is still null", ((StopMessage) stopMessage).getCreator() == null);

		assertTrue("skillRegistry is not set to be closed", skillRegistry.isClosed == false);
		assertTrue("SkillRegistry has not closed", thread.isAlive() == true);

	}
	
	@Test
	public void testStopAndCloseMessageAsReceiver() {

		status.setLastDispatchedSkills(new Skill[] { clockSkill });
		status.setLastResolvedSkill(systemSkill);

		StopMessage stopperMessage = new StopMessage(null, true);

		try {
			skillRegistry.getIncomingQueue().put(stopperMessage);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		AMessage stopMessage = null;

		try {
			stopMessage = ((AMessageProcessor) clockSkill).getIncomingQueue().take();
		} catch (InterruptedException e) {
			fail();
		}
		assertTrue("Message in Queue is of type StopMessage", stopMessage instanceof StopMessage);
		assertTrue("stopMessage is closing", ((StopMessage) stopMessage).isStopAndClose() == true);
		assertTrue("stopMessage creator is still null", ((StopMessage) stopMessage).getCreator() == null);

		try {
			stopMessage = ((AMessageProcessor) systemSkill).getIncomingQueue().take();
		} catch (InterruptedException e) {
			fail();
		}
		assertTrue("Message in Queue is of type StopMessage", stopMessage instanceof StopMessage);
		assertTrue("stopMessage is closing", ((StopMessage) stopMessage).isStopAndClose() == true);
		assertTrue("stopMessage creator is still null", ((StopMessage) stopMessage).getCreator() == null);

		try {
			stopMessage = ((AMessageProcessor) sorrySkill).getIncomingQueue().take();
		} catch (InterruptedException e) {
			fail();
		}
		assertTrue("Message in Queue is of type StopMessage", stopMessage instanceof StopMessage);
		assertTrue("stopMessage is closing", ((StopMessage) stopMessage).isStopAndClose() == true);
		assertTrue("stopMessage creator is still null", ((StopMessage) stopMessage).getCreator() == null);

		assertTrue("skillRegistry is set to be closed", skillRegistry.isClosed == true);
		assertTrue("SkillRegistry has closed", thread.isAlive() == false);
	}
	
	@Test
	public void testFeedbackMessage() {

		SpeechMessage speechMessage = new SpeechMessage();
		speechMessage.setSkillRef(clockSkill);
		FeedbackMessage feedbackerMessage = new FeedbackMessage(speechMessage);
		
		try {
			skillRegistry.getIncomingQueue().put(feedbackerMessage);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		AMessage feedbackMessage = null;

		try {
			feedbackMessage = ((AMessageProcessor) clockSkill).getIncomingQueue().take();
		} catch (InterruptedException e) {
			fail();
		}
		assertTrue("Message in clockSkill Queue is of type FeedbackMessage", feedbackMessage instanceof FeedbackMessage);

		assertTrue("SystemSkill has not received a message yet",
				((AMessageProcessor) systemSkill).getIncomingQueue().size() == 0);
	
		assertTrue("SorrySkill has not received a message yet",
				((AMessageProcessor) sorrySkill).getIncomingQueue().size() == 0);

	}
	
}
