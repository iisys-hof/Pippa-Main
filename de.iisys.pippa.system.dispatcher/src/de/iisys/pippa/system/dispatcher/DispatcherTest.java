package de.iisys.pippa.system.dispatcher;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message.speech_message.SpeechMessage;
import de.iisys.pippa.core.message.stop_message.StopMessage;
import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.status.StatusAccess;
import de.iisys.pippa.skill.clock.ClockSkillImpl;
import de.iisys.pippa.skill.sorry.SorrySkillImpl;
import de.iisys.pippa.skill.system_access.SystemAccessSkillImpl;
import de.iisys.pippa.support.status.Status;

public class DispatcherTest {

	static StatusAccess status = null;
	static DispatcherImpl dispatcher = null;
	static Skill clockSkill = null;
	static Skill sorrySkill = null;
	static Skill systemSkill = null;
	static SpeechMessage speechMessage = null;
	static BlockingQueue<AMessage> controlQueue = null;
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
		dispatcher = new DispatcherImpl(status, sorrySkill, systemSkill);
		speechMessage = new SpeechMessage();
		controlQueue = new LinkedBlockingQueue<AMessage>();

		thread = new Thread(dispatcher);
		thread.start();
		
		Thread.sleep(100);
		
		dispatcher.registerRegexes(clockSkill);
		dispatcher.registerRegexes(sorrySkill);
		dispatcher.registerRegexes(systemSkill);
		dispatcher.setOutgoingQueue(controlQueue);

		
	}

	@SuppressWarnings("deprecation")
	@After
	public void tearDown() throws Exception {
		status = null;
		dispatcher = null;
		clockSkill = null;
		sorrySkill = null;
		systemSkill = null;
		speechMessage = null;
		controlQueue = null;
		if (thread != null) {
			thread.stop();
		}
	}

	@Test
	public void testSimpleMatchingMessage() {

		speechMessage.setAudioText("what time is it");
		speechMessage.setReturnToDispatcher(false);
		status.setDialogRunning(false);

		assertTrue("Dispatcher OutgoingQueue is empty", dispatcher.getOutgoingQueue().size() == 0);

		try {
			dispatcher.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("Dispatcher OutgoingQueue contains 1 message", dispatcher.getOutgoingQueue().size() == 1);
		assertTrue("Message contains single Skill as receiver", speechMessage.getDispatchedSkills().length == 1);
		assertTrue("Message contains ClockSkill as receiver", speechMessage.getDispatchedSkills()[0] == clockSkill);
		assertTrue("Message contains one matched Regex entry", speechMessage.getMatchedRegexes().size() == 1);
		assertTrue("Message contains clockSkill as matched Regex entry",
				speechMessage.getMatchedRegexes().containsKey(clockSkill));
		assertTrue("Status was set by Dispatcher", status.getLastDispatchedSkills() == speechMessage.getDispatchedSkills());
	}

	@Test
	public void testSimpleNonMatchingMessage() {

		speechMessage.setAudioText("I do not contain a matching expression");
		speechMessage.setReturnToDispatcher(false);
		status.setDialogRunning(false);

		assertTrue("Dispatcher OutgoingQueue is empty", dispatcher.getOutgoingQueue().size() == 0);

		try {
			dispatcher.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("Dispatcher OutgoingQueue contains 1 message", dispatcher.getOutgoingQueue().size() == 1);
		assertTrue("Message contains single Skill as receiver", speechMessage.getDispatchedSkills().length == 1);
		assertTrue("Message contains SorrySkill as receiver", speechMessage.getDispatchedSkills()[0] == sorrySkill);
		assertTrue("Message contains no matched Regex entry", speechMessage.getMatchedRegexes().size() == 0);
		assertTrue("Status was set by Dispatcher", status.getLastDispatchedSkills() == speechMessage.getDispatchedSkills());
	}

	@Test
	public void testSimpleDialogMessage() {

		speechMessage.setAudioText("I contain an expression that does not matter in dialog mode");
		speechMessage.setReturnToDispatcher(false);
		status.setDialogRunning(true);
		status.setDialogWith(clockSkill);

		assertTrue("Dispatcher OutgoingQueue is empty", dispatcher.getOutgoingQueue().size() == 0);

		try {
			dispatcher.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("Dispatcher OutgoingQueue contains 1 message", dispatcher.getOutgoingQueue().size() == 1);
		assertTrue("Message contains single Skill as receiver", speechMessage.getDispatchedSkills().length == 1);
		assertTrue("Message contains ClockSkill as receiver", speechMessage.getDispatchedSkills()[0] == clockSkill);
		assertTrue("Message contains no matched Regex entry", speechMessage.getMatchedRegexes().size() == 0);
		assertTrue("Status was set by Dispatcher", status.getLastDispatchedSkills() == speechMessage.getDispatchedSkills());
	}

	@Test
	public void testMatchingReturnMessage() {

		speechMessage.setAudioText("what time is it");
		speechMessage.setReturnToDispatcher(true);
		status.setDialogRunning(true);
		status.setDialogWith(null);

		assertTrue("Dispatcher OutgoingQueue is empty", dispatcher.getOutgoingQueue().size() == 0);

		try {
			dispatcher.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("Dispatcher OutgoingQueue contains 1 message", dispatcher.getOutgoingQueue().size() == 1);
		assertTrue("Message contains single Skill as receiver", speechMessage.getDispatchedSkills().length == 1);
		assertTrue("Message contains ClockSkill as receiver", speechMessage.getDispatchedSkills()[0] == clockSkill);
		assertTrue("Message contains one matched Regex entry", speechMessage.getMatchedRegexes().size() == 1);
		assertTrue("Message contains clockSkill as matched Regex entry",
				speechMessage.getMatchedRegexes().containsKey(clockSkill));
		assertTrue("Status was set by Dispatcher", status.getLastDispatchedSkills() == speechMessage.getDispatchedSkills());
	}

	@Test
	public void testNonMatchingReturnMessage() {

		speechMessage.setAudioText("I do not contain a matching expression");
		speechMessage.setReturnToDispatcher(true);
		status.setDialogRunning(true);
		status.setDialogWith(null);

		assertTrue("Dispatcher OutgoingQueue is empty", dispatcher.getOutgoingQueue().size() == 0);

		try {
			dispatcher.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("Dispatcher OutgoingQueue contains 1 message", dispatcher.getOutgoingQueue().size() == 1);
		assertTrue("Message contains single Skill as receiver", speechMessage.getDispatchedSkills().length == 1);
		assertTrue("Message contains SorrySkill as receiver", speechMessage.getDispatchedSkills()[0] == sorrySkill);
		assertTrue("Message contains no matched Regex entry", speechMessage.getMatchedRegexes().size() == 0);
		assertTrue("Status was set by Dispatcher", status.getLastDispatchedSkills() == speechMessage.getDispatchedSkills());
	}

	@Test
	public void testSimpleMessageWithSystemRegex() {

		speechMessage.setAudioText("turn the volume down");
		speechMessage.setReturnToDispatcher(false);
		status.setDialogRunning(false);

		assertTrue("Dispatcher OutgoingQueue is empty", dispatcher.getOutgoingQueue().size() == 0);

		try {
			dispatcher.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("Dispatcher OutgoingQueue contains 1 message", dispatcher.getOutgoingQueue().size() == 1);
		assertTrue("Message contains single Skill as receiver", speechMessage.getDispatchedSkills().length == 1);
		assertTrue("Message contains SystemSkill as receiver", speechMessage.getDispatchedSkills()[0] == systemSkill);
		assertTrue("Message contains one matched Regex entry", speechMessage.getMatchedRegexes().size() == 1);
		assertTrue("Message contains systemSkill as matched Regex entry",
				speechMessage.getMatchedRegexes().containsKey(systemSkill));
		assertTrue("Status was set by Dispatcher", status.getLastDispatchedSkills() == speechMessage.getDispatchedSkills());
	}

	@Test
	public void testDialogMessageWithSystemRegex() {

		speechMessage.setAudioText("turn the volume down");
		speechMessage.setReturnToDispatcher(false);
		status.setDialogRunning(true);
		status.setDialogWith(clockSkill);

		assertTrue("Dispatcher OutgoingQueue is empty", dispatcher.getOutgoingQueue().size() == 0);

		try {
			dispatcher.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("Dispatcher OutgoingQueue contains 1 message", dispatcher.getOutgoingQueue().size() == 1);
		assertTrue("Message contains single Skill as receiver", speechMessage.getDispatchedSkills().length == 1);
		assertTrue("Message contains SystemSkill as receiver", speechMessage.getDispatchedSkills()[0] == systemSkill);
		assertTrue("Message contains no Regex entry", speechMessage.getMatchedRegexes().size() == 0);
		assertTrue("Status was set by Dispatcher", status.getLastDispatchedSkills() == speechMessage.getDispatchedSkills());
	}

	@Test
	public void testSimpleMessageWithMultipleMatches() {

		speechMessage.setAudioText("it's time to turn the volume down");
		speechMessage.setReturnToDispatcher(false);
		status.setDialogRunning(false);

		assertTrue("Dispatcher OutgoingQueue is empty", dispatcher.getOutgoingQueue().size() == 0);

		try {
			dispatcher.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("Dispatcher OutgoingQueue contains 1 message", dispatcher.getOutgoingQueue().size() == 1);
		assertTrue("Message contains two Skills as receiver", speechMessage.getDispatchedSkills().length == 2);
		assertTrue("Message contains SystemSkill as receiver",
				Arrays.asList(speechMessage.getDispatchedSkills()).contains(systemSkill));
		assertTrue("Message contains ClockSkill as receiver",
				Arrays.asList(speechMessage.getDispatchedSkills()).contains(clockSkill));
		assertTrue("Message contains two matched Regex entry", speechMessage.getMatchedRegexes().size() == 2);
		assertTrue("Message contains systemSkill as matched Regex entry",
				speechMessage.getMatchedRegexes().containsKey(systemSkill));
		assertTrue("Message contains clockSkill as matched Regex entry",
				speechMessage.getMatchedRegexes().containsKey(clockSkill));
		assertTrue("Status was set by Dispatcher", status.getLastDispatchedSkills() == speechMessage.getDispatchedSkills());
	}

	@Test
	public void testSimpleStopMessage() {

		speechMessage.setAudioText("stop what you are doing");
		speechMessage.setReturnToDispatcher(false);
		status.setDialogRunning(false);

		assertTrue("Dispatcher OutgoingQueue is empty", dispatcher.getOutgoingQueue().size() == 0);

		try {
			dispatcher.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("Dispatcher OutgoingQueue contains 1 message", dispatcher.getOutgoingQueue().size() == 1);
		assertTrue("Message contains no Skill as receiver", speechMessage.getDispatchedSkills().length == 0);
		assertTrue("Message contains no matched Regex entry", speechMessage.getMatchedRegexes().size() == 0);

		AMessage stopMessage = null;
		try {
			stopMessage = dispatcher.getOutgoingQueue().take();
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("Message in Queue is of type StopMessage", stopMessage instanceof StopMessage);
		assertTrue("stopMessage is not closing", ((StopMessage) stopMessage).isStopAndClose() == false);
		assertTrue("stopMessage creator is dispatcher", ((StopMessage) stopMessage).getCreator() == dispatcher);
	
	}

	@Test
	public void testDialogStopMessage() {

		speechMessage.setAudioText("stop what you are doing");
		speechMessage.setReturnToDispatcher(false);
		status.setDialogRunning(true);

		assertTrue("Dispatcher OutgoingQueue is empty", dispatcher.getOutgoingQueue().size() == 0);

		try {
			dispatcher.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("Dispatcher OutgoingQueue contains 1 message", dispatcher.getOutgoingQueue().size() == 1);
		assertTrue("Message contains no Skill as receiver", speechMessage.getDispatchedSkills().length == 0);
		assertTrue("Message contains no matched Regex entry", speechMessage.getMatchedRegexes().size() == 0);

		AMessage stopMessage = null;
		try {
			stopMessage = dispatcher.getOutgoingQueue().take();
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("Message in Queue is of type StopMessage", stopMessage instanceof StopMessage);
		assertTrue("stopMessage is not closing", ((StopMessage) stopMessage).isStopAndClose() == false);
		assertTrue("stopMessage creator is dispatcher", ((StopMessage) stopMessage).getCreator() == dispatcher);
		assertTrue("Status DialogRunning was set to false", status.getDialogRunning() == false);
	
	}

	@Test
	public void testStopMessageAsReceiver() {

		status.setDialogRunning(true);
		
		StopMessage stopperMessage = new StopMessage(null, false);

		assertTrue("Dispatcher OutgoingQueue is empty", dispatcher.getOutgoingQueue().size() == 0);

		try {
			dispatcher.getIncomingQueue().put(stopperMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("Dispatcher OutgoingQueue contains 1 message", dispatcher.getOutgoingQueue().size() == 1);

		AMessage stopMessage = null;
		try {
			stopMessage = dispatcher.getOutgoingQueue().take();
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("Message in Queue is of type StopMessage", stopMessage instanceof StopMessage);
		assertTrue("stopMessage is not closing", ((StopMessage) stopMessage).isStopAndClose() == false);
		assertTrue("stopMessage creator is still null", ((StopMessage) stopMessage).getCreator() == null);
		assertTrue("Dispatcher is not set to be closed", dispatcher.isClosed == false);
		assertTrue("Dispatcher has not closed", thread.isAlive() == true);
		assertTrue("Status DialogRunning was set to false", status.getDialogRunning() == false);
	}

	@Test
	public void testStopAndCloseMessageAsReceiver() {

		StopMessage stopperMessage = new StopMessage(null, true);

		assertTrue("Dispatcher OutgoingQueue is empty", dispatcher.getOutgoingQueue().size() == 0);

		try {
			dispatcher.getIncomingQueue().put(stopperMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("Dispatcher OutgoingQueue contains 1 message", dispatcher.getOutgoingQueue().size() == 1);

		AMessage stopMessage = null;
		try {
			stopMessage = dispatcher.getOutgoingQueue().take();
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("Message in Queue is of type StopMessage", stopMessage instanceof StopMessage);
		assertTrue("stopMessage is closing", ((StopMessage) stopMessage).isStopAndClose() == true);
		assertTrue("stopMessage creator is still null", ((StopMessage) stopMessage).getCreator() == null);
		assertTrue("Dispatcher is set to be closed", dispatcher.isClosed == true);
		assertTrue("Dispatcher has closed", thread.isAlive() == false);
	}
}
