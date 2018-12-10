package de.iisys.pippa.service.speech_out.TTS.Flite;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FliteTTSTest {

	FliteTTS flite = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
		this.flite = new FliteTTS();
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConvert() {
		
		File wavFile = null;
		
		wavFile = this.flite.convert("Test Text", false);
		
		assertNotNull(wavFile);
		
	}
	

}
