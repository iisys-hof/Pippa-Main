package de.iisys.pippa.system.speech_recognizer;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iisys.pippa.core.status.StatusAccess;
import de.iisys.pippa.support.status.Status;

public class SpeechRecognizerTest {

	Thread t = null;
	SpeechRecognizer recognizer = null;
	StatusAccess status = Status.getInstance();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		recognizer = new SpeechRecognizer(status);
		t = new Thread(recognizer);
		t.start();
	}

	@SuppressWarnings("deprecation")
	@After
	public void tearDown() throws Exception {
		recognizer = null;
		t.stop();
		t = null;
	}

	@Test
	public void test() {
		
		try {
			Thread.sleep(300000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
