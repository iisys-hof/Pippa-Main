package de.iisys.pippa.system.speech_analyser.STT.Julius;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iisys.pippa.system.speech_analyser.SpeechAnalyser;

public class JuliusSTTTest {

	SpeechAnalyser sa = null;
	Thread t = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		sa = new SpeechAnalyser(); 
		t = new Thread(sa);
		t.start();
	}

	@After
	public void tearDown() throws Exception {
		sa = null;
		t.stop();
	}

	@Test
	public void test() {

	
		
		fail("Not yet implemented");
		
	}

}
