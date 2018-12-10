package de.iisys.pippa.system.executor;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iisys.pippa.core.message.speech_message.SpeechMessage;
import de.iisys.pippa.core.message.stop_message.StopMessage;
import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.skill_executable.ASkillExecutable;
import de.iisys.pippa.core.status.StatusAccess;
import de.iisys.pippa.service.speech_out.SpeechOutImpl;
import de.iisys.pippa.skill.clock.ClockSkillImpl;
import de.iisys.pippa.support.status.Status;

public class ExecutorTest {

	StatusAccess status = null;
	ASkillExecutable executable1 = null;
	ASkillExecutable executable2 = null;
	ExecutorImpl executor = null;
	SpeechOutImpl speechOut = null;
	Thread t1 = null;
	Thread t2 = null;
	Logger log = Logger.getAnonymousLogger();
	Skill clockSkill1 = null;
	Skill clockSkill2 = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

		executable1 = new TestExecutable("exec1");
		executable2 = new TestExecutable("exec2");
		status = Status.getInstance();
		this.speechOut = new SpeechOutImpl();
		this.executor = new ExecutorImpl(this.speechOut, status, this.log);
		clockSkill1 = new ClockSkillImpl();
		clockSkill2 = new ClockSkillImpl();

		this.t1 = new Thread(this.executor);
		this.t2 = new Thread(this.speechOut);
		t1.start();
		t2.start();

	}

	@SuppressWarnings("deprecation")
	@After
	public void tearDown() throws Exception {

		this.executable1 = null;
		this.executable2 = null;
		status.reset();
		status = null;
		this.speechOut = null;
		this.executor = null;
		clockSkill1 = null;
		clockSkill2 = null;

		t1.stop();
		t2.stop();
		t1 = null;
		t2 = null;

	}

	/*
	 * executes a single short executable and sends feedback
	 */
	@Test
	public void test1_3_15_18_29_43_50() {

		SpeechMessage speechMessage = new SpeechMessage();
		speechMessage.setLongRunning(false);
		speechMessage.setSkillRef(clockSkill1);
		speechMessage.setSkillExecutable(executable1);

		try {
			this.executor.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		this.pause(500); // takes some time to start execution, executions takes about 1000ms, so this
							// should be a good time to check on the executor

		assertEquals(this.executor.shortExecutable, executable1); // short executable currently running in executor
		assertNotNull(this.executor.shortThread); // thread that executable is running in
		assertEquals(this.executor.shortSkill, clockSkill1); // skill this executable stems from
		assertTrue(this.executor.waitingShortMessages.size() == 0); // further short messages waiting

		assertNull(this.executor.longExecutable); // long executable currently running in executor
		assertNull(this.executor.longThread); // thread that executable is running in
		assertNull(this.executor.longSkill); // skill this executable stems from
		assertNull(this.executor.waitingLongMessage); // further long messages waiting

		assertTrue(this.executor.getSkillRegistryQueue().size() == 1); // number of feedback messages sent to registry,
																		// usually one at start of an executable and one
																		// at the end

		assertEquals(this.status.getRunningShortExecutable(), executable1); // status reflects which short executable is
																			// running
		assertNull(this.status.getLastShortExecutable()); // status reflects which short executable ran before
		assertNull(this.status.getRunningLongExecutable()); // status reflects which long executable is running
		assertNull(this.status.getLastLongExecutable()); // status reflects which long executable ran before

		this.pause(1500); // the executable should be finished by now

		assertNull(this.executor.shortExecutable);
		assertNull(this.executor.shortThread);
		assertNull(this.executor.shortSkill);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		assertNull(this.executor.longExecutable);
		assertNull(this.executor.longThread);
		assertNull(this.executor.longSkill);
		assertNull(this.executor.waitingLongMessage);

		assertTrue(this.executor.getSkillRegistryQueue().size() == 2);

		assertNull(this.status.getRunningShortExecutable());
		assertEquals(this.status.getLastShortExecutable(), executable1);
		assertNull(this.status.getRunningLongExecutable());
		assertNull(this.status.getLastLongExecutable());
	}

	/*
	 * executes a single long executable and sends feedback
	 */
	@Test
	public void test8_11_22_25_32() {

		SpeechMessage speechMessage = new SpeechMessage();
		speechMessage.setLongRunning(true);
		speechMessage.setSkillRef(clockSkill1);
		speechMessage.setSkillExecutable(executable1);

		try {
			this.executor.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		this.pause(500);

		assertNull(this.executor.shortExecutable);
		assertNull(this.executor.shortThread);
		assertNull(this.executor.shortSkill);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		assertEquals(this.executor.longExecutable, this.executable1);
		assertNotNull(this.executor.longThread);
		assertEquals(this.executor.longSkill, this.clockSkill1);
		assertNull(this.executor.waitingLongMessage);

		assertTrue(this.executor.getSkillRegistryQueue().size() == 1);

		assertNull(this.status.getRunningShortExecutable());
		assertNull(this.status.getLastShortExecutable());
		assertEquals(this.status.getRunningLongExecutable(), executable1);
		assertNull(this.status.getLastLongExecutable());

		this.pause(1500);

		assertNull(this.executor.shortExecutable);
		assertNull(this.executor.shortThread);
		assertNull(this.executor.shortSkill);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		assertNull(this.executor.longExecutable);
		assertNull(this.executor.longThread);
		assertNull(this.executor.longSkill);
		assertNull(this.executor.waitingLongMessage);

		assertTrue(this.executor.getSkillRegistryQueue().size() == 2);

		assertNull(this.status.getRunningShortExecutable());
		assertNull(this.status.getLastShortExecutable());
		assertNull(this.status.getRunningLongExecutable());
		assertEquals(this.status.getLastLongExecutable(), executable1);
	}

	/**
	 * queues a short executable while another is being executed, then executes it
	 * afterwards
	 */
	@Test
	public void test2_5_16_19_30_44_51() {

		SpeechMessage speechMessage1 = new SpeechMessage();
		speechMessage1.setLongRunning(false);
		speechMessage1.setSkillRef(clockSkill1);
		speechMessage1.setSkillExecutable(executable1);

		SpeechMessage speechMessage2 = new SpeechMessage();
		speechMessage2.setLongRunning(false);
		speechMessage2.setSkillRef(clockSkill2);
		speechMessage2.setSkillExecutable(executable2);

		try {
			this.executor.getIncomingQueue().put(speechMessage1);
			this.executor.getIncomingQueue().put(speechMessage2);
		} catch (InterruptedException e) {
			fail();
		}

		this.pause(500);

		assertEquals(this.executor.shortExecutable, executable1);
		assertNotNull(this.executor.shortThread);
		assertEquals(this.executor.shortSkill, clockSkill1);
		assertTrue(this.executor.waitingShortMessages.size() == 1);

		assertNull(this.executor.longExecutable);
		assertNull(this.executor.longThread);
		assertNull(this.executor.longSkill);
		assertNull(this.executor.waitingLongMessage);

		assertTrue(this.executor.getSkillRegistryQueue().size() == 1);

		assertEquals(this.status.getRunningShortExecutable(), executable1);
		assertNull(this.status.getLastShortExecutable());
		assertNull(this.status.getRunningLongExecutable());
		assertNull(this.status.getLastLongExecutable());

		this.pause(1000);

		assertEquals(this.executor.shortExecutable, executable2);
		assertNotNull(this.executor.shortThread);
		assertEquals(this.executor.shortSkill, clockSkill2);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		assertNull(this.executor.longExecutable);
		assertNull(this.executor.longThread);
		assertNull(this.executor.longSkill);
		assertNull(this.executor.waitingLongMessage);

		assertTrue(this.executor.getSkillRegistryQueue().size() == 3);

		assertEquals(this.status.getRunningShortExecutable(), executable2);
		assertEquals(this.status.getLastShortExecutable(), this.executable1);
		assertNull(this.status.getRunningLongExecutable());
		assertNull(this.status.getLastLongExecutable());

		this.pause(1000);

		assertNull(this.executor.shortExecutable);
		assertNull(this.executor.shortThread);
		assertNull(this.executor.shortSkill);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		assertNull(this.executor.longExecutable);
		assertNull(this.executor.longThread);
		assertNull(this.executor.longSkill);
		assertNull(this.executor.waitingLongMessage);

		assertTrue(this.executor.getSkillRegistryQueue().size() == 4);

		assertNull(this.status.getRunningShortExecutable());
		assertEquals(this.status.getLastShortExecutable(), this.executable2);
		assertNull(this.status.getRunningLongExecutable());
		assertNull(this.status.getLastLongExecutable());
	}

	/**
	 * queues a long while a short executable is being executed, then executes it
	 * afterwards
	 */
	@Test
	public void test9_12_23_26_33() {

		SpeechMessage speechMessage1 = new SpeechMessage();
		speechMessage1.setLongRunning(false);
		speechMessage1.setSkillRef(clockSkill1);
		speechMessage1.setSkillExecutable(executable1);

		SpeechMessage speechMessage2 = new SpeechMessage();
		speechMessage2.setLongRunning(true);
		speechMessage2.setSkillRef(clockSkill2);
		speechMessage2.setSkillExecutable(executable2);

		try {
			this.executor.getIncomingQueue().put(speechMessage1);
			this.executor.getIncomingQueue().put(speechMessage2);
		} catch (InterruptedException e) {
			fail();
		}

		this.pause(500);

		assertEquals(this.executor.shortExecutable, executable1);
		assertNotNull(this.executor.shortThread);
		assertEquals(this.executor.shortSkill, clockSkill1);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		assertNull(this.executor.longExecutable);
		assertNull(this.executor.longThread);
		assertNull(this.executor.longSkill);
		assertEquals(this.executor.waitingLongMessage, speechMessage2);

		assertTrue(this.executor.getSkillRegistryQueue().size() == 1);

		assertEquals(this.status.getRunningShortExecutable(), executable1);
		assertNull(this.status.getLastShortExecutable());
		assertNull(this.status.getRunningLongExecutable());
		assertNull(this.status.getLastLongExecutable());

		this.pause(1000);

		assertNull(this.executor.shortExecutable);
		assertNull(this.executor.shortThread);
		assertNull(this.executor.shortSkill);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		assertEquals(this.executor.longExecutable, this.executable2);
		assertNotNull(this.executor.longThread);
		assertEquals(this.executor.longSkill, this.clockSkill2);
		assertNull(this.executor.waitingLongMessage);

		assertTrue(this.executor.getSkillRegistryQueue().size() == 3);

		assertNull(this.status.getRunningShortExecutable());
		assertEquals(this.status.getLastShortExecutable(), this.executable1);
		assertEquals(this.status.getRunningLongExecutable(), this.executable2);
		assertNull(this.status.getLastLongExecutable());

		this.pause(1000);

		assertNull(this.executor.shortExecutable);
		assertNull(this.executor.shortThread);
		assertNull(this.executor.shortSkill);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		assertNull(this.executor.longExecutable);
		assertNull(this.executor.longThread);
		assertNull(this.executor.longSkill);
		assertNull(this.executor.waitingLongMessage);

		assertTrue(this.executor.getSkillRegistryQueue().size() == 4);

		assertNull(this.status.getRunningShortExecutable());
		assertEquals(this.status.getLastShortExecutable(), this.executable1);
		assertNull(this.status.getRunningLongExecutable());
		assertEquals(this.status.getLastLongExecutable(), this.executable2);
	}

	/**
	 * executes a short executable in parallel to an already running long executable
	 */
	@Test
	public void test3_17_31_45_52() {

		SpeechMessage speechMessage1 = new SpeechMessage();
		speechMessage1.setLongRunning(false);
		speechMessage1.setSkillRef(clockSkill1);
		speechMessage1.setSkillExecutable(executable1);

		SpeechMessage speechMessage2 = new SpeechMessage();
		speechMessage2.setLongRunning(true);
		speechMessage2.setSkillRef(clockSkill2);
		speechMessage2.setSkillExecutable(executable2);

		try {
			this.executor.getIncomingQueue().put(speechMessage2);
			this.executor.getIncomingQueue().put(speechMessage1);
		} catch (InterruptedException e) {
			fail();
		}

		this.pause(500);

		assertEquals(this.executor.shortExecutable, executable1);
		assertNotNull(this.executor.shortThread);
		assertEquals(this.executor.shortSkill, clockSkill1);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		assertEquals(this.executor.longExecutable, this.executable2);
		assertNotNull(this.executor.longThread);
		assertEquals(this.executor.longSkill, this.clockSkill2);
		assertNull(this.executor.waitingLongMessage);

		assertTrue(this.executor.getSkillRegistryQueue().size() == 2);

		assertEquals(this.status.getRunningShortExecutable(), executable1);
		assertNull(this.status.getLastShortExecutable());
		assertEquals(this.status.getRunningLongExecutable(), executable2);
		assertNull(this.status.getLastLongExecutable());

		this.pause(1500);

		assertNull(this.executor.shortExecutable);
		assertNull(this.executor.shortThread);
		assertNull(this.executor.shortSkill);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		assertNull(this.executor.longExecutable);
		assertNull(this.executor.longThread);
		assertNull(this.executor.longSkill);
		assertNull(this.executor.waitingLongMessage);

		assertTrue(this.executor.getSkillRegistryQueue().size() == 4);

		assertNull(this.status.getRunningShortExecutable());
		assertEquals(this.status.getLastShortExecutable(), this.executable1);
		assertNull(this.status.getRunningLongExecutable());
		assertEquals(this.status.getLastLongExecutable(), this.executable2);

	}

	/**
	 * stops a running long executable and executes a short executable that contained a dialog-start-request
	 */
	@Test
	public void test6_20() {

		SpeechMessage speechMessage1 = new SpeechMessage();
		speechMessage1.setLongRunning(false);
		speechMessage1.setStartDialogRequest(true);
		speechMessage1.setSkillRef(clockSkill1);
		speechMessage1.setSkillExecutable(executable1);

		SpeechMessage speechMessage2 = new SpeechMessage();
		speechMessage2.setLongRunning(true);
		speechMessage2.setSkillRef(clockSkill2);
		speechMessage2.setSkillExecutable(executable2);

		try {
			this.executor.getIncomingQueue().put(speechMessage2);
		} catch (InterruptedException e) {
			fail();
		}

		this.pause(500);

		assertNull(this.executor.shortExecutable);
		assertNull(this.executor.shortThread);
		assertNull(this.executor.shortSkill);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		assertEquals(this.executor.longExecutable, this.executable2);
		assertNotNull(this.executor.longThread);
		assertEquals(this.executor.longSkill, this.clockSkill2);
		assertNull(this.executor.waitingLongMessage);

		assertTrue(this.executor.getSkillRegistryQueue().size() == 1);

		assertNull(this.status.getRunningShortExecutable());
		assertNull(this.status.getLastShortExecutable());
		assertEquals(this.status.getRunningLongExecutable(), executable2);
		assertNull(this.status.getLastLongExecutable());

		try {
			this.executor.getIncomingQueue().put(speechMessage1);
		} catch (InterruptedException e) {
			fail();
		}

		this.pause(1000);

		assertEquals(this.executor.shortExecutable, executable1);
		assertNotNull(this.executor.shortThread);
		assertEquals(this.executor.shortSkill, clockSkill1);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		assertNull(this.executor.longExecutable);
		assertNull(this.executor.longThread);
		assertNull(this.executor.longSkill);
		assertNull(this.executor.waitingLongMessage);

		assertTrue(this.executor.getSkillRegistryQueue().size() == 3);

		assertEquals(this.status.getRunningShortExecutable(), executable1);
		assertNull(this.status.getLastShortExecutable());
		assertNull(this.status.getRunningLongExecutable());
		assertEquals(this.status.getLastLongExecutable(), executable2);

	}

	/**
	 * stops a running long executable to start another long executable
	 */
	@Test
	public void test13_27_34() {

		SpeechMessage speechMessage1 = new SpeechMessage();
		speechMessage1.setLongRunning(true);
		speechMessage1.setSkillRef(clockSkill1);
		speechMessage1.setSkillExecutable(executable1);

		SpeechMessage speechMessage2 = new SpeechMessage();
		speechMessage2.setLongRunning(true);
		speechMessage2.setSkillRef(clockSkill2);
		speechMessage2.setSkillExecutable(executable2);

		try {
			this.executor.getIncomingQueue().put(speechMessage1);
		} catch (InterruptedException e) {
			fail();
		}

		this.pause(500);

		assertNull(this.executor.shortExecutable);
		assertNull(this.executor.shortThread);
		assertNull(this.executor.shortSkill);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		assertEquals(this.executor.longExecutable, this.executable1);
		assertNotNull(this.executor.longThread);
		assertEquals(this.executor.longSkill, this.clockSkill1);
		assertNull(this.executor.waitingLongMessage);

		assertTrue(this.executor.getSkillRegistryQueue().size() == 1);

		assertNull(this.status.getRunningShortExecutable());
		assertNull(this.status.getLastShortExecutable());
		assertEquals(this.status.getRunningLongExecutable(), this.executable1);
		assertNull(this.status.getLastLongExecutable());

		try {
			this.executor.getIncomingQueue().put(speechMessage2);
		} catch (InterruptedException e) {
			fail();
		}

		this.pause(1000);

		assertNull(this.executor.shortExecutable);
		assertNull(this.executor.shortThread);
		assertNull(this.executor.shortSkill);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		assertEquals(this.executor.longExecutable, this.executable2);
		assertNotNull(this.executor.longThread);
		assertEquals(this.executor.longSkill, this.clockSkill2);
		assertNull(this.executor.waitingLongMessage);

		assertTrue(this.executor.getSkillRegistryQueue().size() == 3);

		assertNull(this.status.getRunningShortExecutable());
		assertNull(this.status.getLastShortExecutable());
		assertEquals(this.status.getRunningLongExecutable(), this.executable2);
		assertEquals(this.status.getLastLongExecutable(), executable1);

	}

	/**
	 * sends multiple short message with no gap to see if processing the list of executables works
	 */
	@Test
	public void testMultipleShortNoGap() {

		for (int i = 0; i < 3; i++) {
			SpeechMessage speechMessage = new SpeechMessage();
			speechMessage.setLongRunning(false);
			speechMessage.setSkillExecutable(new TestExecutable("exec" + i));
			speechMessage.setSkillRef(clockSkill1);

			try {
				this.executor.getIncomingQueue().put(speechMessage);
			} catch (InterruptedException e) {
				fail();
			}
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue(this.executor.shortExecutable.getExecutableId().equals("exec0"));
		assertNotNull(this.executor.shortThread);
		assertTrue(this.executor.waitingShortMessages.size() == 2);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue(this.executor.shortExecutable.getExecutableId().equals("exec1"));
		assertNotNull(this.executor.shortThread);
		assertTrue(this.executor.waitingShortMessages.size() == 1);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue(this.executor.shortExecutable.getExecutableId().equals("exec2"));
		assertNotNull(this.executor.shortThread);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			fail();
		}

		assertNull(this.executor.shortExecutable);
		assertNull(this.executor.shortThread);
		assertTrue(this.executor.waitingShortMessages.size() == 0);
	}

	/**
	 * sends multiple short message with a gap to see if processing the list of executables works
	 */
	@Test
	public void testMultipleShortGap() {

		SpeechMessage speechMessage = new SpeechMessage();
		speechMessage.setLongRunning(false);
		speechMessage.setSkillExecutable(new TestExecutable("exec0"));
		speechMessage.setSkillRef(clockSkill1);

		try {
			this.executor.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue(this.executor.shortExecutable.getExecutableId().equals("exec0"));
		assertNotNull(this.executor.shortThread);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			fail();
		}

		assertNull(this.executor.shortExecutable);
		assertNull(this.executor.shortThread);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		speechMessage = new SpeechMessage();
		speechMessage.setLongRunning(false);
		speechMessage.setSkillExecutable(new TestExecutable("exec1"));
		speechMessage.setSkillRef(clockSkill2);

		try {
			this.executor.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertTrue(this.executor.shortExecutable.getExecutableId().equals("exec1"));
		assertNotNull(this.executor.shortThread);
		assertTrue(this.executor.waitingShortMessages.size() == 0);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			fail();
		}

		assertNull(this.executor.shortExecutable);
		assertNull(this.executor.shortThread);
		assertTrue(this.executor.waitingShortMessages.size() == 0);
	}

	/**
	 * starts a short executable which is then stopped by a stop-message
	 */
	@Test
	public void testStopShort() {

		SpeechMessage speechMessage = new SpeechMessage();
		speechMessage.setLongRunning(false);
		speechMessage.setSkillExecutable(new TestExecutableLong("exec0"));
		speechMessage.setSkillRef(clockSkill1);

		try {
			this.executor.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertEquals(this.executor.shortExecutable, speechMessage.getExecutable());
		assertNotNull(this.executor.shortThread);

		StopMessage stopMessage = new StopMessage(null, false);

		try {
			this.executor.getIncomingQueue().put(stopMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertNull(this.executor.shortExecutable);
		assertNull(this.executor.shortThread);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			fail();
		}

	}

	/**
	 * starts a long executable which is then stopped by a stop-message
	 */
	@Test
	public void testStopLong() {

		SpeechMessage speechMessage = new SpeechMessage();
		speechMessage.setLongRunning(true);
		speechMessage.setSkillExecutable(new TestExecutableLong("exec0"));
		speechMessage.setSkillRef(clockSkill1);

		try {
			this.executor.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertEquals(this.executor.longExecutable, speechMessage.getExecutable());
		assertNotNull(this.executor.longThread);

		StopMessage stopMessage = new StopMessage(null, false);

		try {
			this.executor.getIncomingQueue().put(stopMessage);
		} catch (InterruptedException e) {
			fail();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}

		assertNull(this.executor.longExecutable);
		assertNull(this.executor.longThread);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			fail();
		}

	}

	private void pause(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			fail();
		}
	}

}
