package de.iisys.pippa.system.resolver;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iisys.pippa.core.message.speech_message.SpeechMessage;
import de.iisys.pippa.core.message.stop_message.StopMessage;
import de.iisys.pippa.core.message.unschedule_message.UnscheduleMessage;
import de.iisys.pippa.core.message_processor.AMessageProcessor;
import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.status.StatusAccess;
import de.iisys.pippa.skill.clock.ClockSkillImpl;
import de.iisys.pippa.skill.sorry.SorrySkillImpl;
import de.iisys.pippa.skill.system_access.SystemAccessSkillImpl;
import de.iisys.pippa.support.status.Status;

public class ResolverTest {

	static StatusAccess status = null;
	static ResolverImpl resolver = null;
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
		resolver = new ResolverImpl(status);
		speechMessage = new SpeechMessage();

		thread = new Thread(resolver);
		thread.start();

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1_2_3() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(false);
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);
		
		ResolverTest.status.setDialogRunning(false);
		ResolverTest.status.setDialogWith(null);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains 1 message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertFalse(ResolverTest.status.getDialogRunning());
		assertNull(ResolverTest.status.getDialogWith());
	}
	
	@Test
	public void test4_5_6() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(true);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(false);
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(false);
		ResolverTest.status.setDialogWith(null);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains 1 message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertTrue(ResolverTest.status.getDialogRunning());
		assertTrue(ResolverTest.status.getDialogWith() == ResolverTest.clockSkill);
	}

	@Test
	public void test8_9_10() {

		ResolverTest.speechMessage.setLongRunning(true);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(false);
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);
		
		ResolverTest.status.setDialogRunning(false);
		ResolverTest.status.setDialogWith(null);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains 1 message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertFalse(ResolverTest.status.getDialogRunning());
		assertNull(ResolverTest.status.getDialogWith());
	}
	
	@Test
	public void test11_12_13() {

		ResolverTest.speechMessage.setLongRunning(true);
		ResolverTest.speechMessage.setStartDialogRequest(true);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(false);
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(false);
		ResolverTest.status.setDialogWith(null);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains 1 message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertTrue(ResolverTest.status.getDialogRunning());
		assertTrue(ResolverTest.status.getDialogWith() == ResolverTest.clockSkill);
	}
	
	@Test
	public void test15_16_17() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(false);
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(true);
		ResolverTest.status.setDialogWith(ResolverTest.systemSkill);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains 1 message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertTrue(ResolverTest.status.getDialogRunning());
		assertTrue(ResolverTest.status.getDialogWith() == ResolverTest.systemSkill);
	}
	
	@Test
	public void test18_19_20() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(true);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(false);
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(true);
		ResolverTest.status.setDialogWith(ResolverTest.systemSkill);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains 1 message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);System.out.println(resolver.getSkillRegistryQueue().size());
		assertTrue("SkillRegistryQueue contains 2 messages", resolver.getSkillRegistryQueue().size() == 2);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertTrue(ResolverTest.status.getDialogRunning());
		assertTrue(ResolverTest.status.getDialogWith() == ResolverTest.clockSkill);
	}
	
	@Test
	public void test22_23_24() {

		ResolverTest.speechMessage.setLongRunning(true);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(false);
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(true);
		ResolverTest.status.setDialogWith(ResolverTest.systemSkill);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains 1 message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertFalse(ResolverTest.status.getDialogRunning());
		assertNull(ResolverTest.status.getDialogWith());
	}
	

	@Test
	public void test25_26_27() {

		ResolverTest.speechMessage.setLongRunning(true);
		ResolverTest.speechMessage.setStartDialogRequest(true);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(false);
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(true);
		ResolverTest.status.setDialogWith(ResolverTest.systemSkill);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains 1 message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 2 messages", resolver.getSkillRegistryQueue().size() == 2);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertTrue(ResolverTest.status.getDialogRunning());
		assertTrue(ResolverTest.status.getDialogWith() == ResolverTest.clockSkill);
	}
	
	@Test
	public void test29_30_31() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(true);
		ResolverTest.speechMessage.setFutureExecutionRequest(false);
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(true);
		ResolverTest.status.setDialogWith(ResolverTest.clockSkill);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains 1 message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertFalse(ResolverTest.status.getDialogRunning());
		assertNull(ResolverTest.status.getDialogWith());
	}
	
	@Test
	public void test32_33_34() {

		ResolverTest.speechMessage.setLongRunning(true);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(true);
		ResolverTest.speechMessage.setFutureExecutionRequest(false);
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(true);
		ResolverTest.status.setDialogWith(ResolverTest.clockSkill);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains 1 message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertFalse(ResolverTest.status.getDialogRunning());
		assertNull(ResolverTest.status.getDialogWith());
	}
	
	@Test
	public void test36_37_38() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(false);
		ResolverTest.speechMessage.setPresenceRequest(true);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(false);
		ResolverTest.status.setDialogWith(null);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains 1 message", resolver.waitingPresenceMessages.size() == 1);
		
		assertFalse(ResolverTest.status.getDialogRunning());
		assertNull(ResolverTest.status.getDialogWith());
	}
	
	@Test
	public void test39_40_41() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(true);
		ResolverTest.speechMessage.setExecutionDate(LocalDateTime.now().plusMinutes(3));
		ResolverTest.speechMessage.setPresenceRequest(true);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(false);
		ResolverTest.status.setDialogWith(null);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains 1 message", resolver.getSchedulerQueue().size() == 1);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertFalse(ResolverTest.status.getDialogRunning());
		assertNull(ResolverTest.status.getDialogWith());
	}
	
	@Test
	public void test43_44_45() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(true);
		ResolverTest.speechMessage.setExecutionDate(LocalDateTime.now().minusMinutes(3));
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(false);
		ResolverTest.status.setDialogWith(null);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains 1 message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertFalse(ResolverTest.status.getDialogRunning());
		assertNull(ResolverTest.status.getDialogWith());
	}
	
	@Test
	public void test46_47_48() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(true);
		ResolverTest.speechMessage.setExecutionDate(LocalDateTime.now().minusMinutes(3));
		ResolverTest.speechMessage.setPresenceRequest(true);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(false);
		ResolverTest.status.setDialogWith(null);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains 1 message", resolver.waitingPresenceMessages.size() == 1);
		
		assertFalse(ResolverTest.status.getDialogRunning());
		assertNull(ResolverTest.status.getDialogWith());
	}
	
	@Test
	public void test50_51_52() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(true);
		ResolverTest.speechMessage.setExecutionDate(LocalDateTime.now().minusMinutes(3));
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(true);
		ResolverTest.status.setDialogWith(ResolverTest.systemSkill);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains 1 message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertTrue(ResolverTest.status.getDialogRunning());
		assertTrue(ResolverTest.status.getDialogWith() == ResolverTest.systemSkill);
	}
	
	@Test
	public void test53_54_55() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(true);
		ResolverTest.speechMessage.setExecutionDate(LocalDateTime.now().minusMinutes(3));
		ResolverTest.speechMessage.setPresenceRequest(true);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(true);
		ResolverTest.status.setDialogWith(ResolverTest.systemSkill);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains 1 message", resolver.waitingPresenceMessages.size() == 1);
		
		assertTrue(ResolverTest.status.getDialogRunning());
		assertTrue(ResolverTest.status.getDialogWith() == ResolverTest.systemSkill);
	}
	
	@Test
	public void testF1() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(true);
		ResolverTest.speechMessage.setFutureExecutionRequest(false);
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(true);
		ResolverTest.status.setDialogWith(ResolverTest.systemSkill);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertTrue(ResolverTest.status.getDialogRunning());
		assertTrue(ResolverTest.status.getDialogWith() == ResolverTest.systemSkill);
	}
	
	@Test
	public void testF2() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(true);
		ResolverTest.speechMessage.setFutureExecutionRequest(false);
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(false);
		ResolverTest.status.setDialogWith(null);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertFalse(ResolverTest.status.getDialogRunning());
		assertNull(ResolverTest.status.getDialogWith());
	}
	
	@Test
	public void testF3() {

		ResolverTest.speechMessage.setLongRunning(true);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(false);
		ResolverTest.speechMessage.setPresenceRequest(true);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(false);
		ResolverTest.status.setDialogWith(null);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertFalse(ResolverTest.status.getDialogRunning());
		assertNull(ResolverTest.status.getDialogWith());
	}
	
	@Test
	public void testF4() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(true);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(false);
		ResolverTest.speechMessage.setPresenceRequest(true);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(false);
		ResolverTest.status.setDialogWith(null);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertFalse(ResolverTest.status.getDialogRunning());
		assertNull(ResolverTest.status.getDialogWith());
	}
	
	@Test
	public void testF5() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(true);
		ResolverTest.speechMessage.setFutureExecutionRequest(false);
		ResolverTest.speechMessage.setPresenceRequest(true);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(false);
		ResolverTest.status.setDialogWith(null);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertFalse(ResolverTest.status.getDialogRunning());
		assertNull(ResolverTest.status.getDialogWith());
	}
	
	@Test
	public void testF6() {

		ResolverTest.speechMessage.setLongRunning(true);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(true);
		ResolverTest.speechMessage.setExecutionDate(LocalDateTime.now().minusMinutes(3));
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(false);
		ResolverTest.status.setDialogWith(null);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertFalse(ResolverTest.status.getDialogRunning());
		assertNull(ResolverTest.status.getDialogWith());
	}
	
	@Test
	public void testF7() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(true);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(true);
		ResolverTest.speechMessage.setExecutionDate(LocalDateTime.now().minusMinutes(3));
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(false);
		ResolverTest.status.setDialogWith(null);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertFalse(ResolverTest.status.getDialogRunning());
		assertNull(ResolverTest.status.getDialogWith());
	}
	
	@Test
	public void testF8() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(true);
		ResolverTest.speechMessage.setFutureExecutionRequest(true);
		ResolverTest.speechMessage.setExecutionDate(LocalDateTime.now().minusMinutes(3));
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(false);
		ResolverTest.status.setDialogWith(null);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertFalse(ResolverTest.status.getDialogRunning());
		assertNull(ResolverTest.status.getDialogWith());
	}
	
	@Test
	public void testF9() {

		ResolverTest.speechMessage.setLongRunning(true);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(true);
		ResolverTest.speechMessage.setExecutionDate(LocalDateTime.now().minusMinutes(3));
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(true);
		ResolverTest.status.setDialogWith(ResolverTest.systemSkill);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertTrue(ResolverTest.status.getDialogRunning());
		assertTrue(ResolverTest.status.getDialogWith() == ResolverTest.systemSkill);
	}
	
	@Test
	public void testF10() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(true);
		ResolverTest.speechMessage.setStopDialogRequest(false);
		ResolverTest.speechMessage.setFutureExecutionRequest(true);
		ResolverTest.speechMessage.setExecutionDate(LocalDateTime.now().minusMinutes(3));
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(true);
		ResolverTest.status.setDialogWith(ResolverTest.systemSkill);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertTrue(ResolverTest.status.getDialogRunning());
		assertTrue(ResolverTest.status.getDialogWith() == ResolverTest.systemSkill);
	}
	
	@Test
	public void testF11() {

		ResolverTest.speechMessage.setLongRunning(false);
		ResolverTest.speechMessage.setStartDialogRequest(false);
		ResolverTest.speechMessage.setStopDialogRequest(true);
		ResolverTest.speechMessage.setFutureExecutionRequest(true);
		ResolverTest.speechMessage.setExecutionDate(LocalDateTime.now().minusMinutes(3));
		ResolverTest.speechMessage.setPresenceRequest(false);
		ResolverTest.speechMessage.setSkillRef(clockSkill);

		ResolverTest.status.setDialogRunning(true);
		ResolverTest.status.setDialogWith(ResolverTest.systemSkill);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.pause(500);
				
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("Waitinglist for Presence contains no message", resolver.waitingPresenceMessages.size() == 0);
		
		assertTrue(ResolverTest.status.getDialogRunning());
		assertTrue(ResolverTest.status.getDialogWith() == ResolverTest.systemSkill);
	}
	
	
	@Test
	public void testStopMessage() {

		StopMessage stopMessage = new StopMessage((AMessageProcessor) clockSkill, false);

		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);

		try {
			resolver.getIncomingQueue().put(stopMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("ExecutorQueue contains 1 message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains 1 message", resolver.getSchedulerQueue().size() == 1);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);
		assertTrue("Resolver is not set to close", resolver.isClosed == false);
		assertTrue("Resolver has not closed", thread.isAlive() == true);
	}

	@Test
	public void testStopAndCloseMessage() {

		StopMessage stopMessage = new StopMessage((AMessageProcessor) clockSkill, true);

		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);

		try {
			resolver.getIncomingQueue().put(stopMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("ExecutorQueue contains 1 message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains 1 message", resolver.getSchedulerQueue().size() == 1);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);
		assertTrue("Resolver is set to close", resolver.isClosed == true);
		assertTrue("Resolver has closed", thread.isAlive() == false);

	}

	@Test
	public void testSingleMessage() {

		speechMessage = new SpeechMessage();
		speechMessage.setPresenceRequest(false);
		speechMessage.setFutureExecutionRequest(false);
		speechMessage.setStartDialogRequest(false);

		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("ExecutorQueue contains 1 message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);

	}

	@Test
	public void testSingleMessageFuture() {

		speechMessage = new SpeechMessage();
		speechMessage.setPresenceRequest(false);
		speechMessage.setFutureExecutionRequest(true);
		speechMessage.setExecutionDate(LocalDateTime.now().plusHours(1));
		speechMessage.setStartDialogRequest(false);
		speechMessage.setSkillRef(clockSkill);

		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains 1 message", resolver.getSchedulerQueue().size() == 1);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);

	}

	@Test
	public void testSingleMessageFutureButExpired() {

		speechMessage = new SpeechMessage();
		speechMessage.setPresenceRequest(false);
		speechMessage.setFutureExecutionRequest(true);
		speechMessage.setExecutionDate(LocalDateTime.now().minusHours(1));
		speechMessage.setStartDialogRequest(false);

		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 0);

	}

	@Test
	public void testSingleMessagePresence() {

		speechMessage = new SpeechMessage();
		speechMessage.setPresenceRequest(true);
		speechMessage.setFutureExecutionRequest(false);
		speechMessage.setStartDialogRequest(false);
		speechMessage.setSkillRef(clockSkill);

		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("1 message is waiting in Presence-List", resolver.waitingPresenceMessages.size() == 1);
	}

	@Test
	public void testSingleMessagePresenceAndFuture() {

		speechMessage = new SpeechMessage();
		speechMessage.setPresenceRequest(true);
		speechMessage.setFutureExecutionRequest(true);
		speechMessage.setExecutionDate(LocalDateTime.now().plusHours(1));
		speechMessage.setStartDialogRequest(false);
		speechMessage.setSkillRef(clockSkill);

		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains 1 message", resolver.getSchedulerQueue().size() == 1);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("0 message is waiting in Presence-List", resolver.waitingPresenceMessages.size() == 0);
	}

	@Test
	public void testSingleMessagePresenceAndFutureButExpired() {

		speechMessage = new SpeechMessage();
		speechMessage.setPresenceRequest(true);
		speechMessage.setFutureExecutionRequest(true);
		speechMessage.setExecutionDate(LocalDateTime.now().minusHours(1));
		speechMessage.setStartDialogRequest(false);
		speechMessage.setSkillRef(clockSkill);
		
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 1 message", resolver.getSkillRegistryQueue().size() == 1);
		assertTrue("1 message is waiting in Presence-List", resolver.waitingPresenceMessages.size() == 1);
	}

	@Test
	public void testPresenceMessagesSentAfterMessage() {

		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);
		
		
		for (int i = 0; i < 3; i++) {
			speechMessage = new SpeechMessage();
			speechMessage.setPresenceRequest(true);
			speechMessage.setFutureExecutionRequest(false);
			speechMessage.setStartDialogRequest(false);
			speechMessage.setSkillRef(clockSkill);
			
			try {
				resolver.getIncomingQueue().put(speechMessage);
			} catch (InterruptedException e) {
				fail();
			}

		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}
		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 3 messages", resolver.getSkillRegistryQueue().size() == 3);
		assertTrue("3 messages are waiting in Presence-List", resolver.waitingPresenceMessages.size() == 3);

		speechMessage = new SpeechMessage();
		speechMessage.setPresenceRequest(false);
		speechMessage.setFutureExecutionRequest(false);
		speechMessage.setStartDialogRequest(false);
		speechMessage.setSkillRef(clockSkill);

		try {
			resolver.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("ExecutorQueue contains 4 messagse", resolver.getExecutorQueue().size() == 4);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 3 messages", resolver.getSkillRegistryQueue().size() == 3);
		assertTrue("0 messages are waiting in Presence-List", resolver.waitingPresenceMessages.size() == 0);
	}

	@Test
	public void testMultipleMessages() {

		SpeechMessage copyMessage = null;
		Skill[] dispatchedSkills = { clockSkill, systemSkill, sorrySkill };
		speechMessage = new SpeechMessage();
		speechMessage.setDispatchedSkills(dispatchedSkills);

		copyMessage = speechMessage.copy();
		copyMessage.setSkillRef(clockSkill);
		try {
			resolver.getIncomingQueue().put(copyMessage);
		} catch (InterruptedException e) {
			fail();
		}

		copyMessage = speechMessage.copy();
		copyMessage.setSkillRef(systemSkill);
		try {
			resolver.getIncomingQueue().put(copyMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);

		copyMessage = speechMessage.copy();
		copyMessage.setSkillRef(sorrySkill);
		try {
			resolver.getIncomingQueue().put(copyMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("ExecutorQueue contains 1 message", resolver.getExecutorQueue().size() == 1);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains 2 messages", resolver.getSkillRegistryQueue().size() == 2);

	}

	@Test
	public void testUnscheduleMessage() {

		UnscheduleMessage unscheduleMessage = new UnscheduleMessage(clockSkill, UUID.randomUUID());

		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains no message", resolver.getSchedulerQueue().size() == 0);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);

		try {
			resolver.getIncomingQueue().put(unscheduleMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue("ExecutorQueue contains no message", resolver.getExecutorQueue().size() == 0);
		assertTrue("SchedulerQueue contains 1 message", resolver.getSchedulerQueue().size() == 1);
		assertTrue("DispatcherQueue contains no message", resolver.getDispatcherQueue().size() == 0);
		assertTrue("SkillRegistryQueue contains no message", resolver.getSkillRegistryQueue().size() == 0);

	}

	private void pause(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			fail();
		}
	}

}
