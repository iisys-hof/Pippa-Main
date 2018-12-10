package de.iisys.pippa.system.scheduler;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message.speech_message.SpeechMessage;
import de.iisys.pippa.core.message.unschedule_message.UnscheduleMessage;

public class SchedulerTest {

	Scheduler scheduler = null;
	SpeechMessage speechMessage = null;
	UnscheduleMessage unscheduleMessage = null;
	Thread t = null;
	BlockingQueue<AMessage> feedbackQueue = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		this.feedbackQueue = new LinkedBlockingQueue<AMessage>();
		scheduler = new Scheduler();
		scheduler.feedbackQueue = feedbackQueue;
		t = new Thread(scheduler);
		t.start();

	}

	@SuppressWarnings("deprecation")
	@After
	public void tearDown() throws Exception {
		t.stop();
		t = null;
		scheduler = null;
		speechMessage = null;
		unscheduleMessage = null;
	}

	@Test
	public void testScheduleSingle() {
		
		this.speechMessage = new SpeechMessage();
		this.speechMessage.setFutureExecutionRequest(true);
		this.speechMessage.setExecutionDate(LocalDateTime.now().plusSeconds(3));
		
		try {
			this.scheduler.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}
		
		assertTrue(this.scheduler.getOutgoingQueue().size() == 0);
		assertTrue(this.scheduler.timers.size() == 1);
		assertTrue(this.feedbackQueue.size() == 1);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			fail();
		}
		
		assertTrue(this.scheduler.getOutgoingQueue().size() == 1);
		assertTrue(this.scheduler.timers.size() == 0);
		assertTrue(this.feedbackQueue.size() == 1);
	}
	
	@Test
	public void testScheduleMultiple() {

		for(int i = 0; i < 3; i++) {
			
			this.speechMessage = new SpeechMessage();
			this.speechMessage.setFutureExecutionRequest(true);
			this.speechMessage.setExecutionDate(LocalDateTime.now().plusSeconds(3));
			
			try {
				this.scheduler.getIncomingQueue().put(speechMessage);
			} catch (InterruptedException e) {
				fail();
			}
			
		}
			
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}
		
		assertTrue(this.scheduler.getOutgoingQueue().size() == 0);
		assertTrue(this.scheduler.timers.size() == 3);
		assertTrue(this.feedbackQueue.size() == 3);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			fail();
		}
		
		assertTrue(this.scheduler.getOutgoingQueue().size() == 3);
		assertTrue(this.scheduler.timers.size() == 0);
		assertTrue(this.feedbackQueue.size() == 3);
		
	}
	
	@Test
	public void testScheduleNoRequest() {
		
		this.speechMessage = new SpeechMessage();
		this.speechMessage.setFutureExecutionRequest(false);
		this.speechMessage.setExecutionDate(LocalDateTime.now().plusSeconds(3));
		
		try {
			this.scheduler.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}
		
		assertTrue(this.scheduler.getOutgoingQueue().size() == 1);
		assertTrue(this.scheduler.timers.size() == 0);
		assertTrue(this.feedbackQueue.size() == 1);
				
	}
	
	@Test
	public void testScheduleBygoneRequest() {
		
		this.speechMessage = new SpeechMessage();
		this.speechMessage.setFutureExecutionRequest(true);
		this.speechMessage.setExecutionDate(LocalDateTime.now().minusSeconds(3));
		
		try {
			this.scheduler.getIncomingQueue().put(speechMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}
		
		assertTrue(this.scheduler.getOutgoingQueue().size() == 1);
		assertTrue(this.scheduler.timers.size() == 0);
		assertTrue(this.feedbackQueue.size() == 1);
				
	}

	@Test
	public void testUnschedule() {

		for(int i = 0; i < 3; i++) {
			
			this.speechMessage = new SpeechMessage();
			this.speechMessage.setFutureExecutionRequest(true);
			this.speechMessage.setExecutionDate(LocalDateTime.now().plusSeconds(3));
			
			try {
				this.scheduler.getIncomingQueue().put(speechMessage);
			} catch (InterruptedException e) {
				fail();
			}
			
		}
			
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}
		
		assertTrue(this.scheduler.getOutgoingQueue().size() == 0);
		assertTrue(this.scheduler.timers.size() == 3);
		assertTrue(this.feedbackQueue.size() == 3);
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fail();
		}
		
		this.unscheduleMessage = new UnscheduleMessage(null, this.speechMessage.getMessageId());
		
		try {
			this.scheduler.getIncomingQueue().put(unscheduleMessage);
		} catch (InterruptedException e) {
			fail();
		}
		
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			fail();
		}		
		
		assertTrue(this.scheduler.getOutgoingQueue().size() == 2);
		assertTrue(this.scheduler.timers.size() == 0);
		assertTrue(this.feedbackQueue.size() == 4);
	}
	
}
